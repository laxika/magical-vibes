package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KamiOfTheHunt;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoonringMirror.class, Forest.class, KamiOfTheHunt.class, SakuraTribeElder.class})
class MoonringMirrorTest extends BaseCardTest {

    private UUID addMirror() {
        harness.addToBattlefield(player1, new MoonringMirror());
        return harness.getPermanentId(player1, "Moonring Mirror");
    }

    // Drawing finishes before the triggered ability exiles the next card.
    private void drawAndResolveTrigger(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    @Test
    @DisplayName("Drawing a card exiles the top card of the controller's library face down with the mirror")
    void drawExilesTopCardFaceDown() {
        UUID permId = addMirror();
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        drawAndResolveTrigger(player1);

        // One card drawn into hand, one exiled face down with the mirror.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(1);
        assertThat(gd.exiledCards).filteredOn(e -> permId.equals(e.sourcePermanentId()))
                .allMatch(ExiledCardEntry::faceDown);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The opponent drawing does not trigger the mirror")
    void opponentDrawDoesNotTrigger() {
        UUID permId = addMirror();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(permId)).isEmpty();
    }

    @Test
    @DisplayName("Accepting the upkeep trigger swaps the hand with the cards exiled with the mirror")
    void upkeepSwapsHandWithExiledCards() {
        UUID permId = addMirror();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        drawAndResolveTrigger(player1);
        drawAndResolveTrigger(player1);
        List<UUID> exiledBefore = gd.getCardsExiledByPermanent(permId).stream().map(Card::getId).toList();
        assertThat(exiledBefore).hasSize(2);

        harness.setHand(player1, new ArrayList<>(List.of(new KamiOfTheHunt(), new SakuraTribeElder())));

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // Resolve the upkeep trigger to its optional choice.
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(2)
                .allMatch(c -> exiledBefore.contains(c.getId()));
        assertThat(gd.getCardsExiledByPermanent(permId))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Kami of the Hunt", "Sakura-Tribe Elder");
    }

    @Test
    @DisplayName("Declining the upkeep trigger leaves hand and exiled cards untouched")
    void upkeepDeclineKeepsEverythingInPlace() {
        UUID permId = addMirror();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        drawAndResolveTrigger(player1);

        harness.setHand(player1, new ArrayList<>(List.of(new KamiOfTheHunt())));

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement().matches(c -> c.getName().equals("Kami of the Hunt"));
        assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(1);
    }

    @Test
    @DisplayName("With an empty hand the upkeep trigger still returns the exiled cards")
    void upkeepWithEmptyHandStillReturnsExiledCards() {
        UUID permId = addMirror();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        drawAndResolveTrigger(player1);

        harness.setHand(player1, new ArrayList<>());

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getCardsExiledByPermanent(permId)).isEmpty();
    }

    @Test
    @DisplayName("The upkeep trigger returns only cards owned by the controller")
    void upkeepReturnsOnlyCardsOwnedByController() {
        UUID permId = addMirror();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        drawAndResolveTrigger(player1);
        assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(1);
        Card opponentOwned = new SakuraTribeElder();
        gd.addToExile(player2.getId(), opponentOwned, permId);

        Card handCard = new KamiOfTheHunt();
        harness.setHand(player1, new ArrayList<>(List.of(handCard)));

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement().satisfies(card -> assertThat(card.getId())
                        .isNotEqualTo(opponentOwned.getId()));
        assertThat(gd.getCardsExiledByPermanent(permId))
                .extracting(Card::getId)
                .contains(opponentOwned.getId(), handCard.getId());
    }
    @Test
    @DisplayName("Drawing the last library card leaves nothing for the exile trigger")
    void drawingLastCardDoesNotExileAnything() {
        UUID mirrorId = addMirror();
        Card lastCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(lastCard));

        drawAndResolveTrigger(player1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lastCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(mirrorId)).isEmpty();
    }

    @Test
    @DisplayName("Two mirrors exile separate cards after the same draw")
    void multipleMirrorsTrackSeparateExiledCards() {
        UUID firstId = addMirror();
        harness.addToBattlefield(player1, new MoonringMirror());
        UUID secondId = findPermanents(player1, "Moonring Mirror").get(1).getId();
        Card drawn = new Forest();
        Card firstExiled = new KamiOfTheHunt();
        Card secondExiled = new SakuraTribeElder();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn, firstExiled, secondExiled));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.exiledCards).isEmpty();
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.getCardsExiledByPermanent(firstId)).containsExactly(secondExiled);
        assertThat(gd.getCardsExiledByPermanent(secondId)).containsExactly(firstExiled);
        assertThat(gd.exiledCards).allMatch(ExiledCardEntry::faceDown);
    }

    @Test
    @DisplayName("Accepting with no previously exiled cards exiles the whole hand face down")
    void emptyExiledPileStillExilesHand() {
        UUID mirrorId = addMirror();
        Card handCard = new KamiOfTheHunt();
        harness.setHand(player1, List.of(handCard));
        gd.turnNumber = 2;

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
        });

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(mirrorId)).containsExactly(handCard);
        assertThat(gd.exiledCards).filteredOn(e -> mirrorId.equals(e.sourcePermanentId()))
                .allMatch(ExiledCardEntry::faceDown);
    }

    @Test
    @DisplayName("The mirror does not trigger during its opponent's upkeep")
    void opponentUpkeepDoesNotTrigger() {
        UUID mirrorId = addMirror();
        Card handCard = new KamiOfTheHunt();
        harness.setHand(player1, List.of(handCard));
        gd.turnNumber = 2;

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.getCardsExiledByPermanent(mirrorId)).isEmpty();
    }

    @Test
    @DisplayName("A later upkeep can return the hand exiled by an earlier upkeep")
    void subsequentUpkeepReturnsPreviouslyExiledHand() {
        UUID mirrorId = addMirror();
        Card originalHand = new KamiOfTheHunt();
        Card replacementHand = new SakuraTribeElder();
        harness.setHand(player1, List.of(originalHand));
        gd.turnNumber = 2;

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
        });
        assertThat(gd.getCardsExiledByPermanent(mirrorId)).containsExactly(originalHand);

        harness.setHand(player1, List.of(replacementHand));
        gd.turnNumber = 3;
        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
        });

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(originalHand);
        assertThat(gd.getCardsExiledByPermanent(mirrorId)).containsExactly(replacementHand);
    }
}
