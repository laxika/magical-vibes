package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NobleHierarch.class, GrizzlyBears.class})
class NobleHierarchTest extends BaseCardTest {

    @Test
    @DisplayName("Exalted — another creature attacking alone gets +1/+1")
    void allyAttackingAloneBoosted() {
        addCreatureReady(player1, new NobleHierarch());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1)); // Grizzly Bears attacks alone
        harness.passBothPriorities(); // resolve exalted trigger

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new NobleHierarch());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted does not trigger when attacking with more than one creature")
    void noTriggerWhenNotAlone() {
        addCreatureReady(player1, new NobleHierarch());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1)); // both attack — not alone

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating the ability prompts a choice between green, white, and blue")
    void activatingPromptsColorChoice() {
        addCreatureReady(player1, new NobleHierarch());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("GREEN", "WHITE", "BLUE");
    }

    @Test
    @DisplayName("Choosing a color adds exactly one mana of that color and taps the Hierarch")
    void choosingColorAddsThatMana() {
        for (String color : new String[]{"GREEN", "WHITE", "BLUE"}) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();
            gd = harness.getGameData();

            Permanent hierarch = addCreatureReady(player1, new NobleHierarch());
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleListChoice(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
            assertThat(hierarch.isTapped()).isTrue();
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    @Test
    void hierarchAttackingAloneBoostsItself() {
        Permanent hierarch = addCreatureReady(player1, new NobleHierarch());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, hierarch)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hierarch)).isEqualTo(2);
    }

    @Test
    void exaltedFromMultipleHierarchsStacksEvenWhenOneIsTappedAndSummoningSick() {
        Permanent attacker = addCreatureReady(player1, new NobleHierarch());
        Permanent other = addCreatureReady(player1, new NobleHierarch());
        other.setTapped(true);
        other.setSummoningSick(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
    }

    @Test
    void opposingHierarchDoesNotBoostLoneAttacker() {
        addCreatureReady(player1, new NobleHierarch());
        Permanent attacker = addCreatureReady(player2, new NobleHierarch());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    void multipleAttackersCreateNoExaltedTriggers() {
        addCreatureReady(player1, new NobleHierarch());
        addCreatureReady(player1, new NobleHierarch());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exaltedStillResolvesAfterSourceLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new NobleHierarch());
        Permanent attacker = addCreatureReady(player1, new NobleHierarch());

        declareAttackers(player1, List.of(1));
        assertThat(gd.stack).hasSize(2);
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    void summoningSicknessPreventsManaActivation() {
        Permanent hierarch = addCreatureReady(player1, new NobleHierarch());
        hierarch.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(hierarch.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void tappedHierarchCannotActivateAgain() {
        addCreatureReady(player1, new NobleHierarch());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
