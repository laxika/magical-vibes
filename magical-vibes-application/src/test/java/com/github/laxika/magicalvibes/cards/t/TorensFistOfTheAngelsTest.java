package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TorensFistOfTheAngelsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a creature spell creates a 1/1 Human Soldier token")
    void creatureSpellCreatesToken() {
        addReadyTorens(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Human Soldier");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a noncreature spell creates no token")
    void noncreatureSpellCreatesNoToken() {
        addReadyTorens(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.setHand(player1, List.of(new GloriousAnthem()));

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Human Soldier")).isZero();
    }

    @Test
    @DisplayName("An opponent's creature spell creates no token")
    void opponentCreatureSpellCreatesNoToken() {
        addReadyTorens(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Human Soldier")).isZero();
        assertThat(countPermanents(player2, "Human Soldier")).isZero();
    }

    @Test
    @DisplayName("The token has training")
    void tokenHasTraining() {
        addReadyTorens(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Human Soldier");
        token.setSummoningSick(false);
        Permanent giant = addCreatureReady(player1, new HillGiant());

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        declareAttackers(List.of(battlefield.indexOf(token), battlefield.indexOf(giant)));
        harness.passBothPriorities();

        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Torens itself trains when attacking with a greater-power creature")
    void torensTrains() {
        Permanent torens = addReadyTorens(player1);
        addCreatureReady(player1, new HillGiant());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(torens.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addReadyTorens(Player player) {
        return addCreatureReady(player, new TorensFistOfTheAngels());
    }
}
