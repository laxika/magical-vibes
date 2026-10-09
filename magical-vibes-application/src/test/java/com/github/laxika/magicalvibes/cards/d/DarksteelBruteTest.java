package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BarbedLightning;
import com.github.laxika.magicalvibes.cards.e.EchoingRuin;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarksteelBrute.class, EchoingRuin.class, BarbedLightning.class})
class DarksteelBruteTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability makes it a 2/2 Beast artifact creature")
    void animatesIntoBeast() {
        Permanent brute = addBruteReady();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, indexOf(brute), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, brute)).isTrue();
        assertThat(gqs.isArtifact(brute)).isTrue();
        assertThat(gqs.getEffectivePower(gd, brute)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, brute)).isEqualTo(2);
        assertThat(gqs.hasEffectiveSubtype(gd, brute, CardSubtype.BEAST)).isTrue();
        assertThat(brute.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activating the ability consumes three generic mana")
    void manaIsConsumed() {
        Permanent brute = addBruteReady();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, indexOf(brute), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Animation ends at the end of the turn")
    void animationEndsAtEndOfTurn() {
        Permanent brute = addBruteReady();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, indexOf(brute), 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, brute)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, brute)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, brute, CardSubtype.BEAST)).isFalse();
    }

    @Test
    @DisplayName("Indestructible prevents destruction before animation")
    void survivesDestructionBeforeAnimation() {
        Permanent brute = addBruteReady();
        destroyWithEchoingRuin(brute);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(brute);
        assertThat(gqs.isCreature(gd, brute)).isFalse();
    }

    @Test
    @DisplayName("Animation preserves indestructible")
    void survivesDestructionAfterAnimation() {
        Permanent brute = addBruteReady();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(brute), 0, null, null);
        harness.passBothPriorities();

        destroyWithEchoingRuin(brute);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(brute);
        assertThat(gqs.isCreature(gd, brute)).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, brute)).isEqualTo(2);
    }

    @Test
    @DisplayName("A tapped summoning-sick Brute can animate repeatedly during an opponent's turn")
    void canAnimateWhileTappedAndSummoningSickOnOpponentsTurn() {
        Permanent brute = harness.addToBattlefieldAndReturn(player1, new DarksteelBrute());
        brute.tap();
        brute.setSummoningSick(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            harness.activateAbility(player1, indexOf(brute), 0, null, null);
            harness.passBothPriorities();
            harness.activateAbility(player1, indexOf(brute), 0, null, null);
            harness.passBothPriorities();
        });

        assertThat(gqs.isCreature(gd, brute)).isTrue();
        assertThat(gqs.getEffectivePower(gd, brute)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, brute)).isEqualTo(2);
        assertThat(brute.isTapped()).isTrue();
        assertThat(brute.isSummoningSick()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Animated Brute survives lethal damage without preventing that damage")
    void survivesLethalDamage() {
        Permanent brute = addBruteReady();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(brute), 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new BarbedLightning()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{0}, List.of(brute.getId()));
        harness.passBothPriorities();

        assertThat(brute.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(brute);
        assertThat(gqs.getEffectiveToughness(gd, brute)).isEqualTo(2);
    }

    private void destroyWithEchoingRuin(Permanent brute) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new EchoingRuin()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player2, 0, brute.getId());
    }

    private Permanent addBruteReady() {
        return addCreatureReady(player1, new DarksteelBrute());
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
