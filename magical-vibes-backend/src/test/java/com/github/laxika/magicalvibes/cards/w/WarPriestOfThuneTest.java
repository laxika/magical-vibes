package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
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
        harness.passBothPriorities(); // resolve creature spell -> may prompt
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
        harness.passBothPriorities(); // resolve -> may prompt

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
        harness.passBothPriorities(); // resolve -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept -> permanent choice

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Choosing enchantment target puts ETB triggered ability on stack")
    void choosingTargetPutsEtbOnStack() {
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID enchantmentId = harness.getPermanentId(player2, "Angelic Chorus");
        castAndAcceptMay(enchantmentId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("War Priest of Thune");
        assertThat(gd.stack.getFirst().getTargetPermanentId()).isEqualTo(enchantmentId);
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
        harness.passBothPriorities(); // resolve -> may prompt
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
        harness.passBothPriorities(); // resolve creature -> may prompt

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
        harness.passBothPriorities(); // resolve creature -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept -> no targets

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no valid targets"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("ETB fizzles if target enchantment is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID enchantmentId = harness.getPermanentId(player2, "Angelic Chorus");
        castAndAcceptMay(enchantmentId);

        // Remove enchantment before ETB resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        // Resolve ETB -> fizzles
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Can target own enchantment =====

    @Test
    @DisplayName("Can target own enchantment")
    void canTargetOwnEnchantment() {
        harness.addToBattlefield(player1, new AngelicChorus());
        UUID enchantmentId = harness.getPermanentId(player1, "Angelic Chorus");
        castAndAcceptMay(enchantmentId);

        // Resolve ETB
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Angelic Chorus"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Angelic Chorus"));
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
