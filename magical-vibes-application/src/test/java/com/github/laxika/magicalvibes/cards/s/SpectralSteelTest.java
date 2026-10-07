package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BeskirShieldmate;
import com.github.laxika.magicalvibes.cards.b.BoundInGold;
import com.github.laxika.magicalvibes.cards.g.GoldveinPick;
import com.github.laxika.magicalvibes.cards.p.PullFromEternity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpectralSteel.class, GoldveinPick.class, BeskirShieldmate.class, BoundInGold.class,
        PullFromEternity.class})
class SpectralSteelTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BeskirShieldmate());
        Permanent steel = harness.addToBattlefieldAndReturn(player1, new SpectralSteel());
        steel.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Graveyard ability returns an Aura and exiles Spectral Steel")
    void returnsAuraFromGraveyard() {
        Card steel = new SpectralSteel();
        Card aura = new BoundInGold();
        harness.setGraveyard(player1, List.of(steel, aura));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of(aura.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(aura.getId());
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(steel.getId()));
    }

    @Test
    @DisplayName("Graveyard ability returns an Equipment")
    void returnsEquipmentFromGraveyard() {
        Card steel = new SpectralSteel();
        Card equipment = new GoldveinPick();
        harness.setGraveyard(player1, List.of(steel, equipment));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of(equipment.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(equipment.getId());
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(steel.getId()));
    }

    @Test
    @DisplayName("Graveyard ability cannot target itself or a creature card")
    void rejectsInvalidTargets() {
        Card steel = new SpectralSteel();
        Card creature = new BeskirShieldmate();
        harness.setGraveyard(player1, List.of(steel, creature));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(steel.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canEnchantOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BeskirShieldmate());
        harness.setHand(player1, List.of(new SpectralSteel()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> creature.getId().equals(permanent.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void cannotEnchantEquipment() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new GoldveinPick());
        harness.setHand(player1, List.of(new SpectralSteel()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, equipment.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsAnotherSpectralSteelAndPaysExileBeforeResolution() {
        Card steel = new SpectralSteel();
        Card otherSteel = new SpectralSteel();
        harness.setGraveyard(player1, List.of(steel, otherSteel));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of(otherSteel.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherSteel);
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(steel.getId()));
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(otherSteel);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(otherSteel).doesNotContain(steel);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void rejectsOpponentsGraveyardWithoutExilingSource() {
        Card steel = new SpectralSteel();
        Card aura = new BoundInGold();
        harness.setGraveyard(player1, List.of(steel));
        harness.setGraveyard(player2, List.of(aura));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(aura.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(steel);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(aura);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void requiresExactlyOneTarget() {
        Card steel = new SpectralSteel();
        Card aura = new BoundInGold();
        Card equipment = new GoldveinPick();
        harness.setGraveyard(player1, List.of(steel, aura, equipment));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(aura.getId(), equipment.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(steel, aura, equipment);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayWithoutWhiteMana() {
        Card steel = new SpectralSteel();
        Card aura = new BoundInGold();
        harness.setGraveyard(player1, List.of(steel, aura));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(aura.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(steel, aura);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void missingTargetDoesNotReturnAnotherCardOrRefundExileCost() {
        Card steel = new SpectralSteel();
        Card aura = new BoundInGold();
        Card equipment = new GoldveinPick();
        harness.setGraveyard(player1, List.of(steel, aura, equipment));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of(aura.getId()));

        harness.setGraveyard(player1, List.of(equipment));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(steel, aura, equipment);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(equipment);
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(steel.getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({SpectralSteel.class, GoldveinPick.class, PullFromEternity.class})
    void targetThatLeavesAndReturnsToGraveyardIsANewObject() {
        Card steel = new SpectralSteel();
        Card targetSteel = new SpectralSteel();
        Card equipment = new GoldveinPick();
        harness.setGraveyard(player1, List.of(steel, targetSteel, equipment));
        harness.setHand(player1, List.of(new PullFromEternity()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of(targetSteel.getId()));
        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of(equipment.getId()));
        harness.castInstant(player1, 0, targetSteel.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(targetSteel);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(equipment);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(targetSteel);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(targetSteel);
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(steel.getId()));
        assertThat(gd.stack).isEmpty();
    }
}
