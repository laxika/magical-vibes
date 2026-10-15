package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.c.CloudSprite;
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

@CardUsed({FavoredEnemy.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class,
        Bitterblossom.class, CloudSprite.class})
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

    @Test
    void nonmatchingOpponentCreatureDeathDoesNotGrantACounter() {
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingElf = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new LlanowarElves()));
        harness.setHand(player1, List.of(new FavoredEnemy()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, List.of(hillGiant.getId(), opposingElf.getId()));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(hillGiant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ownMatchingCreatureDeathDoesNotGrantACounter() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new LlanowarElves()));
        harness.setHand(player1, List.of(new FavoredEnemy()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, List.of(ownBears.getId(), opposingGiant.getId()));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canBeCastWithoutAnyCreaturesOnTheBattlefield() {
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new LlanowarElves()));
        harness.setHand(player1, List.of(new FavoredEnemy()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Favored Enemy");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void kindredCardsContributeToTheMostPrevalentCreatureType() {
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingFaerie = harness.addToBattlefieldAndReturn(player2, new CloudSprite());
        harness.setLibrary(player2, List.of(new Bitterblossom(), new Bitterblossom(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new FavoredEnemy()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, List.of(hillGiant.getId(), opposingFaerie.getId()));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Cloud Sprite");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, hillGiant.getId());
        harness.passBothPriorities();

        assertThat(hillGiant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
