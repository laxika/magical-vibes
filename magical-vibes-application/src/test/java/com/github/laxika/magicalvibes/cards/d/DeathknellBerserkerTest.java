package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeathknellBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 2/2 black Zombie Berserker when it dies with power 3 or greater")
    void createsZombieBerserkerWhenItsPowerIsAtLeastThree() {
        Permanent berserker = harness.addToBattlefieldAndReturn(player1, new DeathknellBerserker());
        berserker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        destroyWithMurder(player2, player1, berserker.getId());
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Zombie").getFirst();
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE, CardSubtype.BERSERKER);
    }

    @Test
    @DisplayName("Does not create a token when it dies with power less than 3")
    void doesNotCreateTokenWhenItsPowerIsLessThanThree() {
        Permanent berserker = harness.addToBattlefieldAndReturn(player1, new DeathknellBerserker());

        destroyWithMurder(player2, player1, berserker.getId());

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    private void destroyWithMurder(Player caster, Player targetController, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.addMana(caster, ManaColor.COLORLESS, 1);

        gs.playCard(gd, caster, 0, 0, targetId, null);
        harness.passBothPriorities();
    }
}
