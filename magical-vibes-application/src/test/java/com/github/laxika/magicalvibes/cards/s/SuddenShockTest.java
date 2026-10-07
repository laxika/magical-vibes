package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.k.KherKeep;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuddenShock.class, AshcoatBear.class, ThinkTwice.class, KherKeep.class,
        JaceBeleren.class, SkulkingKnight.class})
class SuddenShockTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to target player")
    void dealsTwoDamageToTargetPlayer() {
        harness.setHand(player1, List.of(new SuddenShock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Deals 2 damage to target creature")
    void dealsTwoDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new SuddenShock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Ashcoat Bear");
        harness.assertInGraveyard(player2, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Deals 2 damage to target planeswalker")
    void dealsTwoDamageToTargetPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        target.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new SuddenShock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents a spell response while on the stack")
    void preventsSpellResponse() {
        harness.setHand(player1, List.of(new SuddenShock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Allows mana abilities but prevents non-mana abilities while on the stack")
    void allowsManaAbilitiesButPreventsNonManaAbilities() {
        Permanent manaSource = harness.addToBattlefieldAndReturn(player2, new KherKeep());
        harness.setHand(player1, List.of(new SuddenShock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("split second");
        assertThat(manaSource.isTapped()).isFalse();

        harness.activateAbility(player2, 0, 0, null, null);
        assertThat(manaSource.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Split second also prevents its controller from casting another spell")
    void preventsControllerFromCastingAnotherSpell() {
        harness.setHand(player1, List.of(new SuddenShock(), new ThinkTwice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Think Twice");
    }

    @Test
    @DisplayName("Can target its controller and permits spells again after resolving")
    void canTargetControllerAndSpellRestrictionEndsAfterResolution() {
        harness.setHand(player1, List.of(new SuddenShock(), new SuddenShock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.assertLife(player1, 18);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Non-mana abilities can be activated after Sudden Shock resolves")
    void abilityRestrictionEndsAfterResolution() {
        harness.addToBattlefield(player2, new KherKeep());
        harness.setHand(player1, List.of(new SuddenShock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Kobolds of Kher Keep");
    }

    @Test
    @DisplayName("Triggered abilities resolve above Sudden Shock and can remove its target")
    void triggeredAbilityCanRemoveTargetWhileSplitSecondIsActive() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SkulkingKnight());
        harness.setHand(player1, List.of(new SuddenShock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Skulking Knight");
        harness.assertInGraveyard(player2, "Skulking Knight");
        harness.assertNotInGraveyard(player1, "Sudden Shock");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sudden Shock");
    }
}
