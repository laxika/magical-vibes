package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GravePactTest {

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
     * Makes player2 the active player in main phase 1, ready to cast sorceries.
     */
    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    /**
     * Sets up combat where player1's creature attacks and is blocked by a 2/4 Giant Spider
     * controlled by player2. The creature will die if its toughness is <= 2.
     * The blocking target is resolved by finding the attacker's actual battlefield index.
     */
    private void setupCombatWhereAttackerDies(String attackerName) {
        List<Permanent> p1Bf = harness.getGameData().playerBattlefields.get(player1.getId());
        int attackerIndex = -1;
        for (int i = 0; i < p1Bf.size(); i++) {
            if (p1Bf.get(i).getCard().getName().equals(attackerName)) {
                attackerIndex = i;
                break;
            }
        }
        Permanent attackerPerm = p1Bf.get(attackerIndex);
        attackerPerm.setSummoningSick(false);
        attackerPerm.setAttacking(true);

        Permanent blockerPerm = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Giant Spider"))
                .findFirst().orElseThrow();
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(attackerIndex);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Grave Pact has correct card properties")
    void hasCorrectProperties() {
        GravePact card = new GravePact();

        assertThat(card.getName()).isEqualTo("Grave Pact");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{1}{B}{B}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES).getFirst())
                .isInstanceOf(EachOpponentSacrificesCreatureEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Grave Pact puts it on the stack as an enchantment spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new GravePact()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Grave Pact");
    }

    @Test
    @DisplayName("Grave Pact resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new GravePact()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grave Pact"));
    }

    // ===== Triggering =====

    @Test
    @DisplayName("When controller's creature dies, Grave Pact's triggered ability goes on the stack")
    void triggersWhenControllerCreatureDies() {
        harness.addToBattlefield(player1, new GravePact());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castSorcery(player2, 0, player1.getId());

        // Resolve Cruel Edict → player1 auto-sacrifices creature → Grave Pact triggers
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Grave Pact");
        assertThat(trigger.getEffectsToResolve()).hasSize(1);
        assertThat(trigger.getEffectsToResolve().getFirst())
                .isInstanceOf(EachOpponentSacrificesCreatureEffect.class);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Opponent with one creature auto-sacrifices when trigger resolves")
    void opponentWithOneCreatureAutoSacrifices() {
        harness.addToBattlefield(player1, new GravePact());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castSorcery(player2, 0, player1.getId());

        harness.passBothPriorities(); // Resolve Cruel Edict
        harness.passBothPriorities(); // Resolve Grave Pact trigger

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Giant Spider"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Giant Spider"));
    }

    @Test
    @DisplayName("Opponent with multiple creatures is prompted to choose which to sacrifice")
    void opponentWithMultipleCreaturesIsPrompted() {
        harness.addToBattlefield(player1, new GravePact());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castSorcery(player2, 0, player1.getId());

        harness.passBothPriorities(); // Resolve Cruel Edict
        harness.passBothPriorities(); // Resolve Grave Pact trigger

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.awaitingPermanentChoicePlayerId).isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext).isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);
    }

    @Test
    @DisplayName("Opponent chooses which creature to sacrifice")
    void opponentChoosesCreatureToSacrifice() {
        harness.addToBattlefield(player1, new GravePact());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        UUID spiderId = harness.getPermanentId(player2, "Giant Spider");

        setupPlayer2Active();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castSorcery(player2, 0, player1.getId());

        harness.passBothPriorities(); // Resolve Cruel Edict
        harness.passBothPriorities(); // Resolve Grave Pact trigger → prompted

        // Player2 chooses to sacrifice Giant Spider
        harness.handlePermanentChosen(player2, spiderId);

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Giant Spider"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Giant Spider"));
    }

    @Test
    @DisplayName("No sacrifice when opponent has no creatures")
    void noSacrificeWhenOpponentHasNoCreatures() {
        harness.addToBattlefield(player1, new GravePact());
        harness.addToBattlefield(player1, new GrizzlyBears());
        // Player2 has no creatures

        setupPlayer2Active();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castSorcery(player2, 0, player1.getId());

        harness.passBothPriorities(); // Resolve Cruel Edict → Grave Pact triggers
        harness.passBothPriorities(); // Resolve Grave Pact trigger → no creatures

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no creatures to sacrifice"));
    }

    // ===== Does not trigger for opponent's creatures =====

    @Test
    @DisplayName("Does not trigger when opponent's creature dies")
    void doesNotTriggerWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new GravePact());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Player1 casts Cruel Edict targeting player2 — opponent's creature dies
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());

        harness.passBothPriorities(); // Resolve Cruel Edict

        GameData gd = harness.getGameData();
        // No Grave Pact trigger on the stack
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Multiple Grave Pacts =====

    @Test
    @DisplayName("Two Grave Pacts trigger separately for the same creature death")
    void multipleGravePactsTriggerSeparately() {
        harness.addToBattlefield(player1, new GravePact());
        harness.addToBattlefield(player1, new GravePact());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castSorcery(player2, 0, player1.getId());

        // Resolve Cruel Edict → player1 sacrifices → two Grave Pact triggers
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack).allMatch(e -> e.getCard().getName().equals("Grave Pact"));
    }

    // ===== Combat =====

    @Test
    @DisplayName("Grave Pact triggers when controller's creature dies in combat")
    void triggersWhenCreatureDiesInCombat() {
        harness.addToBattlefield(player1, new GravePact());
        harness.addToBattlefield(player1, new GrizzlyBears());
        // Player2 has Giant Spider (2/4, will kill the 2/2 attacker) and another creature
        harness.addToBattlefield(player2, new GiantSpider());
        harness.addToBattlefield(player2, new GrizzlyBears());

        setupCombatWhereAttackerDies("Grizzly Bears");

        // Pass priority → combat damage → GrizzlyBears (2/2) dies to Giant Spider (2/4)
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Player1's Grizzly Bears should be dead
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Grave Pact's triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Grave Pact"));
    }

    // ===== Wrath of God =====

    @Test
    @DisplayName("Wrath of God killing all creatures triggers Grave Pact for each of controller's creatures")
    void wrathOfGodTriggersForEachControllerCreature() {
        harness.addToBattlefield(player1, new GravePact());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GiantSpider());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        // Resolve Wrath of God — all creatures die
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // All creatures should be dead
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getType() == CardType.CREATURE);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getType() == CardType.CREATURE);

        // Grave Pact should still be on the battlefield (it's an enchantment)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grave Pact"));

        // Two Grave Pact triggers (one for each of player1's creatures that died)
        long gravePactTriggers = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Grave Pact"))
                .count();
        assertThat(gravePactTriggers).isEqualTo(2);
    }

    @Test
    @DisplayName("Grave Pact triggers from Wrath resolve with no effect when opponent has no creatures left")
    void wrathTriggersResolveWithNoCreaturesLeft() {
        harness.addToBattlefield(player1, new GravePact());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities(); // Resolve Wrath of God
        harness.passBothPriorities(); // Resolve Grave Pact trigger

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        // Player2's creature died to Wrath, not to Grave Pact sacrifice
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no creatures to sacrifice"));
    }
}

