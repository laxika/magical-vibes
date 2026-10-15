package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IgnobleHierarch.class, GrizzlyBears.class})
class IgnobleHierarchTest extends BaseCardTest {

    @Test
    @DisplayName("Exalted boosts the Hierarch itself when it attacks alone")
    void boostsItselfWhenAttackingAlone() {
        Permanent hierarch = addCreatureReady(player1, new IgnobleHierarch());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, hierarch)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hierarch)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Hierarch independently boosts a lone attacker")
    void multipleExaltedAbilitiesStack() {
        addCreatureReady(player1, new IgnobleHierarch());
        Permanent attacker = addCreatureReady(player1, new IgnobleHierarch());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("A tapped Hierarch still grants exalted to another attacker")
    void tappedHierarchStillGrantsExalted() {
        Permanent source = addCreatureReady(player1, new IgnobleHierarch());
        Permanent attacker = addCreatureReady(player1, new IgnobleHierarch());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        assertThat(source.isTapped()).isTrue();

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's Hierarch does not boost your lone attacker")
    void opponentsExaltedDoesNotApply() {
        addCreatureReady(player2, new IgnobleHierarch());
        Permanent attacker = addCreatureReady(player1, new IgnobleHierarch());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted resolves even after its source leaves the battlefield")
    void exaltedResolvesWithoutItsSource() {
        Permanent source = addCreatureReady(player1, new IgnobleHierarch());
        Permanent attacker = addCreatureReady(player1, new IgnobleHierarch());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(1)));
        assertThat(gd.stack).hasSize(2);
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted — another creature attacking alone gets +1/+1")
    void allyAttackingAloneBoosted() {
        addCreatureReady(player1, new IgnobleHierarch());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new IgnobleHierarch());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted does not trigger when attacking with more than one creature")
    void noTriggerWhenNotAlone() {
        addCreatureReady(player1, new IgnobleHierarch());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating the ability prompts a choice between black, red, and green")
    void activatingPromptsColorChoice() {
        addCreatureReady(player1, new IgnobleHierarch());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("BLACK", "RED", "GREEN");
    }

    @Test
    @DisplayName("Choosing a color adds one mana of that color and taps the Hierarch")
    void choosingColorAddsThatMana() {
        for (String color : new String[]{"BLACK", "RED", "GREEN"}) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();
            gd = harness.getGameData();

            Permanent hierarch = addCreatureReady(player1, new IgnobleHierarch());
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleListChoice(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
            assertThat(hierarch.isTapped()).isTrue();
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }
}
