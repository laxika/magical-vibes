package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EidolonOfRhetoric;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.cards.t.ThrillOfPossibility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AshiokNightmareMuse.class, Forest.class, GrizzlyBears.class,
        EidolonOfRhetoric.class, NyxbornColossus.class, ThrillOfPossibility.class})
class AshiokNightmareMuseTest extends BaseCardTest {

    @Test
    @DisplayName("+1 creates a Nightmare whose attack trigger exiles the top two cards of each opponent's library")
    void plusOneCreatesNightmareWithAttackTrigger() {
        Permanent ashiok = addReadyAshiok(player1, 3);
        Card opponentFirst = new Forest();
        Card opponentSecond = new GrizzlyBears();
        Card ownTop = new Forest();
        harness.setLibrary(player1, List.of(ownTop));
        harness.setLibrary(player2, List.of(opponentFirst, opponentSecond));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent nightmare = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, nightmare)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nightmare)).isEqualTo(3);

        nightmare.setSummoningSick(false);
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(nightmare)));
        resolveCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactly(opponentFirst, opponentSecond);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTop);
        assertThat(ashiok.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("-3 returns a nonland permanent and its owner exiles a card from hand")
    void minusThreeBouncesThenExilesFromOwnerHand() {
        Permanent ashiok = addReadyAshiok(player1, 3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card handCard = new Forest();
        harness.setHand(player2, List.of(handCard));

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ExileFromHandChoice.class))
                .isNotNull();
        harness.handleCardChosen(player2, 0);

        assertThat(ashiok.getCounterCount(CounterType.LOYALTY)).isZero();
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(handCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(handCard);
    }

    @Test
    @DisplayName("-7 offers up to three face-up spells owned by opponents from exile")
    void minusSevenCastsAtMostThreeOpponentOwnedFaceUpSpells() {
        Permanent ashiok = addReadyAshiok(player1, 7);
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Card fourth = new GrizzlyBears();
        Card opponentLand = new Forest();
        Card ownSpell = new GrizzlyBears();
        Card faceDown = new GrizzlyBears();
        gd.addToExile(player2.getId(), first);
        gd.addToExile(player2.getId(), second);
        gd.addToExile(player2.getId(), third);
        gd.addToExile(player2.getId(), fourth);
        gd.addToExile(player2.getId(), opponentLand);
        gd.addToExile(player1.getId(), ownSpell);
        gd.addToExile(player2.getId(), faceDown, null, true);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        PendingInteraction.ImprovisationCapstoneCastChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(choice.validCardIds()).containsExactly(
                first.getId(), second.getId(), third.getId(), fourth.getId());
        assertThat(choice.maxCount()).isEqualTo(3);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(first.getId())
                        || permanent.getCard().getId().equals(second.getId())
                        || permanent.getCard().getId().equals(third.getId())))
                .hasSize(3);
        assertThat(gd.findExiledCard(fourth.getId())).isNotNull();
        assertThat(gd.findExiledCard(opponentLand.getId())).isNotNull();
        assertThat(gd.findExiledCard(ownSpell.getId())).isNotNull();
        assertThat(gd.findExiledCard(faceDown.getId())).isNotNull();
        assertThat(ashiok.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    @DisplayName("-3 cannot target a land")
    void minusThreeCannotTargetLand() {
        addReadyAshiok(player1, 3);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nightmareBlockTriggerExilesShortOpponentLibrary() {
        addReadyAshiok(player1, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent nightmare = findPermanent(player1, "Nightmare");
        addCreatureReady(player2, new NyxbornColossus());
        Card opponentTop = new Forest();
        Card ownTop = new Forest();
        harness.setLibrary(player2, List.of(opponentTop));
        harness.setLibrary(player1, List.of(ownTop));

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(nightmare), 0)));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentTop);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTop);
    }

    @Test
    void minusThreeCanExileReturnedCard() {
        addReadyAshiok(player1, 5);
        Card targetCard = new NyxbornColossus();
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        Card otherCard = new Forest();
        harness.setHand(player2, List.of(otherCard));

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, gd.playerHands.get(player2.getId()).indexOf(targetCard));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(otherCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(targetCard);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void minusThreeBouncesTokenThenExilesRealCard() {
        addReadyAshiok(player1, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent nightmare = findPermanent(player1, "Nightmare");
        addReadyAshiok(player2, 5);
        Card first = new Forest();
        Card second = new Forest();
        harness.setHand(player1, List.of(first, second));

        harness.activateAbility(player2, 0, 1, null, nightmare.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(nightmare);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first);
    }

    @Test
    void minusSevenMayCastNoSpells() {
        addReadyAshiok(player1, 7);
        Card exiled = new NyxbornColossus();
        gd.addToExile(player2.getId(), exiled);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void minusSevenRespectsSpellCastingLimit() {
        addReadyAshiok(player1, 7);
        harness.addToBattlefield(player2, new EidolonOfRhetoric());
        harness.setHand(player1, List.of(new NyxbornColossus()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Card exiled = new NyxbornColossus();
        gd.addToExile(player2.getId(), exiled);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        PendingInteraction.ImprovisationCapstoneCastChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        if (choice != null && choice.validCardIds().contains(exiled.getId())) {
            harness.handleMultipleCardsChosen(player1, List.of(exiled.getId()));
        }
        resolveAllTriggers();

        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
        assertThat(countPermanents(player1, "Nyxborn Colossus")).isEqualTo(1);
    }

    @Test
    void minusSevenAllowsPayableAdditionalCost() {
        addReadyAshiok(player1, 7);
        Card thrill = new ThrillOfPossibility();
        Card discard = new Forest();
        Card otherCard = new Forest();
        gd.addToExile(player2.getId(), thrill);
        harness.setHand(player1, List.of(discard, otherCard));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(thrill.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discard, otherCard);
    }

    private Permanent addReadyAshiok(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AshiokNightmareMuse());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
