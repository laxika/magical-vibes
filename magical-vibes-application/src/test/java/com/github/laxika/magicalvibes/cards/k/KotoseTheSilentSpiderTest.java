package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RoguesPassage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KotoseTheSilentSpider.class, GrizzlyBears.class, Plains.class, RoguesPassage.class})
class KotoseTheSilentSpiderTest extends BaseCardTest {

    @Test
    void exilesSelectedCardsWithTheSameNameAndTracksThemToKotose() {
        Card target = new GrizzlyBears();
        Card handCopy = new GrizzlyBears();
        Card libraryCopy = new GrizzlyBears();

        Permanent kotose = resolveKotose(target, List.of(handCopy), List.of(libraryCopy));

        assertThat(gd.getCardsExiledByPermanent(kotose.getId()))
                .containsExactlyInAnyOrder(target, handCopy, libraryCopy);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void allowsPlayingExactlyOneExiledCard() {
        Card target = new GrizzlyBears();
        Card secondCopy = new GrizzlyBears();
        Permanent kotose = resolveKotose(target, List.of(secondCopy), List.of());

        prepareMainPhase();
        harness.addMana(player1, ManaColor.WHITE, 2);
        gs.playCardFromExile(gd, player1, target.getId(), null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(kotose.getId())).contains(secondCopy);

        prepareMainPhase();
        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1, secondCopy.getId(), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No permission to play this exiled card");
    }

    @Test
    void canPlayOneNonbasicLandFromExile() {
        Card target = new RoguesPassage();
        Card secondCopy = new RoguesPassage();
        resolveKotose(target, List.of(secondCopy), List.of());

        prepareMainPhase();
        gs.playCardFromExile(gd, player1, target.getId(), null, null);

        harness.assertOnBattlefield(player1, "Rogue's Passage");

        prepareMainPhase();
        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1, secondCopy.getId(), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No permission to play this exiled card");
    }

    @Test
    void doesNotOfferBasicLandCardsAsTargets() {
        Card plains = new Plains();
        harness.setGraveyard(player2, new ArrayList<>(List.of(plains)));
        harness.setHand(player1, List.of(new KotoseTheSilentSpider()));
        addKotoseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(plains);
    }

    private Permanent resolveKotose(Card target, List<Card> handCopies, List<Card> libraryCopies) {
        harness.setGraveyard(player2, new ArrayList<>(List.of(target)));
        harness.setHand(player2, new ArrayList<>(handCopies));
        harness.setLibrary(player2, libraryCopies);
        harness.setHand(player1, List.of(new KotoseTheSilentSpider()));
        addKotoseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        List<Card> selected = new ArrayList<>();
        selected.add(target);
        selected.addAll(handCopies);
        selected.addAll(libraryCopies);
        harness.handleMultipleCardsChosen(player1, selected.stream().map(Card::getId).toList());
        harness.passBothPriorities();
        return findPermanent(player1, "Kotose, the Silent Spider");
    }

    private void addKotoseMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
