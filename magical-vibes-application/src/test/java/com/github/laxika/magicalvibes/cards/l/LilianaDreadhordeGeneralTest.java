package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.Crusade;
import com.github.laxika.magicalvibes.cards.b.BarterInBlood;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.IronStar;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SarkhanTheMasterless;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LilianaDreadhordeGeneral.class, BarterInBlood.class, Crusade.class,
        GrizzlyBears.class, HillGiant.class, IronStar.class, Juggernaut.class,
        LilianaOfTheVeil.class, Millstone.class, Plains.class, SarkhanTheMasterless.class})
class LilianaDreadhordeGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("+1 creates a 2/2 black Zombie token")
    void plusOneCreatesZombie() {
        Permanent liliana = addReadyLiliana(player1, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The death trigger draws for each creature you control that dies")
    void drawsForEachAllyCreatureDeath() {
        addReadyLiliana(player1, 6);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        Card firstDraw = new Plains();
        Card secondDraw = new IronStar();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        harness.castFromHand(player1, new BarterInBlood(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Hill Giant");
        assertThat(gd.playerHands.get(player1.getId())).contains(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("-9 lets each opponent keep one permanent of each permanent type and sacrifices the rest")
    void minusNineAffectsOpponentsOnly() {
        addReadyLiliana(player1, 9);
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent keptArtifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent sacrificedArtifact = harness.addToBattlefieldAndReturn(player2, new IronStar());
        Permanent keptCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent sacrificedCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new Crusade());
        Permanent keptLand = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent sacrificedLand = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new LilianaOfTheVeil());
        planeswalker.setCounterCount(CounterType.LOYALTY, 1);
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new Juggernaut());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player2, List.of(keptArtifact.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(keptCreature.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(keptLand.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownPermanent);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(keptArtifact, keptCreature, enchantment, keptLand, planeswalker)
                .doesNotContain(sacrificedArtifact, sacrificedCreature, sacrificedLand, artifactCreature);
        harness.assertInGraveyard(player2, "Iron Star");
        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(card -> card.getName().equals("Plains"));
    }

    @Test
    @DisplayName("-4 sacrifices all available creatures when each player has fewer than two")
    void minusFourSacrificesAvailableCreatures() {
        Permanent liliana = addReadyLiliana(player1, 6);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new Juggernaut());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new Juggernaut());
        Card draw = new Plains();
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }

    @Test
    @DisplayName("A Zombie token dying triggers Liliana's draw ability")
    void drawsWhenHerZombieTokenDies() {
        addReadyLiliana(player1, 6);
        Card draw = new Plains();
        harness.setLibrary(player1, List.of(draw));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.castFromHand(player1, new BarterInBlood(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }

    @Test
    @DisplayName("Animated Liliana draws for herself and another creature dying simultaneously")
    void animatedLilianaDrawsForHerOwnDeath() {
        addReadyLiliana(player1, 6);
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player1, new SarkhanTheMasterless());
        sarkhan.setCounterCount(CounterType.LOYALTY, 5);
        Card firstDraw = new Plains();
        Card secondDraw = new Plains();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.castFromHand(player1, new BarterInBlood(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Liliana, Dreadhorde General");
        harness.assertInGraveyard(player1, "Sarkhan the Masterless");
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("-9 allows the same artifact creature to be kept for both permanent types")
    void minusNineKeepsSamePermanentForMultipleTypes() {
        addReadyLiliana(player1, 9);
        Permanent kept = harness.addToBattlefieldAndReturn(player2, new Juggernaut());
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(kept.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(kept.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(kept).doesNotContain(otherArtifact, otherCreature);
        harness.assertInGraveyard(player2, "Millstone");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("-4 lets both players choose two creatures before sacrificing them simultaneously")
    void minusFourWaitsForBothPlayersChoices() {
        addReadyLiliana(player1, 6);
        Permanent ownFirst = harness.addToBattlefieldAndReturn(player1, new Juggernaut());
        Permanent ownSecond = harness.addToBattlefieldAndReturn(player1, new Juggernaut());
        Permanent ownKept = harness.addToBattlefieldAndReturn(player1, new Juggernaut());
        Permanent opponentFirst = harness.addToBattlefieldAndReturn(player2, new Juggernaut());
        Permanent opponentSecond = harness.addToBattlefieldAndReturn(player2, new Juggernaut());
        Permanent opponentKept = harness.addToBattlefieldAndReturn(player2, new Juggernaut());
        Card firstDraw = new Plains();
        Card secondDraw = new Plains();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(ownFirst.getId(), ownSecond.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownFirst, ownSecond, ownKept);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentFirst, opponentSecond, opponentKept);

        harness.handleMultiplePermanentsChosen(player2, List.of(opponentFirst.getId(), opponentSecond.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(ownKept).doesNotContain(ownFirst, ownSecond);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(opponentKept).doesNotContain(opponentFirst, opponentSecond);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("-4 still resolves at four loyalty but Liliana no longer observes the deaths")
    void minusFourAtFourLoyaltyDoesNotDraw() {
        addReadyLiliana(player1, 4);
        harness.addToBattlefield(player1, new Juggernaut());
        harness.addToBattlefield(player2, new Juggernaut());
        Card undrawn = new Plains();
        harness.setLibrary(player1, List.of(undrawn));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Liliana, Dreadhorde General");
        harness.assertInGraveyard(player1, "Juggernaut");
        harness.assertInGraveyard(player2, "Juggernaut");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
    }
    private Permanent addReadyLiliana(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new LilianaDreadhordeGeneral());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
