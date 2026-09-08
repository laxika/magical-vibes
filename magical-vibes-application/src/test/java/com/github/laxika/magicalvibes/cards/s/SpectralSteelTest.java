package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
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

@CardUsed({SpectralSteel.class, Bonesplitter.class, GrizzlyBears.class, Pacifism.class})
class SpectralSteelTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        Permanent steel = new Permanent(new SpectralSteel());
        steel.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(steel);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Graveyard ability returns an Aura and exiles Spectral Steel")
    void returnsAuraFromGraveyard() {
        Card steel = new SpectralSteel();
        Card aura = new Pacifism();
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
        Card equipment = new Bonesplitter();
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
        Card creature = new GrizzlyBears();
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
}
