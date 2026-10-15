package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AncientVendetta.class, Swamp.class, PsychogenicProbe.class})
class AncientVendettaTest extends BaseCardTest {

    @Test
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new AncientVendetta()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    void exilesUpToFourNamedCardsAcrossAllThreeZones() {
        Card swamp1 = new Swamp();
        Card swamp2 = new Swamp();
        Card swamp3 = new Swamp();
        Card swamp4 = new Swamp();
        Card swamp5 = new Swamp();

        harness.setHand(player2, List.of(swamp1, swamp2));
        harness.setGraveyard(player2, List.of(swamp3));
        harness.setLibrary(player2, List.of(swamp4, swamp5));

        harness.setHand(player1, List.of(new AncientVendetta()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "Swamp");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiZoneExileChoice.class)).isNotNull();
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(swamp1.getId(), swamp2.getId(), swamp3.getId(), swamp4.getId(), swamp5.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Choose at most 4 cards");

        harness.handleMultipleCardsChosen(player1,
                List.of(swamp1.getId(), swamp2.getId(), swamp3.getId(), swamp4.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).filteredOn(c -> c.getName().equals("Swamp"))
                .hasSize(4);
        assertThat(gd.playerHands.get(player2.getId())).noneMatch(c -> c.getName().equals("Swamp"));
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(c -> c.getName().equals("Swamp"));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(swamp5);
    }

    @Test
    void canChooseFewerThanFourAndLeavesOtherZonesUntouched() {
        Card selected = new Swamp();
        Card unselected = new Swamp();
        Card unrelated = new AncientVendetta();
        Card ownCard = new Swamp();
        harness.setHand(player1, List.of(new AncientVendetta(), ownCard));
        harness.setHand(player2, List.of(selected, unrelated));
        harness.setGraveyard(player2, List.of(unselected));
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player2, new Swamp());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "Swamp");
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(selected);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(unrelated);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(unselected);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownCard);
        harness.assertOnBattlefield(player2, "Swamp");
    }

    @Test
    void choosingZeroStillTriggersShuffleAbilities() {
        Card matching = new Swamp();
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new AncientVendetta()));
        harness.setHand(player2, List.of(matching));
        harness.setLibrary(player2, List.of(new Swamp()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "Swamp");
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(matching);
        harness.assertLife(player2, 18);
    }

    @Test
    void exilingMatchingCardsStillTriggersShuffleAbilities() {
        Card matching = new Swamp();
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new AncientVendetta()));
        harness.setHand(player2, List.of(matching));
        harness.setLibrary(player2, List.of(new Swamp()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "Swamp");
        harness.handleMultipleCardsChosen(player1, List.of(matching.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(matching);
        harness.assertLife(player2, 18);
    }

    @Test
    void namingAbsentCardStillTriggersShuffleAbilities() {
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new AncientVendetta()));
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player2, List.of(new Swamp()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "Ancient Vendetta");
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 18);
    }

    @Test
    void rejectsNamesThatAreNotOracleCardNames() {
        harness.setHand(player1, List.of(new AncientVendetta()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleListChoice(player1, "This is not an Oracle card name"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
