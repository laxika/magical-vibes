package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
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

class AcidWebSpiderTest extends BaseCardTest {

    /**
     * Casts Acid Web Spider and resolves it onto the battlefield, then accepts the may ability
     * and chooses the target Equipment so the ETB triggered ability is placed on the stack.
     */
    private void castAndAcceptMay(UUID equipmentId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AcidWebSpider()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept -> permanent choice prompt
        harness.handlePermanentChosen(player1, equipmentId); // choose target -> ETB on stack
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Acid Web Spider has correct card properties")
    void hasCorrectProperties() {
        AcidWebSpider card = new AcidWebSpider();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(DestroyTargetPermanentEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Acid Web Spider puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new AcidWebSpider()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Acid Web Spider");
    }

    @Test
    @DisplayName("Resolving puts Acid Web Spider on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new AcidWebSpider()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Acid Web Spider"));
    }

    // ===== ETB may ability =====

    @Test
    @DisplayName("Resolving Acid Web Spider triggers may ability prompt when Equipment exists")
    void resolvingTriggersMayPrompt() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new AcidWebSpider()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve -> may prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Accepting may ability prompts for Equipment target selection")
    void acceptingMayPromptsForTarget() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AcidWebSpider()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept -> permanent choice

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Choosing Equipment target puts ETB triggered ability on stack")
    void choosingTargetPutsEtbOnStack() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID equipmentId = harness.getPermanentId(player2, "Leonin Scimitar");
        castAndAcceptMay(equipmentId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Acid Web Spider");
        assertThat(gd.stack.getFirst().getTargetPermanentId()).isEqualTo(equipmentId);
    }

    @Test
    @DisplayName("ETB resolves and destroys target Equipment")
    void etbDestroysTargetEquipment() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID equipmentId = harness.getPermanentId(player2, "Leonin Scimitar");
        castAndAcceptMay(equipmentId);

        // Resolve ETB triggered ability
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Leonin Scimitar"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Leonin Scimitar"));
    }

    @Test
    @DisplayName("Declining may ability does not destroy Equipment")
    void decliningMaySkipsDestruction() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AcidWebSpider()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve -> may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        assertThat(gd.stack).isEmpty();
        // Acid Web Spider on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Acid Web Spider"));
        // Equipment still on battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leonin Scimitar"));
    }

    // ===== Multiple Equipment =====

    @Test
    @DisplayName("Can choose which Equipment to destroy when multiple exist")
    void canChooseWhichEquipmentToDestroy() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new LoxodonWarhammer());
        UUID warhammerId = harness.getPermanentId(player2, "Loxodon Warhammer");
        castAndAcceptMay(warhammerId);

        // Resolve ETB
        harness.passBothPriorities();

        // Loxodon Warhammer destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Loxodon Warhammer"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Loxodon Warhammer"));
        // Leonin Scimitar still on battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leonin Scimitar"));
    }

    // ===== No Equipment scenarios =====

    @Test
    @DisplayName("May prompt still fires when no Equipment on battlefield")
    void mayPromptFiresWithoutEquipment() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AcidWebSpider()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature -> may prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Accepting may with no Equipment results in no valid targets")
    void acceptingMayWithNoEquipmentHasNoValidTargets() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AcidWebSpider()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept -> no targets

        // Stack should be empty since there are no valid Equipment targets
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no valid targets"));
    }

    @Test
    @DisplayName("Declining may with no Equipment on battlefield")
    void decliningMayWithNoEquipment() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AcidWebSpider()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature -> may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Acid Web Spider"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("ETB fizzles if target Equipment is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID equipmentId = harness.getPermanentId(player2, "Leonin Scimitar");
        castAndAcceptMay(equipmentId);

        // Remove Equipment before ETB resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        // Resolve ETB -> fizzles
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Can target own Equipment =====

    @Test
    @DisplayName("Can target own Equipment")
    void canTargetOwnEquipment() {
        harness.addToBattlefield(player1, new LeoninScimitar());
        UUID equipmentId = harness.getPermanentId(player1, "Leonin Scimitar");
        castAndAcceptMay(equipmentId);

        // Resolve ETB
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Leonin Scimitar"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Leonin Scimitar"));
    }

    // ===== Acid Web Spider stays on battlefield =====

    @Test
    @DisplayName("Acid Web Spider remains on battlefield after destroying Equipment")
    void spiderRemainsOnBattlefield() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID equipmentId = harness.getPermanentId(player2, "Leonin Scimitar");
        castAndAcceptMay(equipmentId);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Acid Web Spider"));
    }

    // ===== Stack is empty after full resolution =====

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterFullResolution() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID equipmentId = harness.getPermanentId(player2, "Leonin Scimitar");
        castAndAcceptMay(equipmentId);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }
}
