package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.Negate;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GeistpackAlpha.class, Forest.class, GrizzlyBears.class, Negate.class, WrathOfGod.class})
class GeistpackAlphaTest extends BaseCardTest {

    @Test
    void seeksPermanentWithManaValueEqualToLandsControlledWhenItDies() {
        Forest firstLand = new Forest();
        Forest secondLand = new Forest();
        GrizzlyBears sought = new GrizzlyBears();
        Negate nonPermanent = new Negate();
        harness.addToBattlefield(player1, firstLand);
        harness.addToBattlefield(player1, secondLand);
        harness.setLibrary(player1, List.of(nonPermanent, sought));
        harness.addToBattlefield(player1, new GeistpackAlpha());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(sought);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonPermanent);
    }

    @Test
    void doesNotSeekPermanentWithDifferentManaValue() {
        harness.addToBattlefield(player1, new Forest());
        Card wrongManaValue = new GrizzlyBears();
        harness.setLibrary(player1, List.of(wrongManaValue));
        Permanent geistpack = harness.addToBattlefieldAndReturn(player1, new GeistpackAlpha());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(wrongManaValue);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(wrongManaValue);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(geistpack.getCard());
    }

    @Test
    void seeksLandWithNoLandsControlledWithoutReorderingRemainingLibrary() {
        Forest sought = new Forest();
        GrizzlyBears first = new GrizzlyBears();
        Negate second = new Negate();
        harness.setLibrary(player1, List.of(first, sought, second));
        harness.addToBattlefield(player1, new GeistpackAlpha());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sought);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void countsLandsWhenDeathTriggerResolves() {
        harness.addToBattlefield(player1, new Forest());
        GrizzlyBears sought = new GrizzlyBears();
        harness.setLibrary(player1, List.of(sought));
        harness.addToBattlefield(player1, new GeistpackAlpha());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player1, new Forest());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sought);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void seeksExactlyOneMatchingCard() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        harness.addToBattlefield(player1, new GeistpackAlpha());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isIn(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst())
                .isNotSameAs(gd.playerHands.get(player1.getId()).getFirst());
    }

    @Test
    void usesDeathTriggersControllerForLandCountAndLibrary() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        GrizzlyBears sought = new GrizzlyBears();
        GrizzlyBears opponentsCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(sought));
        harness.setLibrary(player1, List.of(opponentsCard));
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player2, new GeistpackAlpha());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(sought);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentsCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(opponentsCard, sought);
    }
}
