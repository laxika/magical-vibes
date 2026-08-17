package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PiousEvangelTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life when it enters")
    void gainsLifeWhenItEnters() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new PiousEvangel()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Gains life when another creature you control enters")
    void gainsLifeWhenAllyCreatureEnters() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new PiousEvangel());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        forceMainPhase();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Sacrifices another permanent and transforms")
    void sacrificesAnotherPermanentAndTransforms() {
        Permanent evangel = harness.addToBattlefieldAndReturn(player1, new PiousEvangel());
        evangel.setSummoningSick(false);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        forceMainPhase();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(evangel), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest.getCard());
        assertThat(evangel.isTransformed()).isTrue();
        assertThat(evangel.getCard().getName()).isEqualTo("Wayward Disciple");
    }

    @Test
    @DisplayName("When another creature you control dies, target opponent loses life and you gain life")
    void allyCreatureDeathDrainsOpponent() {
        putTransformedDiscipleOnBattlefield();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killWithShock("Grizzly Bears");

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("When Wayward Disciple dies, target opponent loses life and you gain life")
    void selfDeathDrainsOpponent() {
        Permanent disciple = putTransformedDiscipleOnBattlefield();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killWithMurder("Wayward Disciple");

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private Permanent putTransformedDiscipleOnBattlefield() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new PiousEvangel());
        disciple.setSummoningSick(false);
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        forceMainPhase();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(disciple), null, null);
        harness.passBothPriorities();
        return disciple;
    }

    private void killWithShock(String targetName) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(player1, targetName);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
    }

    private void killWithMurder(String targetName) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        UUID targetId = harness.getPermanentId(player1, targetName);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
    }

    private void forceMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
