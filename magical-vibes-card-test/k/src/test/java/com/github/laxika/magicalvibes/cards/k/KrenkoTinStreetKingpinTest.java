package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.o.ObNixilissCruelty;
import com.github.laxika.magicalvibes.cards.t.TotallyLost;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrenkoTinStreetKingpin.class, GiantGrowth.class, TotallyLost.class, ObNixilissCruelty.class})
class KrenkoTinStreetKingpinTest extends BaseCardTest {

    @Test
    void attackingPutsCounterOnKrenkoAndCreatesTokensEqualToItsNewPower() {
        Permanent krenko = addCreatureReady(player1, new KrenkoTinStreetKingpin());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(krenko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(krenko.getEffectivePower()).isEqualTo(2);
        assertThat(findPermanents(player1, "Goblin")).hasSize(2);
    }

    @Test
    void tokenCountUsesPowerAfterExistingCounters() {
        Permanent krenko = addCreatureReady(player1, new KrenkoTinStreetKingpin());
        krenko.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(krenko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(krenko.getEffectivePower()).isEqualTo(4);
        assertThat(findPermanents(player1, "Goblin")).hasSize(4);
    }

    @Test
    void powerBoostInResponseIsIncludedInTokenCount() {
        Permanent krenko = addCreatureReady(player1, new KrenkoTinStreetKingpin());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, krenko.getId());
        resolveAllTriggers();

        assertThat(krenko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Goblin")).hasSize(5);
        assertThat(findPermanents(player2, "Goblin")).isEmpty();
        assertThat(findPermanents(player1, "Goblin")).allSatisfy(token -> {
            assertThat(token.isTapped()).isFalse();
            assertThat(token.isAttacking()).isFalse();
        });
    }

    @Test
    void removedKrenkoUsesPowerWhenItLeftWithoutAddingAnotherCounter() {
        Permanent krenko = addCreatureReady(player1, new KrenkoTinStreetKingpin());
        krenko.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new TotallyLost()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, krenko.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(krenko);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin")).hasSize(3);
        assertThat(findPermanents(player2, "Goblin")).isEmpty();
    }

    @Test
    void negativeLastKnownPowerCreatesNoTokens() {
        Permanent krenko = addCreatureReady(player1, new KrenkoTinStreetKingpin());
        harness.setHand(player1, List.of(new ObNixilissCruelty()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, krenko.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(krenko);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin")).isEmpty();
    }
}
