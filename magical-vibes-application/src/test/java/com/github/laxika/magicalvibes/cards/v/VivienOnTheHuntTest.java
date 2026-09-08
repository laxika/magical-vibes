package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VivienOnTheHunt.class, BenalishKnight.class, Forest.class, GrizzlyBears.class,
        HillGiant.class, LlanowarElves.class, Shock.class})
class VivienOnTheHuntTest extends BaseCardTest {

    @Test
    @DisplayName("+2 can sacrifice a creature and searches for exactly one higher mana value")
    void plusTwoSacrificesAndSearchesByManaValue() {
        Permanent vivien = addReadyVivien(3);
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card tooSmall = new LlanowarElves();
        Card matching = new BenalishKnight();
        Card tooLarge = new HillGiant();
        harness.setLibrary(player1, List.of(tooSmall, matching, tooLarge));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrificed.getId());

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(matching);

        harness.handleCardChosen(player1, 0);

        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Benalish Knight");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(tooSmall, tooLarge);
    }

    @Test
    @DisplayName("+2 may be declined without sacrificing a creature")
    void plusTwoMayBeDeclined() {
        addReadyVivien(3);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("+1 lets the controller choose any number of milled creatures for their hand")
    void plusOneChoosesAnyNumberOfMilledCreatures() {
        addReadyVivien(3);
        Card creatureOne = new GrizzlyBears();
        Card creatureTwo = new BenalishKnight();
        Card creatureThree = new HillGiant();
        Card instant = new Shock();
        Card land = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(creatureOne, creatureTwo, creatureThree, instant, land));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                creatureOne.getId(), creatureTwo.getId(), creatureThree.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creatureOne.getId(), creatureThree.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creatureOne, creatureThree);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creatureTwo, instant, land);
    }

    @Test
    @DisplayName("+1 may put zero of the milled creatures into the controller's hand")
    void plusOneMayChooseZeroCreatures() {
        addReadyVivien(3);
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(creature, new Shock(), new Shock(), new Shock(), new Shock()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("-1 creates a 4/4 green Rhino Warrior token")
    void minusOneCreatesRhinoWarrior() {
        addReadyVivien(3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent rhino = findPermanent(player1, "Rhino");
        assertThat(rhino.getCard().isToken()).isTrue();
        assertThat(rhino.getCard().getPower()).isEqualTo(4);
        assertThat(rhino.getCard().getToughness()).isEqualTo(4);
        assertThat(rhino.getCard().getSubtypes()).containsExactly(CardSubtype.RHINO, CardSubtype.WARRIOR);
    }

    private Permanent addReadyVivien(int loyalty) {
        Permanent vivien = harness.addToBattlefieldAndReturn(player1, new VivienOnTheHunt());
        vivien.setCounterCount(CounterType.LOYALTY, loyalty);
        vivien.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return vivien;
    }
}
