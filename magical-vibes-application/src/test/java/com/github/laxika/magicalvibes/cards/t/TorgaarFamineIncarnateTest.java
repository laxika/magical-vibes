package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreaturesForCostReductionEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetPlayerLifeToHalfStartingEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TorgaarFamineIncarnateTest extends BaseCardTest {

    @Test
    @DisplayName("Card has correct effects configured")
    void hasCorrectEffects() {
        TorgaarFamineIncarnate card = new TorgaarFamineIncarnate();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(SacrificeCreaturesForCostReductionEffect.class);
        SacrificeCreaturesForCostReductionEffect sacEffect =
                (SacrificeCreaturesForCostReductionEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(sacEffect.reductionPerCreature()).isEqualTo(2);

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(SetTargetPlayerLifeToHalfStartingEffect.class);
    }

    @Test
    @DisplayName("Cast with full mana cost (no sacrifices), ETB sets opponent life to 10")
    void castWithFullManaCostSetsOpponentLifeTo10() {
        harness.setHand(player1, List.of(new TorgaarFamineIncarnate()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreatureWithSacrificeForReduction(player1, 0, player2.getId(), List.of());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Torgaar, Famine Incarnate"));
    }

    @Test
    @DisplayName("Cast with sacrifice reduces mana cost by 2 per creature")
    void castWithSacrificeReducesCost() {
        // Add a creature to battlefield to sacrifice
        Permanent creature1 = harness.addToBattlefieldAndReturn(player1, new com.github.laxika.magicalvibes.cards.t.TolarianScholar());

        harness.setHand(player1, List.of(new TorgaarFamineIncarnate()));
        // {6}{B}{B} minus 2 for sacrificing 1 creature = {4}{B}{B}
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithSacrificeForReduction(player1, 0, player2.getId(), List.of(creature1.getId()));
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
        // Sacrificed creature should be gone
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Tolarian Scholar"));
    }

    @Test
    @DisplayName("Cast with multiple sacrifices reduces cost further")
    void castWithMultipleSacrificesReducesCostFurther() {
        Permanent creature1 = harness.addToBattlefieldAndReturn(player1, new com.github.laxika.magicalvibes.cards.t.TolarianScholar());
        Permanent creature2 = harness.addToBattlefieldAndReturn(player1, new com.github.laxika.magicalvibes.cards.t.TolarianScholar());
        Permanent creature3 = harness.addToBattlefieldAndReturn(player1, new com.github.laxika.magicalvibes.cards.t.TolarianScholar());

        harness.setHand(player1, List.of(new TorgaarFamineIncarnate()));
        // {6}{B}{B} minus 6 for sacrificing 3 creatures = {B}{B}
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreatureWithSacrificeForReduction(player1, 0, player2.getId(),
                List.of(creature1.getId(), creature2.getId(), creature3.getId()));
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("ETB can target self (controller)")
    void etbCanTargetSelf() {
        harness.setHand(player1, List.of(new TorgaarFamineIncarnate()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        // Set player1 life to something other than 10
        harness.setLife(player1, 25);

        harness.castCreatureWithSacrificeForReduction(player1, 0, player1.getId(), List.of());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("ETB sets life to 10 even if target has less than 10 life")
    void etbSetsLifeTo10EvenIfBelow() {
        harness.setHand(player1, List.of(new TorgaarFamineIncarnate()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        // Set opponent life to below 10
        harness.setLife(player2, 5);

        harness.castCreatureWithSacrificeForReduction(player1, 0, player2.getId(), List.of());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Life becomes 10 (which is a gain from 5)
        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("ETB with no target chosen does not change any life total")
    void etbWithNoTargetDoesNothing() {
        harness.setHand(player1, List.of(new TorgaarFamineIncarnate()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        int p1Life = gd.getLife(player1.getId());
        int p2Life = gd.getLife(player2.getId());

        // Cast with null target (up to one — choose zero)
        harness.castCreatureWithSacrificeForReduction(player1, 0, null, List.of());
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.getLife(player1.getId())).isEqualTo(p1Life);
        assertThat(gd.getLife(player2.getId())).isEqualTo(p2Life);
    }

    @Test
    @DisplayName("Cannot sacrifice non-creatures for cost reduction")
    void cannotSacrificeNonCreatures() {
        // Add a non-creature permanent (land)
        com.github.laxika.magicalvibes.cards.f.Forest forest = new com.github.laxika.magicalvibes.cards.f.Forest();
        Permanent land = new Permanent(forest);
        gd.playerBattlefields.get(player1.getId()).add(land);

        harness.setHand(player1, List.of(new TorgaarFamineIncarnate()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castCreatureWithSacrificeForReduction(
                player1, 0, player2.getId(), List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Can only sacrifice creatures");
    }

    @Test
    @DisplayName("Sacrificed creatures go to graveyard")
    void sacrificedCreaturesGoToGraveyard() {
        Permanent creature1 = harness.addToBattlefieldAndReturn(player1, new com.github.laxika.magicalvibes.cards.t.TolarianScholar());

        harness.setHand(player1, List.of(new TorgaarFamineIncarnate()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithSacrificeForReduction(player1, 0, player2.getId(), List.of(creature1.getId()));
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tolarian Scholar"));
    }
}
