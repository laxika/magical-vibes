package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.n.NayaSojourners;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PaintersServant;
import com.github.laxika.magicalvibes.cards.s.SelesnyaGuildmage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EvolvingDoor.class, GrizzlyBears.class, NayaSojourners.class, Ornithopter.class,
        SelesnyaGuildmage.class, DryadArbor.class, PaintersServant.class})
class EvolvingDoorTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a creature with exactly one more color than the sacrificed creature")
    void searchesForOneMoreColor() {
        activateDoor(new SelesnyaGuildmage(), new NayaSojourners(), new GrizzlyBears(),
                new SelesnyaGuildmage());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards())
                .extracting(Card::getName)
                .containsExactly("Naya Sojourners");
    }

    @Test
    @DisplayName("Offers the found creature for a normal-cost cast")
    void offersNormalCostCast() {
        GrizzlyBears bears = new GrizzlyBears();
        activateDoor(new Ornithopter(), bears);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void decliningCastLeavesCreatureExiledWithoutLaterPermission() {
        GrizzlyBears bears = new GrizzlyBears();
        activateDoor(new Ornithopter(), bears);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(bears);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void noEligibleCreatureStillPaysSacrificeCost() {
        activateDoor(new GrizzlyBears(), new GrizzlyBears(), new Ornithopter());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Evolving Door").isTapped()).isTrue();
    }

    @Test
    void mayFailToFindAnEligibleCreature() {
        GrizzlyBears bears = new GrizzlyBears();
        activateDoor(new Ornithopter(), bears);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotPlayAnExiledLandCreature() {
        DryadArbor arbor = new DryadArbor();
        activateDoor(new Ornithopter(), arbor);
        harness.handleCardChosen(player1, 0);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(arbor);
        harness.assertNotOnBattlefield(player1, "Dryad Arbor");
    }

    @Test
    void countsColorsGrantedByStaticAbilitiesBeforeSacrifice() {
        Permanent painter = new Permanent(new PaintersServant());
        painter.setChosenColor(CardColor.BLUE);
        gd.playerBattlefields.get(player2.getId()).add(painter);
        activateDoor(new SelesnyaGuildmage(), new NayaSojourners(), new GrizzlyBears());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName).containsExactly("Naya Sojourners");
    }

    @Test
    void cannotCastFoundCreatureWithoutPayingItsManaCost() {
        GrizzlyBears bears = new GrizzlyBears();
        activateDoor(new Ornithopter(), bears);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(bears);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotActivateDuringCombat() {
        harness.addToBattlefield(player1, new EvolvingDoor());
        addCreatureReady(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(findPermanent(player1, "Evolving Door").isTapped()).isFalse();
    }

    private void activateDoor(Card sacrificed, Card... library) {
        harness.addToBattlefield(player1, new EvolvingDoor());
        addCreatureReady(player1, sacrificed);
        harness.setLibrary(player1, List.of(library));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
