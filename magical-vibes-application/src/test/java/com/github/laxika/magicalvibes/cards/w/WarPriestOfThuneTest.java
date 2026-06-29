package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Telepathy;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WarPriestOfThuneTest extends BaseCardTest {

    /**
     * Casts War Priest of Thune and resolves it onto the battlefield, then accepts the may ability
     * and chooses the target enchantment so the ETB triggered ability is placed on the stack.
     */
    private void castAndAcceptMay(UUID enchantmentId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WarPriestOfThune()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept -> permanent choice prompt
        harness.handlePermanentChosen(player1, enchantmentId); // choose target -> ETB on stack
    }

    // ===== Card properties =====

    @Test
    @DisplayName("War Priest of Thune has correct card properties")
    void hasCorrectProperties() {
        WarPriestOfThune card = new WarPriestOfThune();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(DestroyTargetPermanentEffect.class);
    }

    // ===== ETB may ability =====

    @Test
    @DisplayName("Resolving War Priest of Thune triggers may ability prompt when enchantment exists")
    void resolvingTriggersMayPrompt() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new WarPriestOfThune()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Accepting may ability prompts for enchantment target selection")
    void acceptingMayPromptsForTarget() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WarPriestOfThune()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept -> permanent choice

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Choosing enchantment target resolves destroy effect inline")
    void choosingTargetResolvesDestroyInline() {
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID enchantmentId = harness.getPermanentId(player2, "Angelic Chorus");
        castAndAcceptMay(enchantmentId);

        // CR 603.5 — inner effect resolves inline when target is chosen
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Angelic Chorus"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Angelic Chorus"));
    }

    @Test
    @DisplayName("ETB resolves and destroys target enchantment")
    void etbDestroysTargetEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID enchantmentId = harness.getPermanentId(player2, "Angelic Chorus");
        castAndAcceptMay(enchantmentId);

        // Resolve ETB triggered ability
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Angelic Chorus"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Angelic Chorus"));
    }

    @Test
    @DisplayName("Declining may ability does not destroy enchantment")
    void decliningMaySkipsDestruction() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WarPriestOfThune()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("War Priest of Thune"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Angelic Chorus"));
    }

    // ===== No enchantment scenarios =====

    @Test
    @DisplayName("May prompt still fires when no enchantment on battlefield")
    void mayPromptFiresWithoutEnchantment() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WarPriestOfThune()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Accepting may with no enchantment results in no valid targets")
    void acceptingMayWithNoEnchantmentHasNoValidTargets() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WarPriestOfThune()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept -> no targets

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no valid targets"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Accepting may with target removed before resolution results in no valid targets")
    void acceptingMayAfterTargetRemovedHasNoValidTargets() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WarPriestOfThune()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> MayEffect on stack

        // Remove enchantment before MayEffect resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept -> no valid targets

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no valid targets"));
    }

    // ===== Can target own enchantment =====

    @Test
    @DisplayName("Can target own enchantment")
    void canTargetOwnEnchantment() {
        harness.addToBattlefield(player1, new Telepathy());
        UUID enchantmentId = harness.getPermanentId(player1, "Telepathy");
        castAndAcceptMay(enchantmentId);

        // CR 603.5 — inner effect resolves inline
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Telepathy"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Telepathy"));
    }

    // ===== War Priest stays on battlefield =====

    @Test
    @DisplayName("War Priest of Thune remains on battlefield after destroying enchantment")
    void priestRemainsOnBattlefield() {
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID enchantmentId = harness.getPermanentId(player2, "Angelic Chorus");
        castAndAcceptMay(enchantmentId);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("War Priest of Thune"));
    }
}
