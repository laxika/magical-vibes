package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoralBarrier.class, Island.class})
class CoralBarrierTest extends BaseCardTest {

    @Test
    @DisplayName("When Coral Barrier enters, it creates an islandwalking Squid token")
    void etbCreatesIslandwalkingSquidToken() {
        harness.setHand(player1, List.of(new CoralBarrier()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> squids = findPermanents(player1, "Squid");
        assertThat(squids).hasSize(1);
        assertThat(gqs.hasKeyword(gd, squids.getFirst(), Keyword.ISLANDWALK)).isTrue();
    }

    @Test
    void tokenIsCreatedOnlyWhenEnterTriggerResolves() {
        harness.setHand(player1, List.of(new CoralBarrier()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);

        assertThat(findPermanents(player1, "Squid")).isEmpty();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Coral Barrier")).hasSize(1);
        assertThat(findPermanents(player1, "Squid")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        Permanent squid = findPermanent(player1, "Squid");
        assertThat(gqs.getEffectivePower(gd, squid)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, squid)).isEqualTo(1);
        assertThat(squid.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(squid.getCard().getSubtypes()).containsExactly(CardSubtype.SQUID);
        assertThat(squid.isTapped()).isFalse();
        assertThat(findPermanents(player2, "Squid")).isEmpty();
    }

    @Test
    void enteringWithoutCastingCreatesTokenForController() {
        harness.enterBattlefieldAndReturn(player2, new CoralBarrier());
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Squid")).hasSize(1);
        assertThat(findPermanents(player1, "Squid")).isEmpty();
    }

    @Test
    void defenderCannotAttackButCanBlock() {
        Permanent barrier = addCreatureReady(player1, new CoralBarrier());

        assertThat(als.canAttack(gd, barrier, player1.getId())).isFalse();
        assertThat(bls.canBlock(gd, barrier)).isTrue();
    }

    @Test
    void squidIslandwalkDependsOnDefendingPlayersIslands() {
        harness.enterBattlefieldAndReturn(player1, new CoralBarrier());
        resolveAllTriggers();
        Permanent squid = findPermanent(player1, "Squid");
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new CoralBarrier());
        harness.addToBattlefield(player1, new Island());

        assertThat(bls.canBlockAttacker(gd, blocker, squid,
                gd.playerBattlefields.get(player2.getId()))).isTrue();

        harness.addToBattlefield(player2, new Island());

        assertThat(bls.canBlockAttacker(gd, blocker, squid,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }
}
