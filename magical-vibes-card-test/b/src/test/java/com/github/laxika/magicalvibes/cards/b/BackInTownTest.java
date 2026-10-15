package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ArcaneAdaptation;
import com.github.laxika.magicalvibes.cards.d.DreadWarlock;
import com.github.laxika.magicalvibes.cards.c.ChangelingOutcast;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImpulsivePilferer;
import com.github.laxika.magicalvibes.cards.n.NighthawkScavenger;
import com.github.laxika.magicalvibes.cards.w.WitchOfTheMoors;
import com.github.laxika.magicalvibes.cards.y.YoungPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BackInTown.class, DreadWarlock.class, GrizzlyBears.class, ChangelingOutcast.class,
        ImpulsivePilferer.class, NighthawkScavenger.class, WitchOfTheMoors.class, YoungPyromancer.class,
        ArcaneAdaptation.class})
class BackInTownTest extends BaseCardTest {

    @Test
    @DisplayName("Chooses exactly X outlaw creature cards from your graveyard")
    void choosesEligibleTargets() {
        Card warlock = new DreadWarlock();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(warlock, bears));
        harness.setHand(player1, List.of(new BackInTown()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactly(warlock.getId());
    }

    @Test
    @DisplayName("Returns the selected outlaw creature cards to the battlefield")
    void returnsSelectedOutlaws() {
        Card warlock = new DreadWarlock();
        harness.setGraveyard(player1, List.of(warlock));
        harness.setHand(player1, List.of(new BackInTown()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 1);
        harness.handleMultipleCardsChosen(player1, List.of(warlock.getId()));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Dread Warlock")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Back in Town");
    }

    @Test
    @DisplayName("X greater than the eligible outlaw count is illegal")
    void xGreaterThanEligibleCountThrows() {
        harness.setGraveyard(player1, List.of(new DreadWarlock(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new BackInTown()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough matching creature cards in graveyard");
    }

    @Test
    @DisplayName("X=0 resolves without returning cards")
    void xZeroReturnsNothing() {
        Card warlock = new DreadWarlock();
        harness.setGraveyard(player1, List.of(warlock));
        harness.setHand(player1, List.of(new BackInTown()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Dread Warlock", "Back in Town");
    }

    @Test
    @DisplayName("Returns multiple outlaw types and changelings, excluding non-outlaws and opposing cards")
    void returnsMultipleOutlawTypes() {
        Card pirate = new ImpulsivePilferer();
        Card rogue = new NighthawkScavenger();
        Card warlock = new WitchOfTheMoors();
        Card changeling = new ChangelingOutcast();
        Card shaman = new YoungPyromancer();
        Card opposingPirate = new ImpulsivePilferer();
        harness.setGraveyard(player1, List.of(pirate, rogue, warlock, changeling, shaman));
        harness.setGraveyard(player2, List.of(opposingPirate));
        harness.setHand(player1, List.of(new BackInTown()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, 4);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                pirate.getId(), rogue.getId(), warlock.getId(), changeling.getId());
        harness.handleMultipleCardsChosen(player1,
                List.of(pirate.getId(), rogue.getId(), warlock.getId(), changeling.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(pirate.getId(), rogue.getId(), warlock.getId(), changeling.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(permanent -> !permanent.isTapped());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shaman);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingPirate);
    }

    @Test
    @DisplayName("Cannot choose fewer than X targets or choose the same card twice")
    void requiresExactlyXDistinctTargets() {
        Card pirate = new ImpulsivePilferer();
        Card rogue = new NighthawkScavenger();
        harness.setGraveyard(player1, List.of(pirate, rogue));
        harness.setHand(player1, List.of(new BackInTown()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, 2);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(pirate.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(pirate.getId(), pirate.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(pirate.getId(), rogue.getId()));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Impulsive Pilferer");
        harness.assertOnBattlefield(player1, "Nighthawk Scavenger");
    }

    @Test
    @DisplayName("Still returns a legal target when another target leaves the graveyard")
    void returnsRemainingLegalTarget() {
        Card pirate = new ImpulsivePilferer();
        Card rogue = new NighthawkScavenger();
        harness.setGraveyard(player1, List.of(pirate, rogue));
        harness.setHand(player1, List.of(new BackInTown()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, 2);
        harness.handleMultipleCardsChosen(player1, List.of(pirate.getId(), rogue.getId()));

        harness.setGraveyard(player1, List.of(rogue));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nighthawk Scavenger");
        harness.assertNotOnBattlefield(player1, "Impulsive Pilferer");
        harness.assertInGraveyard(player1, "Back in Town");
    }

    @Test
    @DisplayName("X=0 can be cast with an empty graveyard")
    void xZeroWithEmptyGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new BackInTown()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Back in Town");
    }

    @Test
    @CardUsed(ArcaneAdaptation.class)
    @DisplayName("Can target a creature that gains an outlaw type in the graveyard")
    void targetsGrantedOutlawType() {
        harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation())
                .setChosenSubtype(CardSubtype.ASSASSIN);
        Card shaman = new YoungPyromancer();
        harness.setGraveyard(player1, List.of(shaman));
        harness.setHand(player1, List.of(new BackInTown()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 1);
        harness.handleMultipleCardsChosen(player1, List.of(shaman.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Young Pyromancer");
        harness.assertNotInGraveyard(player1, "Young Pyromancer");
    }
}
