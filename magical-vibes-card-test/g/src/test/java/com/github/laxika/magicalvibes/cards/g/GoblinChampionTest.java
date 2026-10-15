package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinChampion.class, GrizzlyBears.class})
class GoblinChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Exalted boosts another creature attacking alone")
    void allyAttackingAloneBoosted() {
        addCreatureReady(player1, new GoblinChampion());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted boosts Goblin Champion attacking alone")
    void selfAttackingAloneBoosted() {
        Permanent champion = addCreatureReady(player1, new GoblinChampion());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new GoblinChampion());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted does not trigger when multiple creatures attack")
    void noTriggerWhenNotAlone() {
        addCreatureReady(player1, new GoblinChampion());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Goblin Champion"));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Champions each boost the creature attacking alone")
    void multipleExaltedAbilitiesStack() {
        Permanent attacker = addCreatureReady(player1, new GoblinChampion());
        addCreatureReady(player1, new GoblinChampion());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opposing Champion does not boost a creature you control")
    void opposingExaltedDoesNotApply() {
        Permanent attacker = addCreatureReady(player1, new GoblinChampion());
        addCreatureReady(player2, new GoblinChampion());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted resolves after its source leaves the battlefield")
    void exaltedResolvesWithoutSource() {
        Permanent source = addCreatureReady(player1, new GoblinChampion());
        Permanent attacker = addCreatureReady(player1, new GoblinChampion());

        declareAttackers(player1, List.of(1));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Haste allows a newly entered Champion to attack and receive exalted")
    void newlyEnteredChampionCanAttack() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new GoblinChampion());
        champion.setSummoningSick(true);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(2);
    }
}
