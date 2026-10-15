package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AshiokWickedManipulator;
import com.github.laxika.magicalvibes.cards.c.CandyGrapple;
import com.github.laxika.magicalvibes.cards.e.EdgewallPack;
import com.github.laxika.magicalvibes.cards.f.FranticFirebolt;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.h.HarmsWay;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.s.SafePassage;
import com.github.laxika.magicalvibes.cards.u.UpTheBeanstalk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TorchTheTower.class, EdgewallPack.class, PropheticPrism.class, Island.class,
        AshiokWickedManipulator.class, CandyGrapple.class, Gingerbrute.class, UpTheBeanstalk.class,
        SafePassage.class, FranticFirebolt.class, HarmsWay.class})
class TorchTheTowerTest extends BaseCardTest {

    @Test
    void dealsTwoDamageAndExilesTheCreatureIfItDiesLaterThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EdgewallPack());
        harness.setHand(player1, List.of(new TorchTheTower(), new FranticFirebolt()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Edgewall Pack");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Edgewall Pack");
        harness.assertNotInGraveyard(player2, "Edgewall Pack");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    void bargainDealsThreeDamageExilesTheCreatureAndScries() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EdgewallPack());
        Island topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new TorchTheTower()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);

        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertNotOnBattlefield(player2, "Edgewall Pack");
        harness.assertNotInGraveyard(player2, "Edgewall Pack");
        harness.assertInGraveyard(player1, "Prophetic Prism");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    void cannotTargetAPlayer() {
        harness.setHand(player1, List.of(new TorchTheTower()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void dealsDamageToPlaneswalkerAndExilesItAtZeroLoyalty() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshiokWickedManipulator());
        target.setCounterCount(CounterType.LOYALTY, 5);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new TorchTheTower(), new TorchTheTower()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Ashiok, Wicked Manipulator");

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertNotOnBattlefield(player2, "Ashiok, Wicked Manipulator");
        harness.assertNotInGraveyard(player2, "Ashiok, Wicked Manipulator");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    void canBargainBySacrificingAnEnchantment() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new UpTheBeanstalk());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EdgewallPack());
        Island topCard = new Island();
        Gingerbrute secondCard = new Gingerbrute();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new TorchTheTower()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.assertInGraveyard(player1, "Up the Beanstalk");
        harness.assertOnBattlefield(player2, "Edgewall Pack");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        harness.assertNotInGraveyard(player2, "Edgewall Pack");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, topCard);
    }

    @Test
    void canBargainBySacrificingANonartifactNonenchantmentToken() {
        harness.castFromHand(player1, new EdgewallPack(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Rat"))
                .findFirst().orElseThrow();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EdgewallPack());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new TorchTheTower()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), token.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        harness.assertOnBattlefield(player1, "Edgewall Pack");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    void cannotBargainBySacrificingANontokenCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new EdgewallPack());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EdgewallPack());
        harness.setHand(player1, List.of(new TorchTheTower()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifice(
                player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Edgewall Pack");
        harness.assertInHand(player1, "Torch the Tower");
    }

    @Test
    void cannotBargainBySacrificingAnOpponentsArtifact() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EdgewallPack());
        harness.setHand(player1, List.of(new TorchTheTower()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifice(
                player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Prophetic Prism");
    }

    @Test
    void cannotTargetANoncreatureArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        harness.setHand(player1, List.of(new TorchTheTower()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sacrificingTheTargetToBargainMakesTheSpellFailToResolveWithoutScrying() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        Island topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new TorchTheTower()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gingerbrute");
        harness.assertInGraveyard(player1, "Torch the Tower");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    void preventedDamageDoesNotExileTheCreatureWhenItDiesLater() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EdgewallPack());
        harness.setHand(player2, List.of(new SafePassage()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player2, 0);
        harness.setHand(player1, List.of(new TorchTheTower(), new CandyGrapple()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Edgewall Pack");
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    void bargainedSpellStillScriesWhenAllDamageIsPrevented() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EdgewallPack());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        harness.setHand(player2, List.of(new SafePassage()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player2, 0);
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new TorchTheTower()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Edgewall Pack");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    void exilesThePermanentActuallyDealtRedirectedDamage() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new EdgewallPack());
        Permanent redirectTarget = harness.addToBattlefieldAndReturn(player2, new Gingerbrute());
        TorchTheTower torch = new TorchTheTower();
        harness.setHand(player2, List.of(torch));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, originalTarget.getId());
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, redirectTarget.getId());
        harness.handlePermanentChosen(player1, torch.getId());
        harness.passBothPriorities();

        assertThat(originalTarget.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Edgewall Pack");
        harness.assertNotOnBattlefield(player2, "Gingerbrute");
        harness.assertNotInGraveyard(player2, "Gingerbrute");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(redirectTarget.getCard().getId()));
    }

    @Test
    void exileReplacementExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EdgewallPack());
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new TorchTheTower()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CandyGrapple()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Edgewall Pack");
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    void exilesADamagedCreatureThatDiesFromReducedToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EdgewallPack());
        harness.setHand(player1, List.of(new TorchTheTower(), new CandyGrapple()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Edgewall Pack");
        harness.assertNotInGraveyard(player2, "Edgewall Pack");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }
}
