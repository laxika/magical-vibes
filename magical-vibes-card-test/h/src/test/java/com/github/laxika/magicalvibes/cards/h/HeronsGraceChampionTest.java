package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AvacynsPilgrim;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeronsGraceChampion.class, AvacynsPilgrim.class, EliteVanguard.class, GrizzlyBears.class})
class HeronsGraceChampionTest extends BaseCardTest {

    @Test
    @DisplayName("ETB boosts other Humans and grants them lifelink")
    void etbBoostsOtherHumansAndGrantsLifelink() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new AvacynsPilgrim());
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentHuman = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());

        castChampion();

        assertThat(human.getEffectivePower()).isEqualTo(2);
        assertThat(human.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, human, Keyword.LIFELINK)).isTrue();
        assertThat(nonHuman.getEffectivePower()).isEqualTo(2);
        assertThat(nonHuman.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nonHuman, Keyword.LIFELINK)).isFalse();
        assertThat(opponentHuman.getEffectivePower()).isEqualTo(2);
        assertThat(opponentHuman.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opponentHuman, Keyword.LIFELINK)).isFalse();

        Permanent champion = findPermanent(player1, "Heron's Grace Champion");
        assertThat(champion.getEffectivePower()).isEqualTo(3);
        assertThat(champion.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB boost and lifelink wear off at end of turn")
    void etbEffectsWearOffAtEndOfTurn() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new AvacynsPilgrim());

        castChampion();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(human.getEffectivePower()).isEqualTo(1);
        assertThat(human.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, human, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Flash allows casting on the opponent's turn")
    void canCastOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        castChampion();

        assertThat(countPermanents(player1, "Heron's Grace Champion")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Human entering after resolution does not receive the bonus")
    void laterHumanDoesNotReceiveBonus() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new HeronsGraceChampion());
        castChampion();
        Permanent later = harness.addToBattlefieldAndReturn(player1, new HeronsGraceChampion());

        assertThat(existing.getEffectivePower()).isEqualTo(4);
        assertThat(existing.getEffectiveToughness()).isEqualTo(4);
        assertThat(later.getEffectivePower()).isEqualTo(3);
        assertThat(later.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The ETB ability resolves even if its source leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new AvacynsPilgrim());
        Permanent champion = harness.enterBattlefieldAndReturn(player1, new HeronsGraceChampion());
        gd.playerBattlefields.get(player1.getId()).remove(champion);
        gd.playerGraveyards.get(player1.getId()).add(champion.getCard());

        resolveAllTriggers();

        assertThat(human.getEffectivePower()).isEqualTo(2);
        assertThat(human.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, human, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Granted lifelink gains life from combat damage")
    void grantedLifelinkGainsLife() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new AvacynsPilgrim());
        castChampion();
        human.setSummoningSick(false);
        human.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private void castChampion() {
        harness.castFromHand(player1, new HeronsGraceChampion(), "{2}{G}{W}");
        resolveAllTriggers();
    }

    @Test
    @DisplayName("The Champion's own lifelink gains life without another Human")
    void championsOwnLifelinkGainsLife() {
        castChampion();
        Permanent champion = findPermanent(player1, "Heron's Grace Champion");
        champion.setSummoningSick(false);
        champion.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
