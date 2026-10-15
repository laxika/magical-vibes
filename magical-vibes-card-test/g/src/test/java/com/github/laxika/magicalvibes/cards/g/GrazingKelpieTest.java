package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SpringjackPasture;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrazingKelpie.class, HillGiant.class, SpringjackPasture.class})
class GrazingKelpieTest extends BaseCardTest {

    private int kelpieIndex(Permanent kelpie) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(kelpie);
    }

    @Test
    @DisplayName("Tucks a card from its owner's graveyard onto the bottom of that owner's library")
    void tucksCardToBottomOfOwnersLibrary() {
        Permanent kelpie = harness.addToBattlefieldAndReturn(player1, new GrazingKelpie());
        harness.addMana(player1, ManaColor.GREEN, 1);

        Card tucked = new HillGiant();
        harness.setGraveyard(player1, new ArrayList<>(List.of(tucked)));
        harness.setLibrary(player1, new ArrayList<>(List.of(new HillGiant(), new HillGiant())));

        harness.activateAbilityWithGraveyardTargets(player1, kelpieIndex(kelpie), 0, List.of(tucked.getId()));
        resolveAllTriggers();

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(3);
        assertThat(library.get(library.size() - 1).getId()).isEqualTo(tucked.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(tucked.getId()));
    }

    @Test
    @DisplayName("A targeted opponent's card goes to the bottom of the opponent's library")
    void tucksOpponentCardToOpponentLibrary() {
        Permanent kelpie = harness.addToBattlefieldAndReturn(player1, new GrazingKelpie());
        harness.addMana(player1, ManaColor.BLUE, 1);

        Card tucked = new HillGiant();
        harness.setGraveyard(player2, new ArrayList<>(List.of(tucked)));
        harness.setLibrary(player2, new ArrayList<>(List.of(new HillGiant())));

        harness.activateAbilityWithGraveyardTargets(player1, kelpieIndex(kelpie), 0, List.of(tucked.getId()));
        resolveAllTriggers();

        List<Card> opponentLibrary = gd.playerDecks.get(player2.getId());
        assertThat(opponentLibrary).hasSize(2);
        assertThat(opponentLibrary.get(opponentLibrary.size() - 1).getId()).isEqualTo(tucked.getId());
    }

    @Test
    @DisplayName("Persist returns the sacrificed Kelpie with a -1/-1 counter")
    void persistReturnsSacrificedKelpie() {
        Permanent kelpie = harness.addToBattlefieldAndReturn(player1, new GrazingKelpie());
        harness.addMana(player1, ManaColor.GREEN, 1);

        Card tucked = new HillGiant();
        harness.setGraveyard(player1, new ArrayList<>(List.of(tucked)));
        harness.setLibrary(player1, new ArrayList<>(List.of(new HillGiant())));

        harness.activateAbilityWithGraveyardTargets(player1, kelpieIndex(kelpie), 0, List.of(tucked.getId()));
        resolveAllTriggers();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grazing Kelpie"))
                .findFirst().orElse(null);
        assertThat(returned).isNotNull();
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }
    @Test
    @DisplayName("A land card can be put into an empty library")
    void tucksNoncreatureIntoEmptyLibrary() {
        Permanent kelpie = harness.addToBattlefieldAndReturn(player1, new GrazingKelpie());
        Card tucked = new SpringjackPasture();
        harness.setGraveyard(player2, List.of(tucked));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithGraveyardTargets(player1, kelpieIndex(kelpie), 0, List.of(tucked.getId()));
        harness.assertNotOnBattlefield(player1, "Grazing Kelpie");
        harness.assertInGraveyard(player1, "Grazing Kelpie");
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(tucked);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A Kelpie with a -1/-1 counter does not persist after paying its sacrifice cost")
    void doesNotPersistWithMinusOneCounter() {
        Permanent kelpie = harness.addToBattlefieldAndReturn(player1, new GrazingKelpie());
        kelpie.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Card tucked = new SpringjackPasture();
        harness.setGraveyard(player2, List.of(tucked));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbilityWithGraveyardTargets(player1, kelpieIndex(kelpie), 0, List.of(tucked.getId()));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grazing Kelpie");
        harness.assertInGraveyard(player1, "Grazing Kelpie");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(tucked);
    }

    @Test
    @DisplayName("An unavailable graveyard target does not prevent persist")
    void persistsEvenWhenTargetLeavesGraveyard() {
        Permanent kelpie = harness.addToBattlefieldAndReturn(player1, new GrazingKelpie());
        Card tucked = new SpringjackPasture();
        harness.setGraveyard(player2, List.of(tucked));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbilityWithGraveyardTargets(player1, kelpieIndex(kelpie), 0, List.of(tucked.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(tucked));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grazing Kelpie");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(p -> {
                    assertThat(p.getCard().getId()).isEqualTo(kelpie.getCard().getId());
                    assertThat(p.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
                });
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(tucked);
    }
}
