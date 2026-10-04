package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AlloyGolem;
import com.github.laxika.magicalvibes.cards.b.BlurredMongoose;
import com.github.laxika.magicalvibes.cards.d.DrakeSkullCameo;
import com.github.laxika.magicalvibes.cards.q.QuirionElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Exclude.class, DrakeSkullCameo.class, QuirionElves.class, AlloyGolem.class, BlurredMongoose.class})
class ExcludeTest extends BaseCardTest {

    @Test
    void cannotTargetANonCreatureSpell() {
        DrakeSkullCameo cameo = new DrakeSkullCameo();

        harness.setHand(player2, List.of(new Exclude()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castFromHand(player1, cameo, "{3}");
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, cameo.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countersCreatureSpellAndDrawsACard() {
        QuirionElves elves = new QuirionElves();

        QuirionElves drawnCard = new QuirionElves();
        harness.setLibrary(player2, List.of(drawnCard));
        harness.setHand(player2, List.of(new Exclude()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castFromHand(player1, elves, "{1}{G}");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        harness.assertInGraveyard(player1, "Quirion Elves");
        harness.assertNotOnBattlefield(player1, "Quirion Elves");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
    }

    @Test
    void goesToGraveyardAfterResolving() {
        QuirionElves elves = new QuirionElves();

        harness.setHand(player2, List.of(new Exclude()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castFromHand(player1, elves, "{1}{G}");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        harness.assertInGraveyard(player2, "Exclude");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetAnArtifactCreatureSpell() {
        AlloyGolem golem = new AlloyGolem();

        harness.setLibrary(player2, List.of(new QuirionElves()));
        harness.setHand(player2, List.of(new Exclude()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castFromHand(player1, golem, "{6}");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, golem.getId());

        harness.assertInGraveyard(player1, "Alloy Golem");
        harness.assertNotOnBattlefield(player1, "Alloy Golem");
    }

    @Test
    void drawsEvenWhenCreatureSpellCannotBeCountered() {
        BlurredMongoose mongoose = new BlurredMongoose();
        QuirionElves drawnCard = new QuirionElves();
        harness.setLibrary(player2, List.of(drawnCard));
        harness.setHand(player2, List.of(new Exclude()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castFromHand(player1, mongoose, "{1}{G}");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, mongoose.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetableId()).isEqualTo(mongoose.getId());
        harness.assertNotInGraveyard(player1, "Blurred Mongoose");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        harness.assertInGraveyard(player2, "Exclude");
    }

    @Test
    void doesNotDrawWhenTargetWasAlreadyCountered() {
        QuirionElves elves = new QuirionElves();
        QuirionElves firstCard = new QuirionElves();
        AlloyGolem secondCard = new AlloyGolem();
        harness.setLibrary(player2, List.of(firstCard, secondCard));
        harness.setHand(player2, List.of(new Exclude(), new Exclude()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castFromHand(player1, elves, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstCard);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondCard);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .hasSize(2).allMatch(card -> card instanceof Exclude);
        harness.assertInGraveyard(player1, "Quirion Elves");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canCounterItsControllersCreatureSpell() {
        QuirionElves elves = new QuirionElves();
        QuirionElves drawnCard = new QuirionElves();
        harness.castFromHand(player1, elves, "{1}{G}");
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new Exclude()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.ensurePriority(player1);

        harness.castAndResolveInstant(player1, 0, elves.getId());

        harness.assertInGraveyard(player1, "Quirion Elves");
        harness.assertNotOnBattlefield(player1, "Quirion Elves");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertInGraveyard(player1, "Exclude");
        assertThat(gd.stack).isEmpty();
    }
}
