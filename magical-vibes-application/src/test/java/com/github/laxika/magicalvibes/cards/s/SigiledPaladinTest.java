package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SigiledPaladin.class, GrizzlyBears.class})
class SigiledPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Exalted — another creature attacking alone gets +1/+1")
    void allyAttackingAloneBoosted() {
        addCreatureReady(player1, new SigiledPaladin());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1)); // Grizzly Bears attacks alone
        harness.passBothPriorities(); // resolve exalted trigger

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted — the Paladin attacking alone boosts itself")
    void selfAttackingAloneBoosted() {
        Permanent paladin = addCreatureReady(player1, new SigiledPaladin());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve exalted trigger

        assertThat(gqs.getEffectivePower(gd, paladin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, paladin)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new SigiledPaladin());
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
        addCreatureReady(player1, new SigiledPaladin());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1)); // both attack — not alone

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Sigiled Paladin"));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Paladin grants its own exalted bonus to a lone attacker")
    void multipleExaltedAbilitiesStack() {
        Permanent attacker = addCreatureReady(player1, new SigiledPaladin());
        Permanent supporter = addCreatureReady(player1, new SigiledPaladin());
        supporter.setTapped(true);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, supporter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, supporter)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted does not boost an opponent's lone attacker")
    void opposingAttackerIsNotBoosted() {
        addCreatureReady(player1, new SigiledPaladin());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("First strike kills an equal attacker before it damages the blocking Paladin")
    void firstStrikeWorksWhileBlocking() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new SigiledPaladin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(attacker.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isZero();
    }
}
