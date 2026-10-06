package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeskitTheFleshSculptor.class, GrizzlyBears.class, Spellbook.class})
class KeskitTheFleshSculptorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices three other artifacts or creatures and puts two looked-at cards into hand")
    void sacrificesThreeAndSplitsTopThree() {
        Permanent keskit = addReadyKeskit();
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Card first = new GrizzlyBears();
        Card chosen = new Spellbook();
        Card third = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(first, chosen, third));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstCreature.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice interaction =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(interaction).isNotNull();
        assertThat(interaction.allCards()).containsExactly(first, chosen, third);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), chosen.getId()));

        assertThat(keskit.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, chosen);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(firstCreature.getCard(), artifact.getCard(), secondCreature.getCard(), third);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(keskit);
    }

    @Test
    @DisplayName("Cannot sacrifice Keskit itself as one of the three permanents")
    void sourceIsExcludedFromSacrificeCost() {
        addReadyKeskit();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Spellbook());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    private Permanent addReadyKeskit() {
        Permanent keskit = harness.addToBattlefieldAndReturn(player1, new KeskitTheFleshSculptor());
        keskit.setSummoningSick(false);
        return keskit;
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void putsAllAvailableCardsIntoHandWithFewerThanThreeInLibrary(int librarySize) {
        addReadyKeskit();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        List<Card> library = List.<Card>of(new GrizzlyBears(), new Spellbook()).subList(0, librarySize);
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(first.getCard(), second.getCard(), third.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNull();
    }

    @Test
    void mustChooseExactlyTwoCardsAndLeavesCardsBelowTheTopThreeAlone() {
        addReadyKeskit();
        Permanent firstCost = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCost = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card first = new Spellbook();
        Card second = new GrizzlyBears();
        Card third = new Spellbook();
        Card fourth = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstCost.getId());
        harness.handlePermanentChosen(player1, secondCost.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid number of cards selected");
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid number of cards selected");
        harness.handleMultipleCardsChosen(player1, List.of(second.getId(), third.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
    }

    @Test
    void cannotUseOpponentsPermanentsToPaySacrificeCost() {
        Permanent keskit = addReadyKeskit();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
        assertThat(keskit.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void cannotActivateWhileSummoningSickOrTapped(boolean tapped) {
        Permanent keskit = addReadyKeskit();
        if (tapped) {
            keskit.tap();
        }
        keskit.setSummoningSick(!tapped);
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(tapped ? "already tapped" : "summoning sickness");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
    }
}
