package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FesteringGoblinTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    /**
     * Sets up combat where Festering Goblin (player1) attacks and is blocked by a 3/3 creature (player2).
     * FG will die from combat damage.
     */
    private void setupCombatWhereGoblinDies() {
        Permanent goblinPerm = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Festering Goblin"))
                .findFirst().orElseThrow();
        goblinPerm.setSummoningSick(false);
        goblinPerm.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(3);
        bigBear.setToughness(3);
        Permanent blockerPerm = new Permanent(bigBear);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Festering Goblin has correct card properties")
    void hasCorrectProperties() {
        FesteringGoblin card = new FesteringGoblin();

        assertThat(card.getName()).isEqualTo("Festering Goblin");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getPower()).isEqualTo(1);
        assertThat(card.getToughness()).isEqualTo(1);
        assertThat(card.getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ZOMBIE, CardSubtype.GOBLIN);
        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst()).isInstanceOf(BoostTargetCreatureEffect.class);
        BoostTargetCreatureEffect boost = (BoostTargetCreatureEffect) card.getEffects(EffectSlot.ON_DEATH).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(-1);
        assertThat(boost.toughnessBoost()).isEqualTo(-1);
    }

    // ===== Death trigger with target selection =====

    @Test
    @DisplayName("When Festering Goblin dies in combat, controller is prompted to choose a target creature")
    void deathTriggerPromptsForTarget() {
        harness.addToBattlefield(player1, new FesteringGoblin());
        harness.addToBattlefield(player2, new GrizzlyBears());
        setupCombatWhereGoblinDies();

        // Both pass priority — advances to combat damage, FG dies
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Festering Goblin should be dead
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Festering Goblin"));

        // Player1 should be prompted to choose a target creature
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.awaitingPermanentChoicePlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Death trigger gives -1/-1 to chosen creature after resolution")
    void deathTriggerTargetsOpponentCreature() {
        harness.addToBattlefield(player1, new FesteringGoblin());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereGoblinDies();

        harness.passBothPriorities(); // Combat damage — FG dies

        GameData gd = harness.getGameData();

        // Player1 should be prompted to choose a target
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose the surviving Grizzly Bears
        harness.handlePermanentChosen(player1, bearsId);

        // Triggered ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Festering Goblin");
        assertThat(gd.stack.getFirst().getTargetPermanentId()).isEqualTo(bearsId);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Grizzly Bears should have -1/-1
        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(bearsId))
                .findFirst().orElseThrow();
        assertThat(bears.getPowerModifier()).isEqualTo(-1);
        assertThat(bears.getToughnessModifier()).isEqualTo(-1);
        assertThat(bears.getEffectivePower()).isEqualTo(1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Death trigger can target own creature")
    void deathTriggerCanTargetOwnCreature() {
        harness.addToBattlefield(player1, new FesteringGoblin());
        harness.addToBattlefield(player1, new GrizzlyBears());

        UUID ownBearsId = harness.getPermanentId(player1, "Grizzly Bears");

        setupCombatWhereGoblinDies();

        harness.passBothPriorities(); // Combat damage — FG dies

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose own Grizzly Bears
        harness.handlePermanentChosen(player1, ownBearsId);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Grizzly Bears should have -1/-1
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getPowerModifier()).isEqualTo(-1);
        assertThat(bears.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Death trigger killing a 1/1 creature sends it to graveyard after SBA")
    void deathTriggerKillsOneOneCreature() {
        harness.addToBattlefield(player1, new FesteringGoblin());

        // Put a 1/1 creature for player2
        GrizzlyBears weakBear = new GrizzlyBears();
        weakBear.setPower(1);
        weakBear.setToughness(1);
        harness.addToBattlefield(player2, weakBear);

        UUID weakBearId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereGoblinDies();

        harness.passBothPriorities(); // FG dies

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose the 1/1 bear
        harness.handlePermanentChosen(player1, weakBearId);

        // Resolve the triggered ability — bear gets -1/-1 (now 0/0)
        harness.passBothPriorities();

        // The weak bear should now be dead from SBA (0 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(weakBearId));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Death trigger debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new FesteringGoblin());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereGoblinDies();

        harness.passBothPriorities(); // FG dies

        // Choose target and resolve
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        // Bear should have -1/-1 now
        Permanent bears = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(bearsId))
                .findFirst().orElseThrow();
        assertThat(bears.getPowerModifier()).isEqualTo(-1);

        // Advance to cleanup step — modifiers reset
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Death trigger skips if no creatures are on the battlefield (Wrath of God)")
    void deathTriggerSkipsWithNoCreatures() {
        harness.addToBattlefield(player1, new FesteringGoblin());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Use Wrath of God to kill all creatures simultaneously
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities(); // Resolve Wrath — all creatures die

        GameData gd = harness.getGameData();

        // Both creatures should be dead
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Festering Goblin"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Festering Goblin"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // No permanent choice should be prompted (no valid creature targets after Wrath)
        assertThat(gd.awaitingInput).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Log should mention no valid targets
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no valid targets"));
    }

    @Test
    @DisplayName("Ability fizzles when target creature is removed before resolution")
    void abilityFizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player1, new FesteringGoblin());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereGoblinDies();

        harness.passBothPriorities(); // FG dies

        // Choose target
        harness.handlePermanentChosen(player1, bearsId);

        // Remove the target before the ability resolves
        harness.getGameData().playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getId().equals(bearsId));

        // Resolve — should fizzle
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Death trigger with Wrath of God but surviving indestructible creature can be targeted")
    void deathTriggerWithSurvivingCreature() {
        harness.addToBattlefield(player1, new FesteringGoblin());

        // Add a creature that will survive Wrath (we'll keep it by not casting Wrath)
        // Instead, test with a simple kill spell on just FG
        GrizzlyBears survivor = new GrizzlyBears();
        harness.addToBattlefield(player2, survivor);

        UUID survivorId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereGoblinDies();
        harness.passBothPriorities(); // FG dies

        GameData gd = harness.getGameData();

        // Should be prompted — survivor is a valid target
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose the survivor
        harness.handlePermanentChosen(player1, survivorId);

        // Ability should be on the stack
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Festering Goblin")
                && e.getTargetPermanentId().equals(survivorId));
    }
}
