package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.t.ThoughtKnotSeer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CalamityOfTheTitans.class, AirElemental.class, GrizzlyBears.class, JaceBeleren.class,
        MindStone.class, Ornithopter.class, ThoughtKnotSeer.class})
class CalamityOfTheTitansTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles creatures and planeswalkers below the revealed card's mana value")
    void exilesQualifyingCreaturesAndPlaneswalkers() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent jace = harness.enterBattlefieldAndReturn(player2, new JaceBeleren());
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent mindStone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Card revealed = new ThoughtKnotSeer();
        harness.setHand(player1, List.of(new CalamityOfTheTitans(), revealed));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(jace);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(airElemental);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mindStone);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
    }

    @Test
    @DisplayName("Requires revealing a colorless creature card")
    void requiresColorlessCreatureCard() {
        harness.setHand(player1, List.of(new CalamityOfTheTitans(), new GrizzlyBears(), new ThoughtKnotSeer()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castSorceryWithDiscard(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Revealed card must be colorless creature card");
    }

    @Test
    @DisplayName("Creatures at the revealed mana value remain, and lower-value cards go to exile")
    void strictCutoffAndExileDestination() {
        Permanent equal = harness.addToBattlefieldAndReturn(player2, new ThoughtKnotSeer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent jace = harness.enterBattlefieldAndReturn(player2, new JaceBeleren());
        Card revealed = new ThoughtKnotSeer();
        harness.setHand(player1, List.of(revealed, new CalamityOfTheTitans()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorceryWithDiscard(player1, 1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(equal).doesNotContain(jace);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(jace.getCard().getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Jace Beleren");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
    }

    @Test
    @DisplayName("Revealing a zero-mana creature exiles nothing")
    void zeroManaValueExilesNothing() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent jace = harness.enterBattlefieldAndReturn(player2, new JaceBeleren());
        Card revealed = new Ornithopter();
        harness.setHand(player1, List.of(new CalamityOfTheTitans(), revealed));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(thopter);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears, jace);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
        harness.assertInGraveyard(player1, "Calamity of the Titans");
    }

    @Test
    @DisplayName("A colorless noncreature cannot pay the reveal cost")
    void rejectsColorlessNoncreature() {
        harness.setHand(player1, List.of(new CalamityOfTheTitans(), new MindStone(), new ThoughtKnotSeer()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castSorceryWithDiscard(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Revealed card must be colorless creature card");
    }

    @Test
    @DisplayName("The additional reveal cost cannot be omitted")
    void requiresRevealEvenWithEnoughMana() {
        harness.setHand(player1, List.of(new CalamityOfTheTitans(), new ThoughtKnotSeer()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
