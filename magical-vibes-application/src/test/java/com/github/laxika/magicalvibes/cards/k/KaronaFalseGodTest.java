package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.t.TreetopScout;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KaronaFalseGod.class, TreetopScout.class, KaronasZealot.class})
class KaronaFalseGodTest extends BaseCardTest {

    @Test
    @DisplayName("Each player's upkeep untaps Karona and gives that player control")
    void eachPlayersUpkeepUntapsAndGivesControl() {
        Permanent karona = harness.addToBattlefieldAndReturn(player1, new KaronaFalseGod());
        karona.tap();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(karona.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(karona);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(karona);
    }

    @Test
    @DisplayName("The controller's upkeep also untaps Karona and leaves control unchanged")
    void controllerUpkeepAlsoTriggers() {
        Permanent karona = harness.addToBattlefieldAndReturn(player1, new KaronaFalseGod());

        advanceToUpkeep(player1);
        karona.tap();
        harness.passBothPriorities();

        assertThat(karona.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(karona);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(karona);
    }

    @Test
    @DisplayName("Haste allows Karona to attack the turn it enters")
    void hasteAllowsAttackingImmediately() {
        Permanent karona = harness.addToBattlefieldAndReturn(player1, new KaronaFalseGod());

        declareAttackers(List.of(0));

        assertThat(karona.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Attacking lets you choose a creature type to boost across all battlefields")
    void attackingBoostsChosenCreatureType() {
        Permanent karona = addCreatureReady(player1, new KaronaFalseGod());
        Permanent ownElves = addCreatureReady(player1, new TreetopScout());
        Permanent opposingElves = addCreatureReady(player2, new TreetopScout());
        Permanent human = addCreatureReady(player2, new KaronasZealot());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");

        assertThat(gqs.getEffectivePower(gd, ownElves)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownElves)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingElves)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposingElves)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, karona)).isEqualTo(5);
    }

    @Test
    @DisplayName("The chosen creature type boost wears off at end of turn")
    void attackingBoostWearsOffAtEndOfTurn() {
        Permanent karona = addCreatureReady(player1, new KaronaFalseGod());
        Permanent elf = addCreatureReady(player1, new TreetopScout());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(1);
    }

}
