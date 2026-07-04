package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class RestorationSeminarTest extends BaseCardTest {

    @Test
    @DisplayName("Has graveyard return to battlefield effect")
    void hasCorrectStructure() {
        RestorationSeminar card = new RestorationSeminar();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(ReturnCardFromGraveyardEffect.class);
        ReturnCardFromGraveyardEffect effect =
                (ReturnCardFromGraveyardEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.targetGraveyard()).isTrue();
    }

    @Test
    @DisplayName("Returns targeted nonland permanent from graveyard to battlefield")
    void returnsNonlandPermanentToBattlefield() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new RestorationSeminar()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Cannot target land card in graveyard")
    void cannotTargetLandInGraveyard() {
        var forest = new com.github.laxika.magicalvibes.cards.f.Forest();
        harness.setGraveyard(player1, List.of(forest));
        harness.setHand(player1, List.of(new RestorationSeminar()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonpermanent (instant/sorcery) card in graveyard")
    void cannotTargetInstantInGraveyard() {
        var shock = new com.github.laxika.magicalvibes.cards.s.Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new RestorationSeminar()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Paradigm exiles the spell and registers delayed trigger on first resolve")
    void paradigmExilesAndRegisters() {
        RestorationSeminar seminar = new RestorationSeminar();
        assumeTrue(seminar.getKeywords().contains(Keyword.PARADIGM),
                "Scryfall oracle must load Paradigm keyword");

        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(seminar));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Restoration Seminar"));
        assertThat(gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals("Restoration Seminar"))).isTrue();
        assertThat(gd.paradigmDelayedTriggers).hasSize(1);
        assertThat(gd.paradigmResolvedSpellNames.get(player1.getId())).contains("Restoration Seminar");
    }
}
