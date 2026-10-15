package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowHarrier;
import com.github.laxika.magicalvibes.cards.e.EyeblightsEnding;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlackPoplarShaman.class, GoldmeadowHarrier.class, EyeblightsEnding.class})
class BlackPoplarShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Activating regeneration targets a Treefolk and puts ability on stack")
    void activatingTargetsTreefolk() {
        addCreatureReady(player1, new BlackPoplarShaman());
        Permanent treefolk = addCreatureReady(player1, new BlackPoplarShaman());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, treefolk.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(treefolk.getId());
    }

    @Test
    @DisplayName("Resolving regeneration grants a regeneration shield to target Treefolk")
    void resolvingGrantsShield() {
        addCreatureReady(player1, new BlackPoplarShaman());
        Permanent treefolk = addCreatureReady(player1, new BlackPoplarShaman());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, treefolk.getId());
        harness.passBothPriorities();

        assertThat(treefolk.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can regenerate itself (it is a Treefolk)")
    void canRegenerateItself() {
        Permanent shaman = addCreatureReady(player1, new BlackPoplarShaman());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, shaman.getId());
        harness.passBothPriorities();

        assertThat(shaman.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target an opponent's Treefolk")
    void canTargetOpponentsTreefolk() {
        addCreatureReady(player1, new BlackPoplarShaman());
        Permanent treefolk = addCreatureReady(player2, new BlackPoplarShaman());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, treefolk.getId());
        harness.passBothPriorities();

        assertThat(treefolk.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-Treefolk creature")
    void cannotTargetNonTreefolk() {
        addCreatureReady(player1, new BlackPoplarShaman());
        Permanent nonTreefolk = addCreatureReady(player1, new GoldmeadowHarrier());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonTreefolk.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Treefolk");
    }

    @Test
    @DisplayName("Cannot activate regeneration without enough mana")
    void cannotActivateWithoutMana() {
        Permanent shaman = addCreatureReady(player1, new BlackPoplarShaman());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shaman.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new BlackPoplarShaman());
        shaman.setSummoningSick(true);
        shaman.tap();
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, shaman.getId());
        harness.passBothPriorities();

        assertThat(shaman.getRegenerationShield()).isEqualTo(1);
        assertThat(shaman.isTapped()).isTrue();
    }

    @Test
    void repeatedActivationsCreateSeparateShieldsWithoutTapping() {
        Permanent shaman = addCreatureReady(player1, new BlackPoplarShaman());
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.activateAbility(player1, 0, null, shaman.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, shaman.getId());
        harness.passBothPriorities();

        assertThat(shaman.getRegenerationShield()).isEqualTo(2);
        assertThat(shaman.isTapped()).isFalse();
    }

    @Test
    void shieldReplacesDestructionAndIsConsumed() {
        Permanent shaman = addCreatureReady(player1, new BlackPoplarShaman());
        shaman.setMarkedDamage(1);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateAbility(player1, 0, null, shaman.getId());
        harness.passBothPriorities();

        assertThat(shaman.isTapped()).isFalse();
        assertThat(shaman.getMarkedDamage()).isEqualTo(1);

        harness.setHand(player2, List.of(new EyeblightsEnding(), new EyeblightsEnding()));
        harness.addMana(player2, ManaColor.BLACK, 6);
        harness.castAndResolveInstant(player2, 0, shaman.getId());

        harness.assertOnBattlefield(player1, "Black Poplar Shaman");
        harness.assertNotInGraveyard(player1, "Black Poplar Shaman");
        assertThat(shaman.isTapped()).isTrue();
        assertThat(shaman.getMarkedDamage()).isZero();
        assertThat(shaman.getRegenerationShield()).isZero();

        harness.castAndResolveInstant(player2, 0, shaman.getId());

        harness.assertNotOnBattlefield(player1, "Black Poplar Shaman");
        harness.assertInGraveyard(player1, "Black Poplar Shaman");
    }

    @Test
    void abilityDoesNotResolveWhenTargetLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new BlackPoplarShaman());
        Permanent target = addCreatureReady(player2, new BlackPoplarShaman());
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateAbility(player1, 0, null, target.getId());

        harness.setHand(player2, List.of(new EyeblightsEnding()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Black Poplar Shaman");
        assertThat(target.getRegenerationShield()).isZero();
        assertThat(source.getRegenerationShield()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
