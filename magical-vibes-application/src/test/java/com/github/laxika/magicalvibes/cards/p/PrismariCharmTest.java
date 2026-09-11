package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrismariCharmTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils 2, then draws a card")
    void surveilsThenDraws() {
        Card toGraveyard = new Forest();
        Card toTop = new Island();
        harness.setLibrary(player1, List.of(toGraveyard, toTop));
        harness.setHand(player1, List.of(new PrismariCharm()));
        addMana();

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(toTop);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(toGraveyard);
    }

    @Test
    @DisplayName("Deals 1 damage to each of two any-targets")
    void damagesTwoTargets() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PrismariCharm()));
        addMana();

        harness.castModalInstant(player1, 0, 1, List.of(spider.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(spider.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage when only one target is chosen")
    void damagesOneTarget() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PrismariCharm()));
        addMana();

        harness.castModalInstant(player1, 0, 1, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Returns a target nonland permanent to its owner's hand")
    void returnsNonlandPermanent() {
        harness.addToBattlefield(player2, new Spellbook());
        harness.setHand(player1, List.of(new PrismariCharm()));
        addMana();

        harness.castModalInstant(player1, 0, 2,
                List.of(harness.getPermanentId(player2, "Spellbook")));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Spellbook");
        harness.assertNotOnBattlefield(player2, "Spellbook");
    }

    @Test
    @DisplayName("Cannot return a land to its owner's hand")
    void cannotReturnLand() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new PrismariCharm()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 2,
                List.of(harness.getPermanentId(player2, "Island"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
