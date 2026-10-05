package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JennikaBadAppleBigSister.class, Plains.class, Island.class})
class JennikaBadAppleBigSisterTest extends BaseCardTest {

    @Test
    void enteringCreatesTwoTwoRedMutantToken() {
        harness.castFromHand(player1, new JennikaBadAppleBigSister(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Mutant");
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.MUTANT);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    void plainscyclingPaysCostsThenSearchesForPlainsWithoutCreatingToken() {
        JennikaBadAppleBigSister jennika = new JennikaBadAppleBigSister();
        Plains plains = new Plains();
        Island island = new Island();
        harness.setHand(player1, List.of(jennika));
        harness.setLibrary(player1, List.of(island, plains));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(jennika);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island, plains);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(plains);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void plainscyclingMayFailToFindEvenWhenPlainsIsAvailable() {
        JennikaBadAppleBigSister jennika = new JennikaBadAppleBigSister();
        Plains plains = new Plains();
        harness.setHand(player1, List.of(jennika));
        harness.setLibrary(player1, List.of(plains));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(jennika);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
