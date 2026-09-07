package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchpriestOfShadows.class, GrizzlyBears.class, GiantGrowth.class})
class ArchpriestOfShadowsTest extends BaseCardTest {

    @Test
    @DisplayName("Backup puts a +1/+1 counter on another creature and grants deathtouch")
    void backsUpAnotherCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castArchpriest();

        resolveEtbTargeting(bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Backup targeting the source puts on the counter but does not grant deathtouch")
    void backingUpSourceDoesNotGrantDeathtouch() {
        castArchpriest();
        Permanent archpriest = findPermanent(player1, "Archpriest of Shadows");

        resolveEtbTargeting(archpriest);

        assertThat(archpriest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(archpriest.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("Backup's granted deathtouch expires at the end of the turn")
    void grantedDeathtouchExpiresAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castArchpriest();
        resolveEtbTargeting(bears);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage returns a creature card from the controller's graveyard to the battlefield")
    void combatDamageReturnsCreatureFromGraveyard() {
        Card bears = new GrizzlyBears();
        Card giantGrowth = new GiantGrowth();
        harness.setGraveyard(player1, List.of(bears, giantGrowth));
        Permanent archpriest = addCreatureReady(player1, new ArchpriestOfShadows());
        archpriest.setAttacking(true);

        dealCombatDamage();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(bears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(bears.getId()));
    }

    private void castArchpriest() {
        harness.setHand(player1, List.of(new ArchpriestOfShadows()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void resolveEtbTargeting(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private void dealCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
