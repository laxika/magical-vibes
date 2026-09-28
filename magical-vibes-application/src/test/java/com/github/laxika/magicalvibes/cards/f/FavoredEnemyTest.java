package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FavoredEnemy.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class})
class FavoredEnemyTest extends BaseCardTest {

    @Test
    void notesMostPrevalentCreatureTypeAndCountersAfterMatchingCreatureDies() {
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new LlanowarElves()));
        harness.setHand(player1, List.of(new FavoredEnemy()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, List.of(hillGiant.getId(), opposingBears.getId()));
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Favored Enemy").getChosenSubtype())
                .isEqualTo(CardSubtype.BEAR);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, hillGiant.getId());
        harness.passBothPriorities();

        Permanent favoredEnemy = findPermanent(player1, "Favored Enemy");
        assertThat(favoredEnemy.getChosenSubtype()).isEqualTo(CardSubtype.BEAR);
        assertThat(hillGiant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Grizzly Bears");
    }

    @Test
    void canEnterWithoutAnOpponentCreatureFightTarget() {
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FavoredEnemy()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, hillGiant.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingBears);
        assertThat(gqs.getEffectivePower(gd, hillGiant)).isEqualTo(3);
    }
}
