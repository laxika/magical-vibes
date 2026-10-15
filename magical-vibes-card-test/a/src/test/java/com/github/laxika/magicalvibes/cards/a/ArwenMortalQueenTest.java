package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArwenMortalQueen.class, ElvishMystic.class, ArcaneSignet.class})
class ArwenMortalQueenTest extends BaseCardTest {

    @Test
    @DisplayName("Arwen enters with an indestructible counter")
    void entersWithIndestructibleCounter() {
        Permanent arwen = castArwen();

        assertThat(arwen.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, arwen, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Arwen removes an indestructible counter to strengthen another creature")
    void activatesOnAnotherCreature() {
        Permanent target = addCreatureReady(player2, new ElvishMystic());
        Permanent arwen = addCreatureReady(player1, new ArwenMortalQueen());
        arwen.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(arwen), null, target.getId());
        harness.passBothPriorities();

        assertThat(arwen.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(arwen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(arwen.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The granted indestructible keyword expires while counters remain")
    void grantedIndestructibleExpiresAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new ElvishMystic());
        Permanent arwen = addCreatureReady(player1, new ArwenMortalQueen());
        arwen.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(arwen), null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Arwen cannot target herself or activate without an indestructible counter")
    void enforcesCounterCostAndAnotherCreatureTarget() {
        Permanent arwen = addCreatureReady(player1, new ArwenMortalQueen());
        arwen.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(arwen), null, arwen.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature");

        Permanent target = addCreatureReady(player2, new ElvishMystic());
        arwen.setCounterCount(CounterType.INDESTRUCTIBLE, 0);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(arwen), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The counter is paid immediately and an illegal target prevents all benefits")
    void targetLeavingPreventsCountersOnArwen() {
        Permanent target = addCreatureReady(player2, new ElvishMystic());
        Permanent arwen = castArwen();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(arwen), null, target.getId());

        assertThat(arwen.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(gqs.hasKeyword(gd, arwen, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(arwen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(arwen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(arwen.getCounterCount(CounterType.LIFELINK)).isZero();
    }

    @Test
    @DisplayName("The target still gets all benefits when Arwen leaves before resolution")
    void sourceLeavingDoesNotPreventTargetBenefits() {
        Permanent target = addCreatureReady(player2, new ElvishMystic());
        Permanent arwen = castArwen();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(arwen), null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(arwen);
        gd.playerGraveyards.get(player1.getId()).add(arwen.getCard());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
        assertThat(arwen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(arwen.getCounterCount(CounterType.LIFELINK)).isZero();
    }

    @Test
    @DisplayName("Arwen can activate while tapped and summoning sick targeting an allied creature")
    void activatesWhileTappedAndSummoningSick() {
        Permanent target = addCreatureReady(player1, new ElvishMystic());
        Permanent arwen = castArwen();
        arwen.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(arwen), null, target.getId());
        harness.passBothPriorities();

        assertThat(arwen.isTapped()).isTrue();
        assertThat(arwen.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(arwen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(arwen.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, arwen, Keyword.LIFELINK)).isTrue();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Arwen cannot target a noncreature permanent")
    void rejectsNoncreatureTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ArcaneSignet());
        Permanent arwen = castArwen();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(arwen), null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(arwen.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(artifact.getCounterCount(CounterType.LIFELINK)).isZero();
    }

    private Permanent castArwen() {
        harness.castFromHand(player1, new ArwenMortalQueen(), "{1}{G}{W}");
        harness.passBothPriorities();
        return findPermanent(player1, "Arwen, Mortal Queen");
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
