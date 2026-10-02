package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PlanarCleansing;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;

@CardUsed({GravePact.class, DarkBanishing.class, CruelEdict.class, GrizzlyBears.class, GiantSpider.class,
        WrathOfGod.class, Mountain.class, PlanarCleansing.class})
class GravePactTest extends BaseCardTest {

    /**
     * Makes player2 the active player in main phase 1, ready to cast spells.
     */
    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void setupCombatWhereAttackerDies() {
        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
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
        assertThat(entry.getCard()).isInstanceOf(GravePact.class);
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
        harness.assertOnBattlefield(player1, "Grave Pact");
    }

    // ===== Triggering =====

    @Test
    @DisplayName("When controller's creature dies, Grave Pact's triggered ability goes on the stack")
    void triggersWhenControllerCreatureDies() {
        harness.addToBattlefield(player1, new GravePact());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        setupPlayer2Active();
        castDarkBanishingAtGrizzlyBears(player2, player1);

        // Resolve Dark Banishing → player1's creature dies → Grave Pact triggers
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Grizzly Bears");

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard()).isInstanceOf(GravePact.class);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Opponent with one creature auto-sacrifices when trigger resolves")
    void opponentWithOneCreatureAutoSacrifices() {
        harness.addToBattlefield(player1, new GravePact());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GiantSpider());
        harness.addToBattlefield(player2, new GiantSpider());

        setupPlayer2Active();
        castDarkBanishingAtGrizzlyBears(player2, player1);

        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Giant Spider");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Grave Pact does not make its controller sacrifice another creature")
    void doesNotSacrificeControllerCreature() {
        harness.addToBattlefield(player1, new GravePact());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GiantSpider());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID sacrificedByEdict = harness.getPermanentId(player1, "Grizzly Bears");

        setupPlayer2Active();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castSorcery(player2, 0, player1.getId());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sacrificedByEdict);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Giant Spider");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opponent with multiple creatures is prompted to choose which to sacrifice")
    void opponentWithMultipleCreaturesIsPrompted() {
        harness.addToBattlefield(player1, new GravePact());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        setupPlayer2Active();
        castDarkBanishingAtGrizzlyBears(player2, player1);

        harness.passBothPriorities(); // Resolve Dark Banishing
        harness.passBothPriorities(); // Resolve Grave Pact trigger

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);
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
        castDarkBanishingAtGrizzlyBears(player2, player1);

        harness.passBothPriorities(); // Resolve Dark Banishing
        harness.passBothPriorities(); // Resolve Grave Pact trigger → prompted

        // Player2 chooses to sacrifice Giant Spider
        harness.handlePermanentChosen(player2, spiderId);

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("No sacrifice when opponent has no creatures")
    void noSacrificeWhenOpponentHasNoCreatures() {
        harness.addToBattlefield(player1, new GravePact());
        harness.addToBattlefield(player1, new GrizzlyBears());
        // Player2 has no creatures

        setupPlayer2Active();
        castDarkBanishingAtGrizzlyBears(player2, player1);

        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("no creatures to sacrifice")).isTrue();
    }

    @Test
    @DisplayName("Does not sacrifice opponent's noncreature permanents")
    void doesNotSacrificeOpponentNoncreaturePermanent() {
        harness.addToBattlefield(player1, new GravePact());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Mountain());

        setupPlayer2Active();
        castDarkBanishingAtGrizzlyBears(player2, player1);

        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("Triggers when Grave Pact is destroyed at the same time as a creature")
    void triggersWhenDestroyedWithControllerCreature() {
        harness.addToBattlefield(player1, new GravePact());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Grave Pact");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard() instanceof GravePact);
    }

    // ===== Does not trigger for opponent's creatures =====

    @Test
    @DisplayName("Does not trigger when opponent's creature dies")
    void doesNotTriggerWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new GravePact());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Player1 casts Dark Banishing targeting player2's Grizzly Bears — opponent's creature dies
        castDarkBanishingAtGrizzlyBears(player1, player2);

        harness.passBothPriorities(); // Resolve Dark Banishing

        GameData gd = harness.getGameData();
        // No Grave Pact trigger on the stack
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
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
        castDarkBanishingAtGrizzlyBears(player2, player1);

        // Resolve Dark Banishing → player1's creature dies → two Grave Pact triggers
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack).allMatch(e -> e.getCard() instanceof GravePact);
    }

    // ===== Combat =====

    @Test
    @DisplayName("Grave Pact triggers when controller's creature dies in combat")
    void triggersWhenCreatureDiesInCombat() {
        harness.addToBattlefield(player1, new GravePact());
        addCreatureReady(player1, new GrizzlyBears());
        // Player2 has Giant Spider (2/4, will kill the 2/2 attacker) and another creature
        harness.addToBattlefield(player2, new GiantSpider());
        harness.addToBattlefield(player2, new GrizzlyBears());

        setupCombatWhereAttackerDies();

        // Pass priority → combat damage → GrizzlyBears (2/2) dies to Giant Spider (2/4)
        resolveCombat();

        GameData gd = harness.getGameData();
        // Player1's Grizzly Bears should be dead
        harness.assertInGraveyard(player1, "Grizzly Bears");

        // Grave Pact's triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard() instanceof GravePact);
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

        harness.castSorcery(player1, 0, 0);
        // Resolve Wrath of God — all creatures die
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // All creatures should be dead
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.CREATURE));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.CREATURE));

        // Grave Pact should still be on the battlefield (it's an enchantment)
        harness.assertOnBattlefield(player1, "Grave Pact");

        // Two Grave Pact triggers (one for each of player1's creatures that died)
        long gravePactTriggers = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard() instanceof GravePact)
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

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        // Player2's creature died to Wrath, not to Grave Pact sacrifice
        assertThat(gameLogContains("no creatures to sacrifice")).isTrue();
    }

    private void castDarkBanishingAtGrizzlyBears(Player caster, Player target) {
        harness.setHand(caster, List.of(new DarkBanishing()));
        harness.addMana(caster, ManaColor.BLACK, 3);
        harness.castInstant(caster, 0, harness.getPermanentId(target, "Grizzly Bears"));
    }
}
