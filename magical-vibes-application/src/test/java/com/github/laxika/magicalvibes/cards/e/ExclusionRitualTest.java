package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.PullFromEternity;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WordOfSeizing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExclusionRitual.class, GrizzlyBears.class, Naturalize.class, PullFromEternity.class,
        Plains.class, WordOfSeizing.class})
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

    // ===== ETB exile =====

    @Test
    @DisplayName("ETB exiles target nonland permanent permanently")
    void etbExilesTargetNonlandPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolveExclusionRitual(bearsId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
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
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
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
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");

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

    @Test
    @DisplayName("Casting restriction ends when the imprinted card leaves exile")
    void castingRestrictionEndsWhenExiledCardLeavesExile() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player2, bears);
        castAndResolveExclusionRitual(harness.getPermanentId(player2, "Grizzly Bears"));

        resetForFollowUpSpell();
        harness.setHand(player1, List.of(new PullFromEternity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(bears.getId())).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Exclusion Ritual");
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ETB still exiles its target if Exclusion Ritual leaves first")
    void targetIsExiledWhenSourceLeavesBeforeTriggerResolves() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player2, bears);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ExclusionRitual()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castEnchantment(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Exclusion Ritual"));
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Exclusion Ritual");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Plains());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ExclusionRitual()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                harness.getPermanentId(player2, "Plains")))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Plains");
    }

    @Test
    @DisplayName("Exiles noncreature permanents and prevents casting their names")
    void exilesNoncreaturePermanentAndRestrictsItsName() {
        ExclusionRitual target = new ExclusionRitual();
        harness.addToBattlefield(player2, target);
        castAndResolveExclusionRitual(harness.getPermanentId(player2, "Exclusion Ritual"));

        harness.assertNotOnBattlefield(player2, "Exclusion Ritual");
        assertThat(gd.findExiledCard(target.getId())).isNotNull();
        resetForFollowUpSpell();
        assertThatThrownBy(() -> harness.castFromHand(player1, new ExclusionRitual(), "{4}{W}{W}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Changing the source's controller before its ETB resolves preserves imprint")
    void changingControllerBeforeTriggerResolvesPreservesRestriction() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player2, bears);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ExclusionRitual()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castEnchantment(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new WordOfSeizing()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Exclusion Ritual"));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Exclusion Ritual");
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
        assertThatThrownBy(() -> harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
