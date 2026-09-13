package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TameshiRealityArchitect.class, Boomerang.class, FountainOfYouth.class,
        GrizzlyBears.class, Plains.class})
class TameshiRealityArchitectTest extends BaseCardTest {

    @Test
    void returnsAnArtifactFromTheGraveyardUsingX() {
        FountainOfYouth fountain = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(fountain));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefieldAndReturn(player1, new TameshiRealityArchitect());
        harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, 1, fountain.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fountain of Youth");
        harness.assertInHand(player1, "Plains");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(fountain);
    }

    @Test
    void cannotTargetACreatureCard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addToBattlefieldAndReturn(player1, new TameshiRealityArchitect());
        harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, 2, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Plains");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears);
    }

    @Test
    void drawsOnlyOnceForMultipleNoncreaturePermanentsReturnedInOneTurn() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Boomerang(), new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addToBattlefieldAndReturn(player1, new TameshiRealityArchitect());
        Permanent firstFountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent secondFountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        harness.castAndResolveInstant(player1, 0, firstFountain.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, secondFountain.getId());

        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears")))
                .hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    void doesNotDrawWhenAcreatureIsReturnedToHand() {
        harness.setLibrary(player1, List.of(new FountainOfYouth()));
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addToBattlefieldAndReturn(player1, new TameshiRealityArchitect());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Fountain of Youth");
    }
}
