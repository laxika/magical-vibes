package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarWastes;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorneredMarketTest extends BaseCardTest {

    @Test
    @DisplayName("Players can't cast spells sharing a name with a nontoken permanent")
    void preventsSpellsWithNontokenPermanentNames() {
        addReadyCorneredMarket(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Token names do not create a Cornered Market restriction")
    void ignoresTokenNames() {
        addReadyCorneredMarket(player1);
        harness.addToBattlefield(player1, tokenNamed("Grizzly Bears"));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cornered Market prevents matching nonbasic lands but not basic lands")
    void restrictsNonbasicLandsOnly() {
        addReadyCorneredMarket(player1);
        harness.addToBattlefield(player1, new LlanowarWastes());
        harness.addToBattlefield(player1, new Plains());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LlanowarWastes()));

        assertThatThrownBy(() -> harness.playLand(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.setHand(player2, List.of(new Plains()));
        harness.playLand(player2, 0);

        harness.assertOnBattlefield(player2, "Plains");
    }

    @Test
    @DisplayName("Names on the stack do not create a Cornered Market restriction")
    void ignoresNamesOnTheStack() {
        addReadyCorneredMarket(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.castInstant(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(2);
    }

    private Permanent addReadyCorneredMarket(Player player) {
        return harness.addToBattlefieldAndReturn(player, new CorneredMarket());
    }

    private Card tokenNamed(String name) {
        Card token = new Card();
        token.setName(name);
        token.setType(CardType.CREATURE);
        token.setColor(CardColor.GREEN);
        token.setPower(1);
        token.setToughness(1);
        token.setToken(true);
        return token;
    }
}
