package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
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

class FiendHunterTest extends BaseCardTest {

    /**
     * Casts Fiend Hunter, resolves it, accepts the may ability,
     * chooses a target creature, and resolves the ETB trigger.
     */
    private void castAndExileTarget(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FiendHunter()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> creature enters, MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept -> permanent choice
        harness.handlePermanentChosen(player1, targetId); // choose target -> effect resolves inline
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
        FiendHunter card = new FiendHunter();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(ExileTargetPermanentUntilSourceLeavesEffect.class);
    }

    // ===== ETB exile =====

    @Test
    @DisplayName("Resolving triggers may ability prompt when creature exists")
    void resolvingTriggersMayPrompt() {
        harness.addToBattlefield(player2, new GoblinPiker());
        harness.setHand(player1, List.of(new FiendHunter()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> creature enters, MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("ETB exiles target creature")
    void etbExilesTargetCreature() {
        harness.addToBattlefield(player2, new GoblinPiker());
        UUID creatureId = harness.getPermanentId(player2, "Goblin Piker");
        castAndExileTarget(creatureId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Goblin Piker"));
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Goblin Piker"));
    }

    @Test
    @DisplayName("Declining may ability does not exile anything")
    void decliningMaySkipsExile() {
        harness.addToBattlefield(player2, new GoblinPiker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FiendHunter()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> creature enters, MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Fiend Hunter"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Goblin Piker"));
    }

    // ===== LTB return =====

    @Test
    @DisplayName("Exiled card returns when Fiend Hunter dies")
    void exiledCardReturnsWhenHunterDies() {
        harness.addToBattlefield(player2, new GoblinPiker());
        UUID creatureId = harness.getPermanentId(player2, "Goblin Piker");
        castAndExileTarget(creatureId);

        // Verify creature is exiled
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Goblin Piker"));

        // Reset for follow-up spell
        resetForFollowUpSpell();

        // Kill Fiend Hunter with Shock
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID hunterId = harness.getPermanentId(player1, "Fiend Hunter");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, hunterId);
        harness.passBothPriorities(); // resolve Shock

        // Fiend Hunter is dead
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Fiend Hunter"));

        // Exiled card returns to battlefield under owner's control
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Goblin Piker"));
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Goblin Piker"));
    }

    @Test
    @DisplayName("Exiled card returns when Fiend Hunter is bounced")
    void exiledCardReturnsWhenHunterBounced() {
        harness.addToBattlefield(player2, new GoblinPiker());
        UUID creatureId = harness.getPermanentId(player2, "Goblin Piker");
        castAndExileTarget(creatureId);

        // Reset for follow-up spell
        resetForFollowUpSpell();

        // Bounce Fiend Hunter with Unsummon
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        UUID hunterId = harness.getPermanentId(player1, "Fiend Hunter");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, hunterId);
        harness.passBothPriorities(); // resolve Unsummon

        // Fiend Hunter is back in hand
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Fiend Hunter"));

        // Exiled card returns to battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Goblin Piker"));
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Goblin Piker"));
    }

    @Test
    @DisplayName("Exiled card returns under owner's control, not controller's")
    void exiledCardReturnsUnderOwnersControl() {
        harness.addToBattlefield(player2, new GoblinPiker());
        UUID creatureId = harness.getPermanentId(player2, "Goblin Piker");
        castAndExileTarget(creatureId);

        // Reset for follow-up spell
        resetForFollowUpSpell();

        // Kill Fiend Hunter
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID hunterId = harness.getPermanentId(player1, "Fiend Hunter");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, hunterId);
        harness.passBothPriorities();

        // Card returns under player2's control (the owner)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Goblin Piker"));
        // Not under player1's control
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Goblin Piker"));
    }

    // ===== Edge cases =====

    @Test
    @DisplayName("Nothing returns if may was declined")
    void nothingReturnsIfMayDeclined() {
        harness.addToBattlefield(player2, new GoblinPiker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FiendHunter()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> creature enters, MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        // Reset for follow-up spell
        resetForFollowUpSpell();

        // Kill Fiend Hunter
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID hunterId = harness.getPermanentId(player1, "Fiend Hunter");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, hunterId);
        harness.passBothPriorities();

        // Goblin Piker is still on battlefield (was never exiled)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Goblin Piker"));
        // Nothing weird returned
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Returned creature has summoning sickness")
    void returnedCreatureHasSummoningSickness() {
        harness.addToBattlefield(player2, new GoblinPiker());
        UUID creatureId = harness.getPermanentId(player2, "Goblin Piker");
        castAndExileTarget(creatureId);

        // Reset for follow-up spell
        resetForFollowUpSpell();

        // Kill Fiend Hunter
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID hunterId = harness.getPermanentId(player1, "Fiend Hunter");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, hunterId);
        harness.passBothPriorities();

        // The returned creature should have summoning sickness
        Permanent returned = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Piker"))
                .findFirst().orElseThrow();
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Exile tracking is cleaned up after source leaves")
    void exileTrackingCleanedUpAfterSourceLeaves() {
        harness.addToBattlefield(player2, new GoblinPiker());
        UUID creatureId = harness.getPermanentId(player2, "Goblin Piker");
        castAndExileTarget(creatureId);

        // There should be a tracking entry
        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();

        // Reset for follow-up spell
        resetForFollowUpSpell();

        // Kill Fiend Hunter
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID hunterId = harness.getPermanentId(player1, "Fiend Hunter");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, hunterId);
        harness.passBothPriorities();

        // Tracking entry should be removed
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }
}
