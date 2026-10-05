package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Parcelbeast.class, Forest.class, GrizzlyBears.class})
class ParcelbeastTest extends BaseCardTest {

    @Test
    void putsLandOntoBattlefieldWhenMayChoiceIsAccepted() {
        addCreatureReady(player1, new Parcelbeast());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
    }

    @Test
    void putsLandIntoHandWhenMayChoiceIsDeclined() {
        addCreatureReady(player1, new Parcelbeast());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Forest")).isEmpty();
    }

    @Test
    void putsNonlandTopCardIntoHand() {
        addCreatureReady(player1, new Parcelbeast());
        GrizzlyBears grizzlyBears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(grizzlyBears));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(grizzlyBears);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryDoesNotDrawOrOfferAChoice() {
        Permanent parcelbeast = addCreatureReady(player1, new Parcelbeast());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        List<Card> handBefore = List.copyOf(gd.playerHands.get(player1.getId()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(handBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(parcelbeast.isTapped()).isTrue();
    }

    @Test
    void puttingLandOntoBattlefieldDoesNotUseAnotherLandPlay() {
        addCreatureReady(player1, new Parcelbeast());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        gd.landsPlayedThisTurn.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Forest").getCard()).isSameAs(forest);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void canCastForMutateCostTargetingOwnedNonHuman() {
        Permanent target = addCreatureReady(player1, new Parcelbeast());
        harness.setHand(player1, List.of(new Parcelbeast()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castWithAlternateCost(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}

