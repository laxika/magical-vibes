package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HaakonStromgaldScourge;
import com.github.laxika.magicalvibes.cards.k.KalonianBehemoth;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
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
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({DjinnOfWishes.class, GrizzlyBears.class, Pyroclasm.class, Shock.class, Forest.class,
        DemonOfCatastrophes.class, HaakonStromgaldScourge.class, KalonianBehemoth.class})
class DjinnOfWishesTest extends BaseCardTest {

    private void addDjinnMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addDjinnToBattlefield() {
        harness.addToBattlefieldAndReturn(player1, new DjinnOfWishes()).setCounterCount(CounterType.WISH, 3);
    }

    // ===== Enters with wish counters =====

    @Test
    @DisplayName("Enters the battlefield with 3 wish counters when cast")
    void entersWithThreeWishCounters() {
        harness.setHand(player1, List.of(new DjinnOfWishes()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        var permanents = gd.playerBattlefields.get(player1.getId());
        assertThat(permanents).hasSize(1);
        assertThat(permanents.getFirst().getCounterCount(CounterType.WISH)).isEqualTo(3);
    }

    // ===== Activate ability — non-targeted spell =====

    @Test
    @DisplayName("Activating ability reveals top card and can cast non-targeted sorcery for free")
    void activateAbilityCastsNonTargetedSorceryForFree() {
        addDjinnToBattlefield();
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2
        Card pyroclasm = new Pyroclasm();
        gd.playerDecks.get(player1.getId()).addFirst(pyroclasm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addDjinnMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the ability

        // May prompt: play Pyroclasm?
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        // Pyroclasm is on the stack
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getLast().getCard()).isSameAs(pyroclasm);

        // Resolve Pyroclasm
        harness.passBothPriorities();

        // Grizzly Bears (2/2) should be dead
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        // Wish counter was removed
        var djinn = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(djinn.getCounterCount(CounterType.WISH)).isEqualTo(2);
    }

    // ===== Activate ability — targeted instant =====

    @Test
    @DisplayName("Activating ability with targeted instant prompts for target")
    void activateAbilityWithTargetedInstant() {
        addDjinnToBattlefield();
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2
        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(shock);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addDjinnMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the ability

        // May prompt: play Shock?
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        // Target prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        // Shock is on the stack
        assertThat(gd.stack).isNotEmpty();

        // Resolve Shock
        harness.passBothPriorities();

        // Grizzly Bears should be dead
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    // ===== Decline to play — card is exiled =====

    @Test
    @DisplayName("Declining to play exiles the top card")
    void decliningToPlayExilesCard() {
        addDjinnToBattlefield();
        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(shock);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addDjinnMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the ability

        // May prompt: play Shock?
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false); // decline

        // Card is exiled
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
        // Card removed from library
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(shock);
    }

    // ===== Play a land from top =====

    @Test
    @DisplayName("Can play a land from top of library if no land played this turn")
    void canPlayLandFromTop() {
        addDjinnToBattlefield();
        Card forest = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(forest);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addDjinnMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the ability

        // May prompt: play Forest?
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        // Forest is on the battlefield
        harness.assertOnBattlefield(player1, "Forest");
        // Removed from library
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
        // Land play was consumed
        assertThat(gd.landsPlayedThisTurn.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    // ===== Land already played — land gets exiled =====

    @Test
    @DisplayName("Land is exiled if player already played a land this turn")
    void landExiledIfAlreadyPlayedLand() {
        addDjinnToBattlefield();
        Card forest = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(forest);

        // Simulate having already played a land
        gd.landsPlayedThisTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addDjinnMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the ability

        // No may prompt — land is exiled directly
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
        // Removed from library
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
    }

    // ===== Empty library =====

    @Test
    @DisplayName("Empty library — ability resolves without error")
    void emptyLibraryDoesNothing() {
        addDjinnToBattlefield();
        gd.playerDecks.get(player1.getId()).clear();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addDjinnMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the ability

        // No error, game continues
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    // ===== Cannot activate without wish counters =====

    @Test
    @DisplayName("Cannot activate ability without wish counters")
    void cannotActivateWithoutWishCounters() {
        addDjinnToBattlefield();
        // Remove all wish counters
        gd.playerBattlefields.get(player1.getId()).getFirst().setCounterCount(CounterType.WISH, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addDjinnMana();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Cast creature from top =====

    @Test
    @DisplayName("Can cast a creature from top of library without paying mana cost")
    void canCastCreatureFromTop() {
        addDjinnToBattlefield();
        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addDjinnMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the ability

        // May prompt: play Grizzly Bears?
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        // Grizzly Bears is on the stack
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getLast().getCard()).isSameAs(bears);

        // Resolve the creature spell
        harness.passBothPriorities();

        // Grizzly Bears is on the battlefield
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        // Removed from library
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    void landIsExiledOnOpponentsTurn() {
        addDjinnToBattlefield();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addDjinnMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(forest);
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.landsPlayedThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void canCastSorceryDuringOpponentsTurn() {
        addDjinnToBattlefield();
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card pyroclasm = new Pyroclasm();
        harness.setLibrary(player1, List.of(pyroclasm));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addDjinnMana();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getCounterCount(CounterType.WISH))
                .isEqualTo(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.stack.getLast().getCard()).isSameAs(pyroclasm);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void mandatorySacrificeMustBePaidBeforeCastingRevealedCreature() {
        addDjinnToBattlefield();
        Card demon = new DemonOfCatastrophes();
        harness.setLibrary(player1, List.of(demon));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addDjinnMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == demon);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Djinn of Wishes"));
        harness.assertNotOnBattlefield(player1, "Djinn of Wishes");
        assertThat(gd.stack.getLast().getCard()).isSameAs(demon);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Demon of Catastrophes");
    }

    @Test
    void cardThatCannotBeCastFromLibraryIsExiled() {
        addDjinnToBattlefield();
        Card haakon = new HaakonStromgaldScourge();
        harness.setLibrary(player1, List.of(haakon));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addDjinnMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(haakon);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(haakon);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == haakon);
    }

    @Test
    void revealedSpellCannotTargetCreatureWithShroud() {
        addDjinnToBattlefield();
        UUID behemothId = harness.addToBattlefieldAndReturn(player2, new KalonianBehemoth()).getId();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addDjinnMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        var choice = (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        List<UUID> offeredTargets = List.copyOf(choice.validPermanentIds());
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(offeredTargets).doesNotContain(behemothId);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Kalonian Behemoth");
    }
}
