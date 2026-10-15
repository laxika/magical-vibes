package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DeadlyRecluse;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattleSliver.class, BonescytheSliver.class, DeadlyRecluse.class, Disperse.class})
class BattleSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Battle Sliver boosts itself (it is a Sliver)")
    void boostsSelf() {
        Permanent sliver = addCreatureReady(player1, new BattleSliver());

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boosts another Sliver you control")
    void boostsOtherSliver() {
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());
        int basePower = gqs.getEffectivePower(gd, otherSliver);
        int baseToughness = gqs.getEffectiveToughness(gd, otherSliver);

        addCreatureReady(player1, new BattleSliver());

        assertThat(gqs.getEffectivePower(gd, otherSliver)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, otherSliver)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Does not boost a non-Sliver creature")
    void doesNotBoostNonSliver() {
        addCreatureReady(player1, new BattleSliver());
        Permanent recluse = addCreatureReady(player1, new DeadlyRecluse());

        assertThat(gqs.getEffectivePower(gd, recluse)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, recluse)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost an opponent's Sliver")
    void doesNotBoostOpponentSliver() {
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());
        int basePower = gqs.getEffectivePower(gd, opponentSliver);

        addCreatureReady(player1, new BattleSliver());

        assertThat(gqs.getEffectivePower(gd, opponentSliver)).isEqualTo(basePower);
    }

    @Test
    @DisplayName("Multiple Battle Slivers each boost every Sliver you control")
    void bonusesStack() {
        Permanent first = addCreatureReady(player1, new BattleSliver());
        Permanent second = addCreatureReady(player1, new BattleSliver());
        Permanent other = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Battle Sliver starts boosting only after its creature spell resolves")
    void boostBeginsOnResolution() {
        Permanent other = addCreatureReady(player1, new BonescytheSliver());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new BattleSliver(), "{4}{R}");

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Battle Sliver"))).isEqualTo(5);
    }

    @Test
    @DisplayName("Returning Battle Sliver to hand removes only its own bonus")
    void bonusEndsWhenSourceLeavesBattlefield() {
        Permanent first = addCreatureReady(player1, new BattleSliver());
        Permanent second = addCreatureReady(player1, new BattleSliver());
        Permanent other = addCreatureReady(player1, new BonescytheSliver());
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(6);
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, first.getId());

        harness.assertInHand(player1, "Battle Sliver");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }
}
