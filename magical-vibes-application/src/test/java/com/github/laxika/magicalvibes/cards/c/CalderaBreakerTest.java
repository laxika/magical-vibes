package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.v.VolcanicGeyser;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CalderaBreaker.class, ChandraPyromaster.class, Forest.class, HillGiant.class, Mountain.class, VolcanicGeyser.class})
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

    @Test
    void exilesMountainsEvenWhenThereIsNoLegalDamageTarget() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Mountain(), new Forest(), new Mountain()));

        harness.castFromHand(player1, new CalderaBreaker(), "{3}{R}{R}{R}");
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getCardsExiledByPermanent(findPermanent(player1, "Caldera Breaker").getId()))
                .extracting(Card::getName).containsExactly("Mountain", "Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
    }

    @Test
    void canDamageOnlyAnOpponentsCreatureOrPlaneswalker() {
        Permanent friendly = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent chandra = harness.enterBattlefieldAndReturn(player2, new ChandraPyromaster());
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain()));

        harness.castFromHand(player1, new CalderaBreaker(), "{3}{R}{R}{R}");
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(chandra.getId())
                .doesNotContain(friendly.getId(), opposingLand.getId());
        assertThat(choice.validPlayerIds()).isEmpty();

        harness.handlePermanentChosen(player1, chandra.getId());
        resolveAllTriggers();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void deathReturnsTheMountainsActuallyExiledByItsEnterAbility() {
        Mountain first = new Mountain();
        Mountain second = new Mountain();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(first, forest, second));
        harness.castFromHand(player1, new CalderaBreaker(), "{3}{R}{R}{R}");
        resolveAllTriggers();
        Permanent breaker = findPermanent(player1, "Caldera Breaker");

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, breaker));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mountain"))
                .extracting(Permanent::getCard).containsExactlyInAnyOrder(first, second);
        assertThat(findPermanents(player1, "Mountain")).allMatch(permanent -> !permanent.isTapped());
        assertThat(gd.getCardsExiledByPermanent(breaker.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Volcanic Geyser", "Volcanic Geyser",
                        "Volcanic Geyser", "Volcanic Geyser");
    }

    @Test
    void reflexiveDamageStillResolvesAfterBreakerDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain()));
        harness.castFromHand(player1, new CalderaBreaker(), "{3}{R}{R}{R}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, target.getId());
        Permanent breaker = findPermanent(player1, "Caldera Breaker");

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, breaker));
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(countPermanents(player1, "Mountain")).isEqualTo(2);
        harness.assertInGraveyard(player1, "Caldera Breaker");
    }

    @Test
    void exilingBreakerDoesNotReturnMountainsOrConjureCards() {
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.castFromHand(player1, new CalderaBreaker(), "{3}{R}{R}{R}");
        resolveAllTriggers();
        Permanent breaker = findPermanent(player1, "Caldera Breaker");

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToExile(gd, breaker, null));
        resolveAllTriggers();

        assertThat(gd.getCardsExiledByPermanent(breaker.getId()))
                .extracting(Card::getName).containsExactly("Mountain");
        assertThat(countPermanents(player1, "Mountain")).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
