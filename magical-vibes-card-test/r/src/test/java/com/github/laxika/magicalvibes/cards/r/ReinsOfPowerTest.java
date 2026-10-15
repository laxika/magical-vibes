package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HammerheadShark;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.v.VolrathsStronghold;
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

@CardUsed({ReinsOfPower.class, HammerheadShark.class, VolrathsStronghold.class, Humility.class})
class ReinsOfPowerTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps and exchanges all creatures with the target opponent, granting haste")
    void untapsExchangesAndGrantsHaste() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HammerheadShark());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HammerheadShark());
        ownCreature.tap();
        opposingCreature.tap();

        castReins(player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opposingCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ownCreature);
        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opposingCreature.isTapped()).isFalse();
        assertThat(ownCreature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(opposingCreature.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not untap or exchange noncreatures")
    void doesNotUntapOrExchangeNoncreatures() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new VolrathsStronghold());
        ownLand.tap();
        opposingLand.tap();

        castReins(player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownLand);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingLand);
        assertThat(ownLand.isTapped()).isTrue();
        assertThat(opposingLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HammerheadShark());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HammerheadShark());

        castReins(player2.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingCreature);
        assertThat(ownCreature.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(opposingCreature.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new ReinsOfPower()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Takes every opposing creature even when the caster controls none")
    void takesCreaturesWithEmptyOwnBattlefield() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new HammerheadShark());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HammerheadShark());
        first.tap();
        second.tap();

        castReins(player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, first, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Gives away creatures even when the target opponent controls none")
    void givesCreaturesToEmptyOpposingBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HammerheadShark());
        creature.tap();

        castReins(player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(creature);
        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Resolves normally when neither player controls creatures")
    void resolvesWithBothBattlefieldsEmpty() {
        castReins(player2.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Reins of Power");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Creatures entering afterward are not exchanged or granted haste")
    void doesNotAffectLaterCreatures() {
        castReins(player2.getId());

        Permanent ownCreature = harness.enterBattlefieldAndReturn(player1, new HammerheadShark());
        Permanent opposingCreature = harness.enterBattlefieldAndReturn(player2, new HammerheadShark());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opposingCreature);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Two resolutions exchange creatures back and both control effects expire")
    void repeatedExchangeExpiresAtCleanup() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HammerheadShark());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HammerheadShark());

        castReins(player2.getId());
        castReins(player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opposingCreature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingCreature);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @CardUsed({ReinsOfPower.class, HammerheadShark.class, Humility.class})
    @DisplayName("Both sides gain haste when Reins resolves after Humility")
    void grantsHasteAfterHumility() {
        harness.enterBattlefieldAndReturn(player1, new Humility());
        Permanent ownCreature = harness.enterBattlefieldAndReturn(player1, new HammerheadShark());
        Permanent opposingCreature = harness.enterBattlefieldAndReturn(player2, new HammerheadShark());

        castReins(player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opposingCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ownCreature);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HASTE)).isTrue();
    }

    private void castReins(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new ReinsOfPower()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, targetPlayerId);
    }
}
