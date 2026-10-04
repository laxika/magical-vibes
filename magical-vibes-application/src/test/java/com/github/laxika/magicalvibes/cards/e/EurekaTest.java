package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AdventurersGuildhouse;
import com.github.laxika.magicalvibes.cards.c.ChainLightning;
import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.s.SpiritLink;
import com.github.laxika.magicalvibes.cards.w.WallOfBlossoms;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Eureka.class, AdventurersGuildhouse.class, DurkwoodBoars.class, ChainLightning.class,
        SpiritLink.class, WallOfBlossoms.class})
class EurekaTest extends BaseCardTest {

    @Test
    @DisplayName("Repeats controller-first rounds until no player puts a permanent onto the battlefield")
    void repeatsControllerFirstRoundsUntilNoPlayerPutsPermanent() {
        Eureka eureka = new Eureka();
        DurkwoodBoars boars = new DurkwoodBoars();
        AdventurersGuildhouse firstGuildhouse = new AdventurersGuildhouse();
        AdventurersGuildhouse secondGuildhouse = new AdventurersGuildhouse();
        ChainLightning chainLightning = new ChainLightning();
        harness.setHand(player1, List.of(eureka, boars, firstGuildhouse));
        harness.setHand(player2, List.of(secondGuildhouse, chainLightning));

        castEureka();

        PendingInteraction.EachPlayerMayPutCardFromHandChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.validCardIds()).containsExactly(boars.getId(), firstGuildhouse.getId());

        harness.handleMultipleCardsChosen(player1, List.of(boars.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(boars.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class)
                .playerId()).isEqualTo(player2.getId());
        harness.handleMultipleCardsChosen(player2, List.of(secondGuildhouse.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class)
                .playerId()).isEqualTo(player1.getId());
        harness.handleMultipleCardsChosen(player1, List.of(firstGuildhouse.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(boars.getId(), firstGuildhouse.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(secondGuildhouse.getId());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(chainLightning);
        harness.assertInGraveyard(player1, "Eureka");
    }

    @Test
    @DisplayName("Offers a player another choice after they decline while another player puts a permanent")
    void offersAnotherChoiceAfterDeclineWhileAnotherPlayerPutsPermanent() {
        Eureka eureka = new Eureka();
        DurkwoodBoars boars = new DurkwoodBoars();
        AdventurersGuildhouse guildhouse = new AdventurersGuildhouse();
        harness.setHand(player1, List.of(eureka, boars));
        harness.setHand(player2, List.of(guildhouse));

        castEureka();

        harness.handleMultipleCardsChosen(player1, List.of());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class)
                .playerId()).isEqualTo(player2.getId());

        harness.handleMultipleCardsChosen(player2, List.of(guildhouse.getId()));
        PendingInteraction.EachPlayerMayPutCardFromHandChoice repeatedChoice =
                gd.interaction.activeInteraction(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class);
        assertThat(repeatedChoice.playerId()).isEqualTo(player1.getId());
        assertThat(repeatedChoice.validCardIds()).containsExactly(boars.getId());

        harness.handleMultipleCardsChosen(player1, List.of(boars.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Durkwood Boars");
        harness.assertOnBattlefield(player2, "Adventurers' Guildhouse");
    }

    @Test
    @DisplayName("Does not offer nonpermanent cards")
    void doesNotOfferNonpermanentCards() {
        Eureka eureka = new Eureka();
        ChainLightning chainLightning = new ChainLightning();
        harness.setHand(player1, List.of(eureka, chainLightning));
        harness.setHand(player2, List.of());

        castEureka();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chainLightning);
        harness.assertInGraveyard(player1, "Eureka");
    }

    @Test
    void stopsWhenBothPlayersDeclineWithPermanentsStillInHand() {
        DurkwoodBoars boars = new DurkwoodBoars();
        AdventurersGuildhouse guildhouse = new AdventurersGuildhouse();
        harness.setHand(player1, List.of(new Eureka(), boars));
        harness.setHand(player2, List.of(guildhouse));

        castEureka();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMultipleCardsChosen(player2, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(boars);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(guildhouse);
        harness.assertNotOnBattlefield(player1, "Durkwood Boars");
        harness.assertNotOnBattlefield(player2, "Adventurers' Guildhouse");
        harness.assertInGraveyard(player1, "Eureka");
    }

    @Test
    void rejectsPuttingTwoCardsOntoTheBattlefieldInOneChoice() {
        DurkwoodBoars boars = new DurkwoodBoars();
        AdventurersGuildhouse guildhouse = new AdventurersGuildhouse();
        harness.setHand(player1, List.of(new Eureka(), boars, guildhouse));
        harness.setHand(player2, List.of());

        castEureka();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(boars.getId(), guildhouse.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(boars, guildhouse);
        harness.assertNotOnBattlefield(player1, "Durkwood Boars");
        harness.assertNotOnBattlefield(player1, "Adventurers' Guildhouse");
        harness.handleMultipleCardsChosen(player1, List.of());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void auraCanEnchantACreaturePutOntoTheBattlefieldInAnEarlierRound() {
        DurkwoodBoars boars = new DurkwoodBoars();
        SpiritLink aura = new SpiritLink();
        harness.setHand(player1, List.of(new Eureka(), aura));
        harness.setHand(player2, List.of(boars));

        castEureka();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMultipleCardsChosen(player2, List.of(boars.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        assertThat(findPermanent(player1, "Spirit Link").getAttachedTo())
                .isEqualTo(findPermanent(player2, "Durkwood Boars").getId());
        harness.assertNotInHand(player1, "Spirit Link");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void auraWithoutALegalCreatureToEnchantRemainsInHand() {
        SpiritLink aura = new SpiritLink();
        harness.setHand(player1, List.of(new Eureka(), aura));
        harness.setHand(player2, List.of());

        castEureka();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(aura);
        harness.assertNotOnBattlefield(player1, "Spirit Link");
        harness.assertNotInGraveyard(player1, "Spirit Link");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({Eureka.class, WallOfBlossoms.class, AdventurersGuildhouse.class, DurkwoodBoars.class})
    void enteringCreaturesTriggerTheirOwnAbilitiesAfterEurekaFinishes() {
        WallOfBlossoms wall = new WallOfBlossoms();
        AdventurersGuildhouse guildhouse = new AdventurersGuildhouse();
        DurkwoodBoars drawnCard = new DurkwoodBoars();
        harness.setHand(player1, List.of(new Eureka(), wall, guildhouse));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(drawnCard));

        castEureka();
        harness.handleMultipleCardsChosen(player1, List.of(wall.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(guildhouse);
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertOnBattlefield(player1, "Wall of Blossoms");
        harness.assertInGraveyard(player1, "Eureka");
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(guildhouse, drawnCard);
    }

    private void castEureka() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
