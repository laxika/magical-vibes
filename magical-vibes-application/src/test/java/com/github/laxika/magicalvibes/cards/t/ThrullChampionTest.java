package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BasalThrull;
import com.github.laxika.magicalvibes.cards.o.Orgg;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThrullChampion.class, BasalThrull.class, Orgg.class})
class ThrullChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Thrull creatures on all battlefields get +1/+1")
    void boostsThrullsOnAllBattlefields() {
        Permanent ownThrull = addCreatureReady(player1, new BasalThrull());
        Permanent opponentThrull = addCreatureReady(player2, new BasalThrull());
        Permanent nonThrull = addCreatureReady(player2, new Orgg());

        int ownPowerBefore = gqs.getEffectivePower(gd, ownThrull);
        int ownToughnessBefore = gqs.getEffectiveToughness(gd, ownThrull);
        int opponentPowerBefore = gqs.getEffectivePower(gd, opponentThrull);
        int opponentToughnessBefore = gqs.getEffectiveToughness(gd, opponentThrull);
        int nonThrullPowerBefore = gqs.getEffectivePower(gd, nonThrull);
        int nonThrullToughnessBefore = gqs.getEffectiveToughness(gd, nonThrull);

        addCreatureReady(player1, new ThrullChampion());

        assertThat(gqs.getEffectivePower(gd, ownThrull)).isEqualTo(ownPowerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, ownThrull)).isEqualTo(ownToughnessBefore + 1);
        assertThat(gqs.getEffectivePower(gd, opponentThrull)).isEqualTo(opponentPowerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, opponentThrull))
                .isEqualTo(opponentToughnessBefore + 1);
        assertThat(gqs.getEffectivePower(gd, nonThrull)).isEqualTo(nonThrullPowerBefore);
        assertThat(gqs.getEffectiveToughness(gd, nonThrull)).isEqualTo(nonThrullToughnessBefore);
    }

    @Test
    @DisplayName("Thrull Champion boosts itself")
    void boostsItself() {
        Permanent champion = new Permanent(new ThrullChampion());
        int powerBefore = champion.getEffectivePower();
        int toughnessBefore = champion.getEffectiveToughness();

        champion.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(champion);

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(toughnessBefore + 1);
    }

    @Test
    @DisplayName("Tapping Thrull Champion gains control of a target Thrull")
    void gainsControlOfTargetThrull() {
        Permanent champion = addCreatureReady(player1, new ThrullChampion());
        Permanent target = addCreatureReady(player2, new BasalThrull());

        activate(player1, champion, target);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(champion.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping Thrull Champion can target a Thrull you control")
    void canTargetThrullYouControl() {
        Permanent champion = addCreatureReady(player1, new ThrullChampion());
        Permanent target = addCreatureReady(player1, new BasalThrull());

        activate(player1, champion, target);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(champion.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated ability cannot target a non-Thrull")
    void cannotTargetNonThrull() {
        Permanent champion = addCreatureReady(player1, new ThrullChampion());
        Permanent target = addCreatureReady(player2, new Orgg());

        int championIndex = gd.playerBattlefields.get(player1.getId()).indexOf(champion);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, championIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Thrull");
    }

    @Test
    @DisplayName("Control returns when Thrull Champion leaves the battlefield")
    void controlReturnsWhenSourceLeaves() {
        Permanent champion = addCreatureReady(player1, new ThrullChampion());
        Permanent target = addCreatureReady(player2, new BasalThrull());

        activate(player1, champion, target);
        gd.playerBattlefields.get(player1.getId()).remove(champion);
        advanceToUpkeep(player1);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Control ends when another player gains control of Thrull Champion")
    void controlEndsWhenAnotherPlayerGainsControlOfSource() {
        Permanent champion = addCreatureReady(player1, new ThrullChampion());
        Permanent target = addCreatureReady(player2, new BasalThrull());
        Permanent opponentChampion = addCreatureReady(player2, new ThrullChampion());

        activate(player1, champion, target);
        activate(player2, opponentChampion, champion);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(champion.getId(), target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .doesNotContain(champion.getId(), target.getId());
    }

    @Test
    @DisplayName("Thrull Champion has no effect if it leaves before the ability resolves")
    void abilityHasNoEffectIfSourceLeavesBeforeResolution() {
        Permanent champion = addCreatureReady(player1, new ThrullChampion());
        Permanent target = addCreatureReady(player2, new BasalThrull());

        int championIndex = gd.playerBattlefields.get(player1.getId()).indexOf(champion);
        harness.activateAbility(player1, championIndex, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(champion);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    private void activate(Player controller, Permanent champion, Permanent target) {
        int championIndex = gd.playerBattlefields.get(controller.getId()).indexOf(champion);
        harness.activateAbility(controller, championIndex, null, target.getId());
        harness.passBothPriorities();
    }
}
