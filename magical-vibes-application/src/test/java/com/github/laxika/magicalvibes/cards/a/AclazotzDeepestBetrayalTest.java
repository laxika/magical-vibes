package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JoinTheDead;
import com.github.laxika.magicalvibes.cards.p.PrimordialGnawer;
import com.github.laxika.magicalvibes.cards.t.TamiyoCollectorOfTales;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AclazotzDeepestBetrayal.class, Forest.class, PrimordialGnawer.class, JoinTheDead.class, TamiyoCollectorOfTales.class})
class AclazotzDeepestBetrayalTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking makes an opponent discard a land and creates a flying Bat")
    void attackingDiscardedLandCreatesBat() {
        Forest discarded = new Forest();
        harness.setHand(player2, List.of(discarded));
        Permanent aclazotz = addAclazotzReady(player1);

        declareAttackers(List.of(indexOf(player1, aclazotz)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        List<Permanent> bats = findPermanents(player1, "Bat");
        assertThat(bats).hasSize(1);
        assertThat(gqs.hasKeyword(gd, bats.get(0), Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Attacking draws for an opponent who cannot discard")
    void attackingDrawsForOpponentWithEmptyHand() {
        Card drawn = new PrimordialGnawer();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(drawn));
        Permanent aclazotz = addAclazotzReady(player1);

        declareAttackers(List.of(indexOf(player1, aclazotz)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("The back face taps for black mana")
    void backFaceTapsForBlackMana() {
        Permanent temple = addTempleReady(player1);

        harness.activateAbility(player1, indexOf(player1, temple), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(temple.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The back face transforms when any player has one or fewer cards in hand")
    void backFaceTransformsWhenAnyPlayerHasSmallHandAtActivation() {
        Permanent temple = addTempleReady(player1);
        harness.setHand(player1, List.of(new PrimordialGnawer(), new PrimordialGnawer()));
        harness.setHand(player2, List.of(new PrimordialGnawer()));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(player1, temple), 1, null, null);
        harness.setHand(player2, List.of(new PrimordialGnawer(), new PrimordialGnawer()));
        harness.passBothPriorities();

        assertThat(temple.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("The back face cannot transform when every player has more than one card")
    void backFaceCannotTransformWithLargeHands() {
        Permanent temple = addTempleReady(player1);
        harness.setHand(player1, List.of(new PrimordialGnawer(), new PrimordialGnawer()));
        harness.setHand(player2, List.of(new PrimordialGnawer(), new PrimordialGnawer()));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, temple), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Dying returns the card transformed and tapped")
    void dyingReturnsTransformedAndTapped() {
        Permanent aclazotz = addAclazotzReady(player1);
        harness.setHand(player2, List.of(new JoinTheDead()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, aclazotz.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent returned = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(returned.isTransformed()).isTrue();
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(aclazotz.getCard().getId()));
    }

    @Test
    @DisplayName("The attack draw happens during the original ability's resolution")
    void attackDrawIsNotASeparateAbility() {
        Card drawn = new PrimordialGnawer();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(drawn));
        Permanent aclazotz = addAclazotzReady(player1);
        declareAttackers(List.of(indexOf(player1, aclazotz)));

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent protected from discard still causes the controller to draw")
    void preventedDiscardCausesDraw() {
        Card drawn = new PrimordialGnawer();
        Forest protectedCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(protectedCard));
        harness.setLibrary(player1, List.of(drawn));
        harness.addToBattlefield(player2, new TamiyoCollectorOfTales());
        Permanent tamiyo = gd.playerBattlefields.get(player2.getId()).getLast();
        tamiyo.setCounterCount(com.github.laxika.magicalvibes.model.CounterType.LOYALTY, 5);
        Permanent aclazotz = addAclazotzReady(player1);
        declareAttackers(List.of(indexOf(player1, aclazotz)));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(protectedCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(findPermanents(player1, "Bat")).isEmpty();
    }

    @Test
    @DisplayName("Discarding a nonland creates no Bat and draws no card")
    void nonlandDiscardDoesNotCreateBatOrDraw() {
        Card discarded = new PrimordialGnawer();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent aclazotz = addAclazotzReady(player1);
        declareAttackers(List.of(indexOf(player1, aclazotz)));
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Bat")).isEmpty();
    }

    @Test
    @DisplayName("Temple cannot transform outside its controller's main phase")
    void templeCannotTransformAtInstantSpeed() {
        Permanent temple = addTempleReady(player1);
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, temple), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(temple.isTransformed()).isTrue();
        assertThat(temple.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A stolen Aclazotz returns under its owner's control")
    void stolenAclazotzReturnsToOwner() {
        Permanent aclazotz = addAclazotzReady(player2);
        gd.stolenCreatures.put(aclazotz.getId(), player1.getId());
        harness.setHand(player1, List.of(new JoinTheDead()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, aclazotz.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent returned = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(returned.isTransformed()).isTrue();
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The death trigger cannot return Aclazotz after it leaves the graveyard")
    void deathTriggerDoesNotReturnCardRemovedFromGraveyard() {
        Permanent aclazotz = addAclazotzReady(player1);
        harness.setHand(player2, List.of(new JoinTheDead()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.inMutationScope(() -> {
            harness.castInstant(player2, 0, aclazotz.getId());
            harness.getStackResolutionService().resolveTopOfStack(gd);
        });
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aclazotz.getCard());
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(aclazotz.getCard()));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(aclazotz.getCard());
    }

    private Permanent addAclazotzReady(Player player) {
        return addCreatureReady(player, new AclazotzDeepestBetrayal());
    }

    private Permanent addTempleReady(Player player) {
        AclazotzDeepestBetrayal card = new AclazotzDeepestBetrayal();
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
