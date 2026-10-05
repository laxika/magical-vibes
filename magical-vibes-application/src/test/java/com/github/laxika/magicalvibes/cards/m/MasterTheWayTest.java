package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SarkhanTheDragonspeaker;
import com.github.laxika.magicalvibes.cards.w.Waterwhirl;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MasterTheWay.class, Plains.class, Island.class, AlpineGrizzly.class,
        SarkhanTheDragonspeaker.class, Waterwhirl.class})
class MasterTheWayTest extends BaseCardTest {

    @Test
    void drawsBeforeDealingDamageEqualToResultingHandSize() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MasterTheWay(), new Plains(), new Island()));
        harness.setLibrary(player1, List.of(new Plains()));
        addMana(player1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void dealsDamageToAcreatureTarget() {
        harness.addToBattlefield(player2, new AlpineGrizzly());
        harness.setHand(player1, List.of(new MasterTheWay(), new Plains()));
        harness.setLibrary(player1, List.of(new Island()));
        addMana(player1);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Alpine Grizzly"));

        harness.assertInGraveyard(player2, "Alpine Grizzly");
    }

    @Test
    void dealsOneDamageWhenCastAsTheOnlyCardInHand() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new MasterTheWay()));
        harness.setLibrary(player1, List.of(new Plains()));
        addMana(player1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 19);
    }

    @Test
    void dealsDamageToAPlaneswalker() {
        harness.addToBattlefield(player2, new SarkhanTheDragonspeaker());
        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(new MasterTheWay(), new Plains()));
        harness.setLibrary(player1, List.of(new Island()));
        addMana(player1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Sarkhan, the Dragonspeaker");
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void countsCardsReturnedToHandBeforeResolution() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AlpineGrizzly());
        harness.setHand(player1, List.of(new MasterTheWay(), new Waterwhirl(), new Plains()));
        harness.setLibrary(player1, List.of(new Island()));
        addMana(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0,
                List.of(harness.getPermanentId(player1, "Alpine Grizzly")));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInHand(player1, "Alpine Grizzly");
        harness.assertLife(player2, 17);
    }

    @Test
    void doesNotDrawWhenItsOnlyTargetLeavesTheBattlefield() {
        harness.addToBattlefield(player2, new AlpineGrizzly());
        var targetId = harness.getPermanentId(player2, "Alpine Grizzly");
        harness.setHand(player1, List.of(new MasterTheWay(), new Waterwhirl(), new Plains()));
        Island topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        addMana(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, targetId);
        harness.castAndResolveInstant(player1, 0, List.of(targetId));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertInHand(player2, "Alpine Grizzly");
        harness.assertInGraveyard(player1, "Master the Way");
    }

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }
}
