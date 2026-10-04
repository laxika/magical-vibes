package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AltarsReap;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EtheriumHornSorcerer.class, GrizzlyBears.class, Island.class, Mountain.class, AltarsReap.class})
class EtheriumHornSorcererTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability returns Etherium-Horn Sorcerer to its owner's hand")
    void activatedAbilityReturnsToHand() {
        harness.addToBattlefield(player1, new EtheriumHornSorcerer());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Etherium-Horn Sorcerer");
        harness.assertNotOnBattlefield(player1, "Etherium-Horn Sorcerer");
    }

    @Test
    @DisplayName("Cascade stops at the first lesser-cost nonland card")
    void cascadeOffersFirstLesserNonlandCard() {
        harness.setLibrary(player1, List.of(new Mountain(), new GrizzlyBears(), new Island()));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new EtheriumHornSorcerer(), "{4}{U}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<String> castable = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
        assertThat(castable).containsExactly("Grizzly Bears");

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Grizzly Bears")
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
    }

    @Test
    void cascadeMayBeDeclinedAndReturnsAllExiledCardsToBottom() {
        Mountain skipped = new Mountain();
        GrizzlyBears hit = new GrizzlyBears();
        Island remaining = new Island();
        harness.setLibrary(player1, List.of(skipped, hit, remaining));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new EtheriumHornSorcerer(), "{4}{U}{R}");
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(remaining);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(skipped, hit);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(hit.getId()));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Etherium-Horn Sorcerer");
    }

    @Test
    void cascadeSkipsEqualManaValueAndResolvesFreeCreatureBeforeSorcerer() {
        EtheriumHornSorcerer equalCost = new EtheriumHornSorcerer();
        GrizzlyBears hit = new GrizzlyBears();
        Island remaining = new Island();
        harness.setLibrary(player1, List.of(equalCost, hit, remaining));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new EtheriumHornSorcerer(), "{4}{U}{R}");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining, equalCost);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Etherium-Horn Sorcerer");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Etherium-Horn Sorcerer");
    }

    @Test
    void cascadeWithNoQualifyingCardReturnsWholeLibrary() {
        EtheriumHornSorcerer equalCost = new EtheriumHornSorcerer();
        Mountain land = new Mountain();
        harness.setLibrary(player1, List.of(land, equalCost));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new EtheriumHornSorcerer(), "{4}{U}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, equalCost);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Etherium-Horn Sorcerer");
    }

    @Test
    void cascadeWithEmptyLibraryStillAllowsSorcererToResolve() {
        harness.setLibrary(player1, List.of());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new EtheriumHornSorcerer(), "{4}{U}{R}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Etherium-Horn Sorcerer");
    }

    @Test
    void tappedSorcererControlledByOpponentReturnsToOwner() {
        EtheriumHornSorcerer sorcerer = new EtheriumHornSorcerer();
        sorcerer.setOwnerId(player1.getId());
        var permanent = harness.addToBattlefieldAndReturn(player2, sorcerer);
        permanent.tap();
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Etherium-Horn Sorcerer");
        harness.assertNotInHand(player2, "Etherium-Horn Sorcerer");
        harness.assertNotOnBattlefield(player2, "Etherium-Horn Sorcerer");
    }

    @Test
    @CardUsed({EtheriumHornSorcerer.class, AltarsReap.class, Island.class})
    void cascadeCannotCastAltarsReapWithoutSacrificingCreature() {
        var sacrifice = harness.addToBattlefieldAndReturn(player1, new EtheriumHornSorcerer());
        AltarsReap reap = new AltarsReap();
        harness.setLibrary(player1, List.of(reap, new Island(), new Island()));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new EtheriumHornSorcerer(), "{4}{U}{R}");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        boolean reapOnStack = gd.stack.stream()
                .anyMatch(entry -> entry.getCard().getId().equals(reap.getId()));
        boolean sacrificeStillOnBattlefield = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(permanent -> permanent.getId().equals(sacrifice.getId()));
        assertThat(reapOnStack && sacrificeStillOnBattlefield).isFalse();
    }
}
