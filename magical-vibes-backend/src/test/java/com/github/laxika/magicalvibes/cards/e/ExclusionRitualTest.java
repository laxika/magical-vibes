package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellsWithSameNameAsExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndImprintEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExclusionRitualTest extends BaseCardTest {

    private void castAndResolveExclusionRitual(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ExclusionRitual()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities(); // resolve enchantment spell -> ETB on stack
        harness.passBothPriorities(); // resolve ETB -> exile + imprint
    }

    private void resetForFollowUpSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Card has ExileTargetPermanentAndImprintEffect on ETB and static casting restriction")
    void hasCorrectEffects() {
        ExclusionRitual card = new ExclusionRitual();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ExileTargetPermanentAndImprintEffect.class);

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(CantCastSpellsWithSameNameAsExiledCardEffect.class);
    }

    // ===== ETB exile =====

    @Test
    @DisplayName("ETB exiles target nonland permanent permanently")
    void etbExilesTargetNonlandPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolveExclusionRitual(bearsId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiled card stays exiled when Exclusion Ritual is destroyed (not O-ring style)")
    void exiledCardStaysExiledWhenSourceDestroyed() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolveExclusionRitual(bearsId);

        resetForFollowUpSpell();

        // Destroy Exclusion Ritual
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID ritualId = harness.getPermanentId(player1, "Exclusion Ritual");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, ritualId);
        harness.passBothPriorities();

        // Grizzly Bears should remain exiled — exile is permanent
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Static casting restriction =====

    @Test
    @DisplayName("Opponent cannot cast spells with same name as exiled card")
    void opponentCannotCastSpellsWithSameName() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolveExclusionRitual(bearsId);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Controller also cannot cast spells with same name as exiled card")
    void controllerAlsoCannotCastSpellsWithSameName() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolveExclusionRitual(bearsId);

        resetForFollowUpSpell();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Spells with different names can still be cast")
    void spellsWithDifferentNamesCanStillBeCast() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolveExclusionRitual(bearsId);

        // Naturalize has a different name than Grizzly Bears, so it should be castable
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        UUID ritualId = harness.getPermanentId(player1, "Exclusion Ritual");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castInstant(player2, 0, ritualId);

        assertThat(gd.stack).hasSize(1);
    }

    // ===== Restriction lifts when source leaves =====

    @Test
    @DisplayName("Casting restriction lifts when Exclusion Ritual is destroyed")
    void castingRestrictionLiftsWhenSourceDestroyed() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolveExclusionRitual(bearsId);

        resetForFollowUpSpell();

        // Destroy Exclusion Ritual
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID ritualId = harness.getPermanentId(player1, "Exclusion Ritual");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, ritualId);
        harness.passBothPriorities();

        // Casting restriction should be gone since Exclusion Ritual left the battlefield
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    // ===== Edge cases =====

    @Test
    @DisplayName("No exile-return tracking is created (exile is permanent)")
    void noExileReturnTracking() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolveExclusionRitual(bearsId);

        // No O-ring style tracking should exist
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }
}
