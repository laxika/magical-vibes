package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.Crusade;
import com.github.laxika.magicalvibes.cards.b.BarterInBlood;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.IronStar;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        harness.setHand(player1, List.of(new BarterInBlood()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, 0);
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

    private Permanent addReadyLiliana(Player player, int loyalty) {
        Permanent permanent = new Permanent(new LilianaDreadhordeGeneral());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
