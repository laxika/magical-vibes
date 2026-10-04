package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeadOfTheHunt.class, DoomBlade.class, GrizzlyBears.class, WrathOfGod.class, Humble.class})
class HeadOfTheHuntTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opponent's dying creature and creates a 2/2 green Wolf")
    void exilesOpponentCreatureAndCreatesWolf() {
        harness.addToBattlefield(player1, new HeadOfTheHunt());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(bears.getCard().getId())).isNotNull();
        harness.assertNotInGraveyard(player2, "Grizzly Bears");

        List<Permanent> wolves = findPermanents(player1, "Wolf");
        assertThat(wolves).hasSize(1);
        Permanent wolf = wolves.getFirst();
        assertThat(wolf.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(wolf.getCard().getSubtypes()).containsExactly(CardSubtype.WOLF);
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not replace the controller's own dying creature")
    void ownCreatureDiesNormally() {
        harness.addToBattlefield(player1, new HeadOfTheHunt());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Wolf")).isEmpty();
    }

    @Test
    @DisplayName("Creates one Wolf for each opponent creature exiled by a mass destruction")
    void createsOneWolfPerExiledCreature() {
        harness.addToBattlefield(player1, new HeadOfTheHunt());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wolf")).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .doesNotContain("Grizzly Bears");
    }

    @Test
    @DisplayName("Wolf creation waits for its triggered ability to resolve")
    void wolfCreationUsesTheStack() {
        harness.addToBattlefield(player1, new HeadOfTheHunt());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(gd.findExiledCard(bears.getCard().getId())).isNotNull();
        assertThat(findPermanents(player1, "Wolf")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        assertThat(findPermanents(player1, "Wolf")).hasSize(1);
    }

    @Test
    @DisplayName("Losing abilities disables the exile replacement and Wolf trigger")
    void losingAbilitiesDisablesReplacement() {
        Permanent head = harness.addToBattlefieldAndReturn(player1, new HeadOfTheHunt());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Humble(), new DoomBlade()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, head.getId());
        harness.castAndResolveInstant(player2, 0, bears.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNull();
        assertThat(findPermanents(player1, "Wolf")).isEmpty();
    }

    @Test
    @DisplayName("Exiling an opponent's dying token also creates a Wolf")
    void tokenCreaturesAlsoCreateWolves() {
        harness.addToBattlefield(player1, new HeadOfTheHunt());
        harness.addToBattlefield(player2, new HeadOfTheHunt());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        resolveAllTriggers();

        Permanent opposingWolf = findPermanent(player2, "Wolf");
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, opposingWolf.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Wolf");
        harness.assertNotInGraveyard(player2, "Wolf");
        assertThat(findPermanents(player1, "Wolf")).hasSize(1);
    }

    @Test
    @DisplayName("Flash allows casting Head of the Hunt during the opponent's turn")
    void canBeCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new HeadOfTheHunt()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Head of the Hunt");
    }
}
