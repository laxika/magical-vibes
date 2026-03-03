package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LeoninRelicWarderTest extends BaseCardTest {

    /**
     * Casts Leonin Relic-Warder, resolves it, accepts the may ability,
     * chooses a target, and resolves the ETB trigger.
     */
    private void castAndExileTarget(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LeoninRelicWarder()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept -> permanent choice
        harness.handlePermanentChosen(player1, targetId); // choose target -> ETB on stack
        harness.passBothPriorities(); // resolve ETB trigger
    }

    /**
     * Resets game state to allow casting more spells after castAndExileTarget.
     */
    private void resetForFollowUpSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Card has MayEffect wrapping ExileTargetPermanentUntilSourceLeavesEffect")
    void hasCorrectEffects() {
        LeoninRelicWarder card = new LeoninRelicWarder();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(ExileTargetPermanentUntilSourceLeavesEffect.class);
    }

    // ===== ETB exile =====

    @Test
    @DisplayName("Resolving triggers may ability prompt when artifact exists")
    void resolvingTriggersMayPrompt() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new LeoninRelicWarder()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> may prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("ETB exiles target artifact")
    void etbExilesTargetArtifact() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID artifactId = harness.getPermanentId(player2, "Leonin Scimitar");
        castAndExileTarget(artifactId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Leonin Scimitar"));
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Leonin Scimitar"));
    }

    @Test
    @DisplayName("ETB exiles target enchantment")
    void etbExilesTargetEnchantment() {
        // Put Pacifism directly on the battlefield
        harness.addToBattlefield(player2, new Pacifism());

        UUID pacifismId = harness.getPermanentId(player2, "Pacifism");
        castAndExileTarget(pacifismId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Pacifism"));
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Pacifism"));
    }

    @Test
    @DisplayName("Declining may ability does not exile anything")
    void decliningMaySkipsExile() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LeoninRelicWarder()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve -> may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leonin Relic-Warder"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leonin Scimitar"));
    }

    // ===== LTB return =====

    @Test
    @DisplayName("Exiled card returns when Leonin Relic-Warder dies")
    void exiledCardReturnsWhenWarderDies() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID artifactId = harness.getPermanentId(player2, "Leonin Scimitar");
        castAndExileTarget(artifactId);

        // Verify artifact is exiled
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Leonin Scimitar"));

        // Reset for follow-up spell
        resetForFollowUpSpell();

        // Kill Leonin Relic-Warder with Shock
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID warderId = harness.getPermanentId(player1, "Leonin Relic-Warder");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, warderId);
        harness.passBothPriorities(); // resolve Shock

        // Leonin Relic-Warder is dead
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Leonin Relic-Warder"));

        // Exiled card returns to battlefield under owner's control
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leonin Scimitar"));
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Leonin Scimitar"));
    }

    @Test
    @DisplayName("Exiled card returns when Leonin Relic-Warder is bounced")
    void exiledCardReturnsWhenWarderBounced() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID artifactId = harness.getPermanentId(player2, "Leonin Scimitar");
        castAndExileTarget(artifactId);

        // Reset for follow-up spell
        resetForFollowUpSpell();

        // Bounce Leonin Relic-Warder with Unsummon
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        UUID warderId = harness.getPermanentId(player1, "Leonin Relic-Warder");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, warderId);
        harness.passBothPriorities(); // resolve Unsummon

        // Leonin Relic-Warder is back in hand
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Leonin Relic-Warder"));

        // Exiled card returns to battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leonin Scimitar"));
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Leonin Scimitar"));
    }

    @Test
    @DisplayName("Exiled card returns under owner's control, not controller's")
    void exiledCardReturnsUnderOwnersControl() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID artifactId = harness.getPermanentId(player2, "Leonin Scimitar");
        castAndExileTarget(artifactId);

        // Reset for follow-up spell
        resetForFollowUpSpell();

        // Kill Leonin Relic-Warder
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID warderId = harness.getPermanentId(player1, "Leonin Relic-Warder");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, warderId);
        harness.passBothPriorities();

        // Card returns under player2's control (the owner)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leonin Scimitar"));
        // Not under player1's control
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Leonin Scimitar"));
    }

    // ===== Edge cases =====

    @Test
    @DisplayName("Nothing returns if may was declined")
    void nothingReturnsIfMayDeclined() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LeoninRelicWarder()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature -> may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        // Reset for follow-up spell
        resetForFollowUpSpell();

        // Kill Leonin Relic-Warder
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID warderId = harness.getPermanentId(player1, "Leonin Relic-Warder");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, warderId);
        harness.passBothPriorities();

        // Leonin Scimitar is still on battlefield (was never exiled)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leonin Scimitar"));
        // Nothing weird returned
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("ETB fizzles if target is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID artifactId = harness.getPermanentId(player2, "Leonin Scimitar");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LeoninRelicWarder()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept -> permanent choice
        harness.handlePermanentChosen(player1, artifactId); // choose target -> ETB on stack

        // Remove target before ETB resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        // Resolve ETB -> fizzles
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        // No exile-return tracking should exist
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Returned permanent has summoning sickness")
    void returnedPermanentHasSummoningSickness() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID artifactId = harness.getPermanentId(player2, "Leonin Scimitar");
        castAndExileTarget(artifactId);

        // Reset for follow-up spell
        resetForFollowUpSpell();

        // Kill Leonin Relic-Warder
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID warderId = harness.getPermanentId(player1, "Leonin Relic-Warder");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, warderId);
        harness.passBothPriorities();

        // The returned permanent should have summoning sickness
        Permanent returned = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Leonin Scimitar"))
                .findFirst().orElseThrow();
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Exile tracking is cleaned up after source leaves")
    void exileTrackingCleanedUpAfterSourceLeaves() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID artifactId = harness.getPermanentId(player2, "Leonin Scimitar");
        castAndExileTarget(artifactId);

        // There should be a tracking entry
        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();

        // Reset for follow-up spell
        resetForFollowUpSpell();

        // Kill Leonin Relic-Warder
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID warderId = harness.getPermanentId(player1, "Leonin Relic-Warder");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, warderId);
        harness.passBothPriorities();

        // Tracking entry should be removed
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }
}
