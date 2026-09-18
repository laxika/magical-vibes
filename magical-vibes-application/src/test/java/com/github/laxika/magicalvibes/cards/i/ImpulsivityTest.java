package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({Impulsivity.class, CounselOfTheSoratami.class, GrizzlyBears.class, Shock.class})
class ImpulsivityTest extends BaseCardTest {

    @Test
    @DisplayName("ETB can target an instant or sorcery card in any graveyard")
    void etbTargetsAnyGraveyard() {
        Shock ownInstant = new Shock();
        CounselOfTheSoratami opponentSorcery = new CounselOfTheSoratami();
        GrizzlyBears invalidCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownInstant));
        harness.setGraveyard(player2, List.of(opponentSorcery, invalidCreature));
        harness.setHand(player1, List.of(new Impulsivity()));
        addManaForCast();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                ownInstant.getId(), opponentSorcery.getId());
    }

    @Test
    @DisplayName("ETB casts the chosen spell for free and exiles it after resolution")
    void etbCastsForFreeAndExilesSpell() {
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Impulsivity()));
        addManaForCast();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(shock.getId()));
    }

    @Test
    @DisplayName("Encore creates a hasty attacking token and sacrifices it at the next end step")
    void encoreCreatesAttackingTokenAndSacrificesIt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Impulsivity()));
        addManaForEncore();

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Impulsivity");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Impulsivity")).isEmpty();
    }

    private void addManaForCast() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }

    private void addManaForEncore() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
    }
}
