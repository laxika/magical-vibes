package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NecrogenCommunionTest extends BaseCardTest {

    @Test
    void enchantedCreatureHasToxicTwoAndGivesTwoPoisonCountersOnCombatDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castNecrogenCommunion(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TOXIC)).isTrue();

        creature.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    void returnsEnchantedCreatureToBattlefieldUnderAuraControllersControl() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Card creatureCard = creature.getCard();
        castNecrogenCommunion(player1, creature);

        killCreature(creature);

        Permanent returned = findPermanent(player1, creatureCard);
        assertThat(returned).isNotNull();
        assertThat(returned.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    void canEnchantOnlyACreatureYouControl() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NecrogenCommunion()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castNecrogenCommunion(Player controller, Permanent target) {
        harness.setHand(controller, List.of(new NecrogenCommunion()));
        harness.addMana(controller, ManaColor.BLACK, 2);
        harness.castEnchantment(controller, 0, target.getId());
        harness.passBothPriorities();
    }

    private void killCreature(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent findPermanent(Player player, Card card) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElse(null);
    }
}
