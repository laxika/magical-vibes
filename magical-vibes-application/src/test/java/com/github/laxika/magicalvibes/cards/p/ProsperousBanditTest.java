package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ProsperousBandit.class)
class ProsperousBanditTest extends BaseCardTest {

    @Test
    void offspringCreatesOneOneTokenCopyWhenPaid() {
        harness.setHand(player1, List.of(new ProsperousBandit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getEffectivePower()).isEqualTo(1);
        assertThat(tokens.getFirst().getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void offspringDoesNotCreateTokenWhenNotPaid() {
        harness.setHand(player1, List.of(new ProsperousBandit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void createsTappedTreasuresEqualToCombatDamageDealt() {
        addCreatureReady(player1, new ProsperousBandit());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(
                permanent -> permanent.getCard().isToken() && permanent.isTapped())
                .hasSize(2);
    }
}
