package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.ArmoredGuardian;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NightscapeMaster.class, ArmoredGuardian.class, Island.class})
class NightscapeMasterTest extends BaseCardTest {

    @Test
    @DisplayName("First ability returns a target creature to its owner's hand")
    void returnsTargetCreatureToOwnersHand() {
        Permanent source = addCreatureReady(player1, new NightscapeMaster());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredGuardian());

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Armored Guardian");
        harness.assertInHand(player2, "Armored Guardian");
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second ability deals 2 damage to a target creature")
    void dealsTwoDamageToTargetCreature() {
        Permanent source = addCreatureReady(player1, new NightscapeMaster());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredGuardian());

        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    void canReturnItselfToHand() {
        Permanent source = addCreatureReady(player1, new NightscapeMaster());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, source.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nightscape Master");
        harness.assertInHand(player1, "Nightscape Master");
    }

    @Test
    void canDealLethalDamageToItself() {
        Permanent source = addCreatureReady(player1, new NightscapeMaster());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 1, null, source.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nightscape Master");
        harness.assertInGraveyard(player1, "Nightscape Master");
    }

    @Test
    void returnsStolenCreatureToOwnerRatherThanController() {
        addCreatureReady(player1, new NightscapeMaster());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredGuardian());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Armored Guardian");
        harness.assertInHand(player2, "Armored Guardian");
        harness.assertNotInHand(player1, "Armored Guardian");
    }

    @Test
    void neitherAbilityCanBeActivatedWithSummoningSickness() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new NightscapeMaster());
        source.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredGuardian());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        for (int abilityIndex = 0; abilityIndex < 2; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, index, null, target.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
        assertThat(source.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void neitherAbilityCanBeActivatedWithOnlyOneRequiredColoredMana() {
        Permanent source = addCreatureReady(player1, new NightscapeMaster());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredGuardian());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        for (int abilityIndex = 0; abilityIndex < 2; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, index, null, target.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
        assertThat(source.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Neither ability can target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent source = addCreatureReady(player1, new NightscapeMaster());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.addMana(player1, ManaColor.BLUE, 2);
        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(source),
                0,
                null,
                island.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 2);
        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(source),
                1,
                null,
                island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Neither ability can be activated while Nightscape Master is tapped")
    void cannotActivateWhenTapped() {
        Permanent source = addCreatureReady(player1, new NightscapeMaster());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredGuardian());
        source.tap();

        harness.addMana(player1, ManaColor.BLUE, 2);
        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(source),
                0,
                null,
                target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.addMana(player1, ManaColor.RED, 2);
        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(source),
                1,
                null,
                target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
