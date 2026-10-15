package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LotusguardDisciple.class, GrizzlyBears.class, DuskLegionDreadnought.class, Forest.class})
class LotusguardDiscipleTest extends BaseCardTest {

    @Test
    @DisplayName("ETB grants lifelink and indestructible to a target creature")
    void grantsKeywordsToCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castWithTarget(bears);

        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("ETB grants lifelink and indestructible to a target Vehicle")
    void grantsKeywordsToVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new DuskLegionDreadnought());

        castWithTarget(vehicle);

        assertThat(vehicle.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(vehicle.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Granted keywords wear off at end of turn")
    void grantedKeywordsWearOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castWithTarget(bears);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isFalse();
        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature non-Vehicle permanent")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new LotusguardDisciple()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Keywords are granted by the ETB trigger, not by the creature spell")
    void keywordsAreGrantedOnlyWhenTriggerResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LotusguardDisciple());
        harness.setHand(player1, List.of(new LotusguardDisciple()));
        addMana();

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.LIFELINK)).isFalse();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("ETB trigger still grants keywords after its source leaves")
    void triggerResolvesAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LotusguardDisciple());
        harness.setHand(player1, List.of(new LotusguardDisciple()));
        addMana();
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("ETB trigger does not grant keywords to a replacement for a departed target")
    void departedTargetDoesNotTransferKeywords() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LotusguardDisciple());
        harness.setHand(player1, List.of(new LotusguardDisciple()));
        addMana();
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(target);
        Permanent replacement = harness.addToBattlefieldAndReturn(player2, new LotusguardDisciple());
        harness.passBothPriorities();

        assertThat(replacement.hasKeyword(Keyword.LIFELINK)).isFalse();
        assertThat(replacement.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Lotusguard Disciple");
    }

    private void castWithTarget(Permanent target) {
        harness.setHand(player1, List.of(new LotusguardDisciple()));
        addMana();
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
