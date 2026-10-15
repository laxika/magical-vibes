package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KuraTheBoundlessSky;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShigekiJukaiVisionary.class, Forest.class, GrizzlyBears.class, KuraTheBoundlessSky.class, LeylineOfTheVoid.class})
class ShigekiJukaiVisionaryTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability may put a revealed land onto the battlefield tapped")
    void activatedAbilityPutsLandOntoBattlefieldTappedAndRestIntoGraveyard() {
        addCreatureReady(player1, new ShigekiJukaiVisionary());
        Forest forest = new Forest();
        List<Card> nonlands = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, List.of(forest, nonlands.get(0), nonlands.get(1), nonlands.get(2)));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == forest && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(nonlands);
        harness.assertInHand(player1, "Shigeki, Jukai Visionary");
    }

    @Test
    @DisplayName("Channel returns exactly X target nonlegendary cards and discards the source")
    void channelReturnsXNonlegendaryCards() {
        harness.setHand(player1, List.of(new ShigekiJukaiVisionary()));
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card legendary = new KuraTheBoundlessSky();
        harness.setGraveyard(player1, List.of(first, second, legendary));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateHandAbilityWithGraveyardTargets(player1, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(legendary)
                .anyMatch(card -> card.getName().equals("Shigeki, Jukai Visionary"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Channel cannot choose fewer targets than X when enough legal cards exist")
    void channelRequiresExactlyXTargets() {
        harness.setHand(player1, List.of(new ShigekiJukaiVisionary()));
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.activateHandAbilityWithGraveyardTargets(
                player1, 0, 2, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Shigeki, Jukai Visionary");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(6);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returning Shigeki to hand is paid before its ability resolves")
    void returnToHandIsAnActivationCost() {
        addCreatureReady(player1, new ShigekiJukaiVisionary());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertInHand(player1, "Shigeki, Jukai Visionary");
        harness.assertNotOnBattlefield(player1, "Shigeki, Jukai Visionary");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == forest && permanent.isTapped());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the land puts all four revealed cards into the graveyard")
    void mayDeclineLandAndLeavesFifthCardInLibrary() {
        addCreatureReady(player1, new ShigekiJukaiVisionary());
        List<Card> revealed = List.of(new Forest(), new Forest(), new KuraTheBoundlessSky(), new Forest());
        Card fifth = new Forest();
        harness.setLibrary(player1, List.of(revealed.get(0), revealed.get(1), revealed.get(2), revealed.get(3), fifth));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(revealed);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifth);
    }

    @Test
    @DisplayName("Revealed cards with no land respect graveyard replacement effects")
    void noLandRevealedRespectsLeylineOfTheVoid() {
        addCreatureReady(player1, new ShigekiJukaiVisionary());
        harness.addToBattlefield(player2, new LeylineOfTheVoid());
        List<Card> revealed = List.of(new KuraTheBoundlessSky(), new KuraTheBoundlessSky());
        harness.setLibrary(player1, revealed);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(revealed);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Shigeki, Jukai Visionary");
    }

    @Test
    @DisplayName("Channel can be activated with X zero and no graveyard targets")
    void channelWithZeroX() {
        Card shigeki = new ShigekiJukaiVisionary();
        harness.setHand(player1, List.of(shigeki));
        harness.setGraveyard(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbilityWithGraveyardTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shigeki);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Channel rejects legendary cards and cards in an opponent's graveyard")
    void channelRejectsIllegalGraveyardTargets() {
        harness.setHand(player1, List.of(new ShigekiJukaiVisionary()));
        Card legendary = new KuraTheBoundlessSky();
        Card opponentsCard = new Forest();
        harness.setGraveyard(player1, List.of(legendary));
        harness.setGraveyard(player2, List.of(opponentsCard));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.activateHandAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(legendary.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateHandAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(opponentsCard.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Shigeki, Jukai Visionary");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Channel returns surviving legal targets when another target leaves the graveyard")
    void channelResolvesWithOneRemainingTarget() {
        harness.setHand(player1, List.of(new ShigekiJukaiVisionary()));
        Card first = new Forest();
        Card second = new Forest();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateHandAbilityWithGraveyardTargets(player1, 0, 2, List.of(first.getId(), second.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(first);
        gd.addToExile(player1.getId(), first);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first);
        harness.assertInGraveyard(player1, "Shigeki, Jukai Visionary");
        assertThat(gd.stack).isEmpty();
    }
}
