package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BladeOfTheSixthPride;
import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.f.FatalAttraction;
import com.github.laxika.magicalvibes.cards.f.FomoriNomad;
import com.github.laxika.magicalvibes.cards.f.FlowstoneEmbrace;
import com.github.laxika.magicalvibes.cards.s.SpinIntoMyth;
import com.github.laxika.magicalvibes.cards.s.SuddenSpoiling;
import com.github.laxika.magicalvibes.cards.u.UmbralMantle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistmeadowSkulk.class, BlindPhantasm.class, BladeOfTheSixthPride.class,
        FomoriNomad.class, SpinIntoMyth.class, FatalAttraction.class, MoltenDisaster.class,
        SuddenSpoiling.class, FlowstoneEmbrace.class, UmbralMantle.class})
class MistmeadowSkulkTest extends BaseCardTest {

    @Test
    @DisplayName("Creature with mana value 3 cannot block Mistmeadow Skulk")
    void manaValue3CannotBlock() {
        addCreatureReady(player1, new MistmeadowSkulk());
        addCreatureReady(player2, new BlindPhantasm());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Creature with mana value 2 can block Mistmeadow Skulk")
    void manaValue2CanBlock() {
        addCreatureReady(player1, new MistmeadowSkulk());
        Permanent blocker = addCreatureReady(player2, new BladeOfTheSixthPride());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Mistmeadow Skulk takes no combat damage from a mana value 3 or greater creature")
    void takesNoDamageFromHighManaValueCreature() {
        addCreatureReady(player1, new FomoriNomad());
        addCreatureReady(player2, new MistmeadowSkulk());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat(player1);

        // The 4/4's damage to the 1/1 Skulk is prevented (protection from MV 3+); Skulk survives.
        harness.assertOnBattlefield(player2, "Mistmeadow Skulk");
    }

    @Test
    @DisplayName("A spell with mana value 3 or greater cannot target Mistmeadow Skulk")
    void highManaValueSpellCannotTarget() {
        Permanent skulk = addCreatureReady(player2, new MistmeadowSkulk());

        harness.setHand(player1, List.of(new SpinIntoMyth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, skulk.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("An Aura with mana value 3 or greater cannot enchant Mistmeadow Skulk")
    void highManaValueAuraCannotEnchant() {
        Permanent skulk = addCreatureReady(player2, new MistmeadowSkulk());

        harness.setHand(player1, List.of(new FatalAttraction()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, skulk.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Combat damage from Mistmeadow Skulk gains its controller that much life")
    void gainsLifeFromCombatDamage() {
        harness.setLife(player1, 10);
        addCreatureReady(player1, new MistmeadowSkulk());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
    }

    @Test
    @DisplayName("Blocked Skulk gains life even when killed by a low mana value blocker")
    void gainsLifeWhileDyingToLowManaValueBlocker() {
        harness.setLife(player1, 10);
        addCreatureReady(player1, new MistmeadowSkulk());
        addCreatureReady(player2, new BladeOfTheSixthPride());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Mistmeadow Skulk");
        harness.assertInGraveyard(player2, "Blade of the Sixth Pride");
        harness.assertLife(player1, 11);
    }

    @Test
    @DisplayName("A mana value 2 Aura can target and enchant Skulk")
    void lowManaValueAuraCanEnchant() {
        Permanent skulk = harness.addToBattlefieldAndReturn(player1, new MistmeadowSkulk());
        harness.setHand(player1, List.of(new FlowstoneEmbrace()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, skulk.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Flowstone Embrace").getAttachedTo()).isEqualTo(skulk.getId());
        harness.assertOnBattlefield(player1, "Mistmeadow Skulk");
    }

    @Test
    @DisplayName("A mana value 3 Equipment cannot equip Skulk, even for zero mana")
    void highManaValueEquipmentCannotEquip() {
        harness.addToBattlefield(player1, new UmbralMantle());
        Permanent skulk = harness.addToBattlefieldAndReturn(player1, new MistmeadowSkulk());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, skulk.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Chosen X contributes to the mana value of a damaging spell")
    void preventsDamageFromXSpellWithManaValueThree() {
        harness.addToBattlefield(player2, new MistmeadowSkulk());
        harness.setHand(player1, List.of(new MoltenDisaster()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 1);

        harness.assertOnBattlefield(player2, "Mistmeadow Skulk");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Skulk loses mana value protection when it loses all abilities")
    void highManaValueAttackerDamagesSkulkAfterAbilityRemoval() {
        addCreatureReady(player1, new FomoriNomad());
        addCreatureReady(player2, new MistmeadowSkulk());
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new SuddenSpoiling()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Mistmeadow Skulk");
        harness.assertLife(player2, 10);
    }
}
