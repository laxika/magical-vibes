package com.github.laxika.magicalvibes.cards.b;

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
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BogardanFirefiendTest {

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
     * Sets up combat where Bogardan Firefiend (player1) attacks and is blocked by a 3/3 creature (player2).
     * Firefiend (2/1) will die from combat damage.
     */
    private void setupCombatWhereFirefiendDies() {
        Permanent firefiendPerm = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Bogardan Firefiend"))
                .findFirst().orElseThrow();
        firefiendPerm.setSummoningSick(false);
        firefiendPerm.setAttacking(true);

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
    @DisplayName("Bogardan Firefiend has correct card properties")
    void hasCorrectProperties() {
        BogardanFirefiend card = new BogardanFirefiend();

        assertThat(card.getName()).isEqualTo("Bogardan Firefiend");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{2}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(1);
        assertThat(card.getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ELEMENTAL, CardSubtype.SPIRIT);
        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst()).isInstanceOf(DealDamageToTargetCreatureEffect.class);
        DealDamageToTargetCreatureEffect dmg = (DealDamageToTargetCreatureEffect) card.getEffects(EffectSlot.ON_DEATH).getFirst();
        assertThat(dmg.damage()).isEqualTo(2);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Bogardan Firefiend puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new BogardanFirefiend()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Bogardan Firefiend"));
    }

    // ===== Death trigger with target selection =====

    @Test
    @DisplayName("When Bogardan Firefiend dies in combat, controller is prompted to choose a target creature")
    void deathTriggerPromptsForTarget() {
        harness.addToBattlefield(player1, new BogardanFirefiend());
        harness.addToBattlefield(player2, new GrizzlyBears());
        setupCombatWhereFirefiendDies();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Bogardan Firefiend should be dead
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Bogardan Firefiend"));

        // Player1 should be prompted to choose a target creature
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.awaitingPermanentChoicePlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Death trigger deals 2 damage to chosen creature and destroys it if lethal")
    void deathTriggerDeals2DamageAndKills() {
        harness.addToBattlefield(player1, new BogardanFirefiend());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereFirefiendDies();
        harness.passBothPriorities(); // Combat damage — Firefiend dies

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose the surviving Grizzly Bears (2/2)
        harness.handlePermanentChosen(player1, bearsId);

        // Triggered ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Bogardan Firefiend");
        assertThat(gd.stack.getFirst().getTargetPermanentId()).isEqualTo(bearsId);

        // Resolve the triggered ability — 2 damage to a 2/2 is lethal
        harness.passBothPriorities();

        // Grizzly Bears should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bearsId));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Death trigger deals 2 damage but does not kill a creature with toughness > 2")
    void deathTriggerDoesNotKillHighToughness() {
        harness.addToBattlefield(player1, new BogardanFirefiend());

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(3);
        bigBear.setToughness(3);
        harness.addToBattlefield(player2, bigBear);

        UUID bigBearId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereFirefiendDies();
        harness.passBothPriorities(); // Firefiend dies

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose the 3/3 bear
        harness.handlePermanentChosen(player1, bigBearId);

        // Resolve the triggered ability — 2 damage to a 3/3 is not lethal
        harness.passBothPriorities();

        // Grizzly Bears should still be alive
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(bigBearId));
    }

    @Test
    @DisplayName("Death trigger can target own creature")
    void deathTriggerCanTargetOwnCreature() {
        harness.addToBattlefield(player1, new BogardanFirefiend());
        harness.addToBattlefield(player1, new GrizzlyBears());

        UUID ownBearsId = harness.getPermanentId(player1, "Grizzly Bears");

        setupCombatWhereFirefiendDies();
        harness.passBothPriorities(); // Firefiend dies

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose own Grizzly Bears
        harness.handlePermanentChosen(player1, ownBearsId);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Own Grizzly Bears (2/2) should be destroyed by 2 damage
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(ownBearsId));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Death trigger skips if no creatures are on the battlefield (Wrath of God)")
    void deathTriggerSkipsWithNoCreatures() {
        harness.addToBattlefield(player1, new BogardanFirefiend());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Use Wrath of God to kill all creatures simultaneously
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities(); // Resolve Wrath — all creatures die

        GameData gd = harness.getGameData();

        // Both creatures should be dead
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Bogardan Firefiend"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Bogardan Firefiend"));
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
        harness.addToBattlefield(player1, new BogardanFirefiend());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereFirefiendDies();
        harness.passBothPriorities(); // Firefiend dies

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose target
        harness.handlePermanentChosen(player1, bearsId);

        // Remove the target before the ability resolves
        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getId().equals(bearsId));

        // Resolve — should fizzle
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Death trigger puts triggered ability on the stack with correct target")
    void deathTriggerPutsAbilityOnStack() {
        harness.addToBattlefield(player1, new BogardanFirefiend());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereFirefiendDies();
        harness.passBothPriorities(); // Firefiend dies

        GameData gd = harness.getGameData();

        // Choose target
        harness.handlePermanentChosen(player1, bearsId);

        // Ability should be on the stack with correct attributes
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Bogardan Firefiend")
                && e.getTargetPermanentId().equals(bearsId));
    }
}
