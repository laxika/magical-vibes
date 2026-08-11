package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CavalierOfFlameTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability boosts and grants haste to creatures you control until end of turn")
    void activatedAbilityBoostsOwnCreaturesAndGrantsHaste() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CavalierOfFlame());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(7);
        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);
        assertThat(ownCreature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.hasKeyword(Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(6);
        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Enters by discarding any number of cards and drawing that many")
    void entersWithRummage() {
        Card discardOne = new GrizzlyBears();
        Card discardTwo = new GrizzlyBears();
        Card kept = new GrizzlyBears();
        Card drawOne = new Forest();
        Card drawTwo = new Mountain();
        harness.setLibrary(player1, List.of(drawOne, drawTwo));
        harness.setHand(player1, List.of(new CavalierOfFlame(), discardOne, discardTwo, kept));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(kept, drawOne, drawTwo);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(discardOne, discardTwo);
    }

    @Test
    @DisplayName("Death deals damage equal to lands in its controller's graveyard to opponents and their planeswalkers")
    void deathDamagesOpponentsAndTheirPlaneswalkersBasedOnGraveyardLands() {
        Permanent cavalier = harness.addToBattlefieldAndReturn(player1, new CavalierOfFlame());
        Permanent planeswalker = new Permanent(new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);
        harness.setGraveyard(player1, List.of(new Forest(), new Mountain(), new GrizzlyBears()));
        cavalier.setMarkedDamage(6);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }
}
