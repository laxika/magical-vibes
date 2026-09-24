package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.m.MindStone;
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
        MindStone.class, ThoughtKnotSeer.class})
class CalamityOfTheTitansTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles creatures and planeswalkers below the revealed card's mana value")
    void exilesQualifyingCreaturesAndPlaneswalkers() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
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
        harness.setHand(player1, List.of(new CalamityOfTheTitans(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castSorceryWithDiscard(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Revealed card must be colorless creature");
    }
}
