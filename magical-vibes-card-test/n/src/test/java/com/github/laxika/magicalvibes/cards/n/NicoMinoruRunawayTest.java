package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BonecrusherGiant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Stomp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NicoMinoruRunaway.class, Forest.class, GrizzlyBears.class, Pyroclasm.class, Shock.class, BonecrusherGiant.class, Stomp.class})
class NicoMinoruRunawayTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell from hand does not trigger Nico")
    void castingFromHandDoesNotTrigger() {
        harness.addToBattlefield(player1, new NicoMinoruRunaway());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The activated ability discards, exiles until a nonland, and offers it for free")
    void activatedAbilityCastsExiledNonlandForFree() {
        forceMainPhase();
        Permanent nico = harness.addToBattlefieldAndReturn(player1, new NicoMinoruRunaway());
        nico.setSummoningSick(false);

        GrizzlyBears discarded = new GrizzlyBears();
        Forest land = new Forest();
        Pyroclasm spell = new Pyroclasm();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(land, spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land, spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Pyroclasm");
    }

    @Test
    @DisplayName("The activated ability cannot be paid without a discard")
    void activatedAbilityRequiresDiscard() {
        forceMainPhase();
        Permanent nico = harness.addToBattlefieldAndReturn(player1, new NicoMinoruRunaway());
        nico.setSummoningSick(false);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 3);

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Declining the free cast leaves every revealed card in exile")
    void decliningFreeCastLeavesCardsExiled() {
        forceMainPhase();
        Permanent nico = harness.addToBattlefieldAndReturn(player1, new NicoMinoruRunaway());
        nico.setSummoningSick(false);
        Forest land = new Forest();
        GrizzlyBears spell = new GrizzlyBears();
        Shock remaining = new Shock();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(land, spell, remaining));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land, spell);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player2, 20);
        assertThat(nico.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A library containing only lands is entirely exiled without a cast")
    void allLandLibraryIsExiled() {
        forceMainPhase();
        Permanent nico = harness.addToBattlefieldAndReturn(player1, new NicoMinoruRunaway());
        nico.setSummoningSick(false);
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An empty library still permits activation and payment of the costs")
    void emptyLibraryDoesNotPreventActivation() {
        forceMainPhase();
        Permanent nico = harness.addToBattlefieldAndReturn(player1, new NicoMinoruRunaway());
        nico.setSummoningSick(false);
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        assertThat(nico.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent casting from exile triggers only their own Nico")
    void opponentCastingFromExileDoesNotTriggerOurNico() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new NicoMinoruRunaway());
        Permanent opposingNico = harness.addToBattlefieldAndReturn(player2, new NicoMinoruRunaway());
        opposingNico.setSummoningSick(false);
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.activateAbility(player2, 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The free cast offers both the creature and Adventure spell")
    void freeCastOffersAdventureChoice() {
        forceMainPhase();
        Permanent nico = harness.addToBattlefieldAndReturn(player1, new NicoMinoruRunaway());
        nico.setSummoningSick(false);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new BonecrusherGiant()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains("Cast Bonecrusher Giant", "Cast Stomp");
    }

    private void forceMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
