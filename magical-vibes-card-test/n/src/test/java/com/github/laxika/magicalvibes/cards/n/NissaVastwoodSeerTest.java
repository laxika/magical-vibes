package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LeafGilder;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NissaVastwoodSeer.class, Forest.class, Mountain.class, LeafGilder.class})
class NissaVastwoodSeerTest extends BaseCardTest {

    @Test
    @DisplayName("The enter trigger offers only basic Forest cards and puts the chosen one into hand")
    void enterTriggerFetchesBasicForest() {
        setLibrary(new Forest(), new Mountain(), new LeafGilder());
        castNissa();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(1)
                .allMatch(c -> c.getName().equals("Forest"));

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Declining the enter trigger leaves the library untouched")
    void decliningEnterTriggerFetchesNothing() {
        setLibrary(new Forest(), new Mountain());
        castNissa();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A land entering does not transform Nissa while you control fewer than seven lands")
    void landfallBelowSevenLandsDoesNotTransform() {
        setLibrary(new LeafGilder());
        Permanent nissa = addReadyNissa(player1);
        addLands(player1, 5);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nissa, Vastwood Seer");
        harness.assertNotOnBattlefield(player1, "Nissa, Sage Animist");
        assertThat(nissa.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("The seventh land counts itself, so Nissa returns transformed as a planeswalker")
    void landfallAtSevenLandsTransforms() {
        setLibrary(new LeafGilder());
        addReadyNissa(player1);
        addLands(player1, 6);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nissa, Vastwood Seer");
        harness.assertOnBattlefield(player1, "Nissa, Sage Animist");

        Permanent walker = findPermanent(player1, "Nissa, Sage Animist");
        assertThat(walker.isTransformed()).isTrue();
        assertThat(walker.getCard().hasType(CardType.PLANESWALKER)).isTrue();
        assertThat(walker.getCounterCount(CounterType.LOYALTY)).isPositive();
    }

    @Test
    @DisplayName("A land an opponent plays never transforms Nissa")
    void opponentLandDoesNotTransform() {
        setLibrary(new LeafGilder());
        Permanent nissa = addReadyNissa(player1);
        addLands(player1, 6);
        addLands(player2, 6);
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nissa, Vastwood Seer");
        assertThat(nissa.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("The search may fail to find even when a basic Forest is available")
    void searchMayFailToFind() {
        setLibrary(new Forest(), new Mountain());
        castNissa();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Searching a library with no basic Forest completes without taking a card")
    void searchWithoutForestCompletes() {
        setLibrary(new Mountain());
        castNissa();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInHand(player1, "Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Entering while seven lands are already controlled does not transform Nissa")
    void enteringWithSevenLandsDoesNotTransform() {
        setLibrary(new Forest());
        addLands(player1, 7);
        castNissa();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Nissa, Vastwood Seer");
        harness.assertNotOnBattlefield(player1, "Nissa, Sage Animist");
    }

    @Test
    @DisplayName("Losing the seventh land before resolution prevents transformation")
    void landfallRechecksLandCountOnResolution() {
        setLibrary(new Forest());
        addReadyNissa(player1);
        addLands(player1, 6);
        harness.setHand(player1, List.of(new Mountain()));
        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(1);

        Permanent mountain = findPermanent(player1, "Mountain");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, mountain));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nissa, Vastwood Seer");
        harness.assertNotOnBattlefield(player1, "Nissa, Sage Animist");
    }

    @Test
    @DisplayName("A Nissa that leaves before the landfall trigger resolves is not returned")
    void landfallDoesNotReturnNissaThatAlreadyLeft() {
        setLibrary(new Forest());
        Permanent nissa = addReadyNissa(player1);
        addLands(player1, 6);
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, nissa));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Nissa, Vastwood Seer");
        harness.assertNotOnBattlefield(player1, "Nissa, Sage Animist");
    }

    @Test
    @DisplayName("A stolen Nissa returns as a new planeswalker under her owner's control")
    void landfallReturnsUnderOwnersControl() {
        setLibrary(new Forest());
        NissaVastwoodSeer card = new NissaVastwoodSeer();
        card.setOwnerId(player2.getId());
        Permanent original = addCreatureReady(player1, card);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addLands(player1, 6);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nissa, Vastwood Seer");
        harness.assertNotOnBattlefield(player1, "Nissa, Sage Animist");
        Permanent returned = findPermanent(player2, "Nissa, Sage Animist");
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Lands entering without being played trigger Nissa only once for the original object")
    void multipleLandfallTriggersDoNotTransformReturnedNissaAgain() {
        setLibrary(new Forest());
        addReadyNissa(player1);
        addLands(player1, 6);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Mountain());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Nissa, Sage Animist");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Nissa, Sage Animist")).isSameAs(returned);
        assertThat(returned.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertNotOnBattlefield(player1, "Nissa, Vastwood Seer");
    }

    @Test
    @DisplayName("An opponent's land does not trigger Nissa even when her controller has seven lands")
    void opponentLandDoesNotTriggerWithSevenLands() {
        setLibrary(new Forest());
        addReadyNissa(player1);
        addLands(player1, 7);
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Nissa, Vastwood Seer");
        harness.assertNotOnBattlefield(player1, "Nissa, Sage Animist");
    }

    private void castNissa() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new NissaVastwoodSeer(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addReadyNissa(Player player) {
        Permanent perm = addCreatureReady(player, new NissaVastwoodSeer());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private void addLands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
