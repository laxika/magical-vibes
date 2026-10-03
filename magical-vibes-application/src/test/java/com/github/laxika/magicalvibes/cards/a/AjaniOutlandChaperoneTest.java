package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({AjaniOutlandChaperone.class, GrizzlyBears.class, MindStone.class, Plains.class,
        SerraAngel.class, Shock.class, SoulWarden.class})
class AjaniOutlandChaperoneTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts planeswalker spell on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new AjaniOutlandChaperone()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castPlaneswalker(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.PLANESWALKER_SPELL);
        assertThat(entry.getCard()).isInstanceOf(AjaniOutlandChaperone.class);
    }

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with initial loyalty")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new AjaniOutlandChaperone()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Ajani, Outland Chaperone"));
        Permanent ajani = bf.stream().filter(p -> p.getCard().getName().equals("Ajani, Outland Chaperone")).findFirst().orElseThrow();
        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(ajani.isSummoningSick()).isFalse();
    }

    @Test
    @DisplayName("+1 ability creates a 1/1 Kithkin token and increases loyalty")
    void plusOneCreatesTokenAndIncreasesLoyalty() {
        Permanent ajani = addReadyAjani(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Kithkin")
                && p.getCard().getPower() == 1
                && p.getCard().getToughness() == 1);
        Permanent token = bf.stream().filter(p -> p.getCard().getName().equals("Kithkin"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectiveColors(gd, token)).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.KITHKIN);
    }

    @Test
    @DisplayName("-2 ability deals 4 damage to tapped creature and decreases loyalty")
    void minusTwoDealsDamageAndDecreasesLoyalty() {
        Permanent ajani = addReadyAjani(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        // Grizzly Bears is 2/2, 4 damage kills it
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("-2 ability cannot target untapped creature")
    void minusTwoCannotTargetUntappedCreature() {
        addReadyAjani(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
    }

    @Test
    @DisplayName("Cannot activate loyalty ability during opponent's turn")
    void cannotActivateOnOpponentsTurn() {
        addReadyAjani(player1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");
    }

    @Test
    @DisplayName("Cannot activate loyalty ability during combat")
    void cannotActivateDuringCombat() {
        addReadyAjani(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("Cannot activate loyalty ability when stack is not empty")
    void cannotActivateWithNonEmptyStack() {
        addReadyAjani(player1);
        // Put something on the stack
        harness.getGameData().stack.add(new StackEntry(
                StackEntryType.CREATURE_SPELL,
                new GrizzlyBears(),
                player2.getId(),
                "Grizzly Bears",
                List.of()
        ));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
    }

    @Test
    @DisplayName("Cannot activate two loyalty abilities on same planeswalker in one turn")
    void cannotActivateTwicePerTurn() {
        addReadyAjani(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one loyalty ability");
    }

    @Test
    @DisplayName("Cannot use -2 when loyalty is only 1")
    void cannotActivateNegativeCostWithInsufficientLoyalty() {
        Permanent ajani = addReadyAjani(player1);
        ajani.setCounterCount(CounterType.LOYALTY, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("Planeswalker dies when loyalty reaches 0 from ability activation")
    void diesWhenLoyaltyReachesZero() {
        Permanent ajani = addReadyAjani(player1);
        ajani.setCounterCount(CounterType.LOYALTY, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();

        // -2 ability: 2 - 2 = 0, Ajani dies to state-based actions after ability goes on stack
        harness.activateAbility(player1, 0, 1, null, target.getId());

        GameData gd = harness.getGameData();
        // Ajani should be gone from battlefield
        harness.assertNotOnBattlefield(player1, "Ajani, Outland Chaperone");
        // Ajani goes to graveyard
        harness.assertInGraveyard(player1, "Ajani, Outland Chaperone");
        // The ability is still on the stack though
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Ability still resolves after planeswalker dies to SBA at 0 loyalty")
    void abilityResolvesAfterPlaneswalkerDiesToSBA() {
        Permanent ajani = addReadyAjani(player1);
        ajani.setCounterCount(CounterType.LOYALTY, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();

        // -2 ability: loyalty 2 - 2 = 0, Ajani dies to SBA, ability stays on stack
        harness.activateAbility(player1, 0, 1, null, target.getId());

        // Ajani is already gone but the ability is on the stack
        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Ajani, Outland Chaperone");

        // Now resolve the ability â€” it should still deal 4 damage and kill Grizzly Bears
        harness.passBothPriorities();

        gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Legend rule applies to planeswalkers")
    void legendRuleApplies() {
        harness.setHand(player1, List.of(new AjaniOutlandChaperone()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        // Cast a second one
        harness.setHand(player1, List.of(new AjaniOutlandChaperone()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        // Legend rule: player should be prompted to choose which to keep
        // or one should be automatically removed. Check that there's only one Ajani on battlefield
        // (the exact handling depends on the legend rule implementation)
        GameData gd = harness.getGameData();
        long ajaniCount = countPermanents(player1, "Ajani, Outland Chaperone");
        // Either legend rule triggers a choice (interaction.isAwaitingInput()) or only one remains
        assertThat(ajaniCount <= 1 || gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    void minusTwoDoesNotDamageCreatureThatUntapsBeforeResolution() {
        Permanent ajani = addReadyAjani(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        target.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void ultimateSelectsOnlyCheapNonlandPermanentsAndReturnsTheRestToLibrary() {
        Permanent ajani = addReadyAjani(player1);
        ajani.setCounterCount(CounterType.LOYALTY, 8);
        GrizzlyBears bears = new GrizzlyBears();
        MindStone stone = new MindStone();
        Plains land = new Plains();
        Shock instant = new Shock();
        SerraAngel expensive = new SerraAngel();
        AjaniOutlandChaperone otherAjani = new AjaniOutlandChaperone();
        harness.setLibrary(player1, List.of(bears, stone, land, instant, expensive, otherAjani));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(bears.getId(), stone.getId(), otherAjani.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), stone.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Mind Stone");
        harness.assertNotOnBattlefield(player1, "Ajani, Outland Chaperone");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, instant, expensive, otherAjani);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void ultimateCanDeclineAllCardsAndUsesLifeTotalAtResolution() {
        Permanent ajani = addReadyAjani(player1);
        ajani.setCounterCount(CounterType.LOYALTY, 9);
        GrizzlyBears first = new GrizzlyBears();
        MindStone second = new MindStone();
        GrizzlyBears below = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, below));
        harness.setLife(player1, 3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.setLife(player1, 2);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below);
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second, below);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Mind Stone");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void ultimateWithNoEligibleCardsKeepsAllCardsInLibrary() {
        Permanent ajani = addReadyAjani(player1);
        ajani.setCounterCount(CounterType.LOYALTY, 9);
        Plains land = new Plains();
        Shock instant = new Shock();
        SerraAngel expensive = new SerraAngel();
        harness.setLibrary(player1, List.of(land, instant, expensive));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, instant, expensive);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void ultimateWithEmptyLibraryFinishesWithoutAChoice() {
        Permanent ajani = addReadyAjani(player1);
        ajani.setCounterCount(CounterType.LOYALTY, 9);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ultimateEnteringCreaturesSeeEachOthersEntryRegardlessOfLibraryOrder() {
        Permanent ajani = addReadyAjani(player1);
        ajani.setCounterCount(CounterType.LOYALTY, 9);
        GrizzlyBears bears = new GrizzlyBears();
        SoulWarden warden = new SoulWarden();
        harness.setLibrary(player1, List.of(bears, warden));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), warden.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Soul Warden");
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    private Permanent addReadyAjani(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AjaniOutlandChaperone());
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}

