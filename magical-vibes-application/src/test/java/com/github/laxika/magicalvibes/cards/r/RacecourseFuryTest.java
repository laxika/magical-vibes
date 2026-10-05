package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RacecourseFury.class, Mountain.class, DrudgeBeetle.class})
class RacecourseFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land taps to give target creature haste")
    void enchantedLandGrantsHaste() {
        Permanent mountain = attachFury(player1);
        Permanent bears = addCreatureReady(player1, new DrudgeBeetle());
        bears.setSummoningSick(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(mountain.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Granted haste wears off at cleanup")
    void hasteExpiresAtCleanup() {
        attachFury(player1);
        Permanent bears = addCreatureReady(player1, new DrudgeBeetle());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a non-land permanent")
    void cannotEnchantCreature() {
        Permanent bears = addCreatureReady(player1, new DrudgeBeetle());
        harness.setHand(player1, List.of(new RacecourseFury()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can enchant a land an opponent controls")
    void canEnchantOpponentLand() {
        Permanent opponentMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new RacecourseFury()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, opponentMountain.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> opponentMountain.getId().equals(p.getAttachedTo()));
    }

    @Test
    void landControllerCanActivateDespiteOpponentControllingAura() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new RacecourseFury()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();
        Permanent creature = addCreatureReady(player1, new DrudgeBeetle());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    void cannotActivateTappedLand() {
        Permanent land = attachFury(player1);
        Permanent creature = addCreatureReady(player1, new DrudgeBeetle());
        land.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    void grantedAbilityCannotTargetNoncreatureLand() {
        Permanent land = attachFury(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void abilityResolvesAfterAuraLeavesAndHasteRemainsUntilCleanup() {
        Permanent land = attachFury(player1);
        Permanent creature = addCreatureReady(player2, new DrudgeBeetle());
        Permanent aura = findPermanent(player1, "Racecourse Fury");
        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura);
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        land.untap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    private Permanent attachFury(Player player) {
        Permanent mountain = harness.addToBattlefieldAndReturn(player, new Mountain());
        Permanent aura = harness.addToBattlefieldAndReturn(player, new RacecourseFury());
        aura.setAttachedTo(mountain.getId());
        return mountain;
    }
}
