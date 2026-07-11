package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LashOutTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private void prepare() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LashOut()));
        harness.addMana(player1, ManaColor.RED, 2); // {1}{R}
        harness.setLife(player2, 20);
    }

    // Caster (player1) wins the clash: their revealed top card (MV 2) beats the opponent's Forest (MV 0).
    private void stackClashWinForCaster() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
    }

    // Caster (player1) loses the clash: the opponent reveals the higher mana value.
    private void stackClashLossForCaster() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest(), new Forest()));
    }

    @Test
    @DisplayName("Winning the clash deals 3 to the creature and 3 to its controller")
    void wonClashDealsDamageToControllerCreatureSurvives() {
        prepare();
        harness.addToBattlefield(player2, createCreature("Large Beast", 4, 5));
        stackClashWinForCaster();

        UUID targetId = harness.getPermanentId(player2, "Large Beast");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // 4/5 survives 3 marked damage, and its controller takes the clash-win 3 damage.
        harness.assertOnBattlefield(player2, "Large Beast");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Winning the clash still damages the controller when the creature dies to the 3 damage")
    void wonClashDamagesControllerWhenCreatureDies() {
        prepare();
        harness.addToBattlefield(player2, new GrizzlyBears());
        stackClashWinForCaster();

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // 2/2 dies to the 3 damage, but its controller still takes 3 (last-known controller).
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Losing the clash deals 3 to the creature but nothing to its controller")
    void lostClashDealsNoDamageToController() {
        prepare();
        harness.addToBattlefield(player2, new GrizzlyBears());
        stackClashLossForCaster();

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Creature still takes the 3 damage and dies; controller is untouched on a loss.
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        prepare();
        harness.addToBattlefield(player1, new GrizzlyBears()); // valid target so spell is castable
        harness.addToBattlefield(player2, new Forest());

        UUID landId = harness.getPermanentId(player2, "Forest");

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class);
    }
}
