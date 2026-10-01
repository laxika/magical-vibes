package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.v.VolcanicGeyser;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CalderaBreaker.class, Forest.class, HillGiant.class, Mountain.class, VolcanicGeyser.class})
class CalderaBreakerTest extends BaseCardTest {

    @Test
    void exilesAllMountainsAndDealsThatMuchDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new Mountain(), new Forest(), new Mountain()));

        harness.castFromHand(player1, new CalderaBreaker(), "{3}{R}{R}{R}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        Permanent breaker = findPermanent(player1, "Caldera Breaker");
        assertThat(gd.getCardsExiledByPermanent(breaker.getId()))
                .extracting(Card::getName)
                .containsExactly("Mountain", "Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");

        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void doesNotCreateTheReflexiveAbilityWhenNoMountainWasExiled() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.castFromHand(player1, new CalderaBreaker(), "{3}{R}{R}{R}");
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void returnsTrackedCardsToTheirOwnersAndConjuresVolcanicGeysersOnDeath() {
        harness.setLibrary(player1, List.of());
        Permanent breaker = harness.addToBattlefieldAndReturn(player1, new CalderaBreaker());
        HillGiant returned = new HillGiant();
        gd.addToExile(player2.getId(), returned, breaker.getId());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, breaker));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .contains("Hill Giant");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Volcanic Geyser", "Volcanic Geyser", "Volcanic Geyser", "Volcanic Geyser");
    }
}
