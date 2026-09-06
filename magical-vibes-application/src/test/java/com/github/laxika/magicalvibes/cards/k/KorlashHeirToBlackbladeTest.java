package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KorlashHeirToBlackblade.class, Swamp.class, Forest.class})
class KorlashHeirToBlackbladeTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of Swamps its controller controls")
    void powerAndToughnessEqualControlledSwamps() {
        Permanent korlash = addCreatureReady(player1, new KorlashHeirToBlackblade());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, korlash)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, korlash)).isEqualTo(2);
    }

    @Test
    @DisplayName("Regeneration ability grants a regeneration shield")
    void regenerationAbilityGrantsShield() {
        Permanent korlash = addCreatureReady(player1, new KorlashHeirToBlackblade());
        harness.addToBattlefield(player1, new Swamp());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(korlash.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Grandeur discards another Korlash and searches for two Swamps")
    void grandeurSearchesForTwoSwamps() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent korlash = addCreatureReady(player1, new KorlashHeirToBlackblade());
        harness.addToBattlefield(player1, new Swamp());
        KorlashHeirToBlackblade discardedKorlash = new KorlashHeirToBlackblade();
        Swamp firstSwamp = new Swamp();
        Swamp secondSwamp = new Swamp();
        Forest forest = new Forest();
        setLibrary(firstSwamp, secondSwamp, forest);
        harness.setHand(player1, List.of(discardedKorlash));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(firstSwamp, secondSwamp);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Swamp"))
                .hasSize(3)
                .filteredOn(Permanent::isTapped)
                .hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discardedKorlash);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("Grandeur requires another Korlash card to discard")
    void grandeurRequiresAnotherKorlash() {
        addCreatureReady(player1, new KorlashHeirToBlackblade());
        harness.setHand(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void setLibrary(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
