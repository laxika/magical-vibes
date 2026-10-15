package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.h.Hushbringer;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnholyIndenture.class, GreenwoodSentinel.class, Murder.class, Unsummon.class,
        GrafdiggersCage.class, RaiseTheAlarm.class, Hushbringer.class})
class UnholyIndentureTest extends BaseCardTest {

    @Test
    void returnsEnchantedCreatureUnderAuraControllersControlWithCounter() {
        Permanent creature = addCreatureReady(player2, new GreenwoodSentinel());
        Card creatureCard = creature.getCard();

        castUnholyIndenture(player1, creature);
        killCreature(creature);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(creatureCard.getId()))
                .findFirst()
                .orElse(null);
        assertThat(returned).isNotNull();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
    }

    @Test
    void auraGoesToGraveyardWhenEnchantedCreatureDies() {
        Permanent creature = addCreatureReady(player2, new GreenwoodSentinel());

        castUnholyIndenture(player1, creature);
        killCreature(creature);

        harness.assertInGraveyard(player1, "Unholy Indenture");
        harness.assertNotOnBattlefield(player1, "Unholy Indenture");
    }

    @Test
    void cannotEnchantNonCreaturePermanent() {
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new GrafdiggersCage());

        harness.setHand(player1, List.of(new UnholyIndenture()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ownCreatureReturnsUntappedWithOnlyTheNewCounter() {
        Permanent creature = addCreatureReady(player1, new GreenwoodSentinel());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        creature.tap();
        castUnholyIndenture(player1, creature);

        killCreature(creature);

        Permanent returned = findPermanent(player1, "Greenwood Sentinel");
        assertThat(returned.getId()).isNotEqualTo(creature.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.isSummoningSick()).isTrue();
        harness.assertNotInGraveyard(player1, "Greenwood Sentinel");
    }

    @Test
    void stolenCreatureStillReturnsToItsOwnersHandWhenBounced() {
        Permanent creature = addCreatureReady(player2, new GreenwoodSentinel());
        castUnholyIndenture(player1, creature);
        killCreature(creature);

        Permanent returned = findPermanent(player1, "Greenwood Sentinel");
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, returned.getId());

        harness.assertInHand(player2, "Greenwood Sentinel");
        harness.assertNotOnBattlefield(player1, "Greenwood Sentinel");
        harness.assertNotOnBattlefield(player2, "Greenwood Sentinel");
    }

    @Test
    void bouncingEnchantedCreatureDoesNotTriggerReturn() {
        Permanent creature = addCreatureReady(player2, new GreenwoodSentinel());
        castUnholyIndenture(player1, creature);
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Greenwood Sentinel");
        harness.assertNotOnBattlefield(player1, "Greenwood Sentinel");
        harness.assertInGraveyard(player1, "Unholy Indenture");
    }

    @Test
    void creatureRemainsInItsOwnersGraveyardWhenCagePreventsReturn() {
        harness.addToBattlefield(player1, new GrafdiggersCage());
        Permanent creature = addCreatureReady(player2, new GreenwoodSentinel());
        castUnholyIndenture(player1, creature);

        killCreature(creature);

        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        harness.assertNotInGraveyard(player1, "Greenwood Sentinel");
        harness.assertNotOnBattlefield(player1, "Greenwood Sentinel");
        harness.assertNotOnBattlefield(player2, "Greenwood Sentinel");
    }

    @Test
    void olderReturnTriggerCannotReturnCreatureAfterItReturnsAndDiesAgain() {
        Permanent creature = addCreatureReady(player2, new GreenwoodSentinel());
        castUnholyIndenture(player1, creature);
        castUnholyIndenture(player1, creature);
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Greenwood Sentinel");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, returned.getId());
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        harness.assertNotOnBattlefield(player1, "Greenwood Sentinel");
        harness.assertNotOnBattlefield(player2, "Greenwood Sentinel");
    }

    @Test
    void enchantedTokenDoesNotReturn() {
        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);
        Permanent token = findPermanents(player1, "Soldier").getFirst();
        castUnholyIndenture(player1, token);

        killCreature(token);

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Soldier");
        harness.assertInGraveyard(player1, "Unholy Indenture");
    }

    @Test
    void nonactivePlayersAuraReturnsCreatureFirst() {
        Permanent creature = addCreatureReady(player1, new GreenwoodSentinel());
        castUnholyIndenture(player1, creature);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castUnholyIndenture(player2, creature);

        killCreature(creature);
        resolveAllTriggers();

        Permanent returned = findPermanent(player2, "Greenwood Sentinel");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Greenwood Sentinel");
        harness.assertInGraveyard(player1, "Unholy Indenture");
        harness.assertInGraveyard(player2, "Unholy Indenture");
    }

    @Test
    void hushbringerPreventsEnchantedCreatureDeathTrigger() {
        harness.addToBattlefield(player1, new Hushbringer());
        Permanent creature = addCreatureReady(player2, new GreenwoodSentinel());
        castUnholyIndenture(player1, creature);

        killCreature(creature);

        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        harness.assertNotOnBattlefield(player1, "Greenwood Sentinel");
        harness.assertNotOnBattlefield(player2, "Greenwood Sentinel");
    }

    private void castUnholyIndenture(Player controller, Permanent target) {
        harness.setHand(controller, List.of(new UnholyIndenture()));
        harness.addMana(controller, ManaColor.BLACK, 3);

        harness.castEnchantment(controller, 0, target.getId());
        harness.passBothPriorities();
    }

    private void killCreature(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
