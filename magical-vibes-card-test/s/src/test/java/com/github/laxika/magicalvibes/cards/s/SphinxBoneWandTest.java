package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FlameSlash;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SphinxBoneWand.class, Staggershock.class, FlameSlash.class, Naturalize.class, SporecapSpider.class})
class SphinxBoneWandTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the trigger adds a charge counter and deals damage equal to its counters")
    void acceptsTriggerAndDealsDamage() {
        Permanent wand = addReadyWand();
        harness.setHand(player1, List.of(new Staggershock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(wand.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Declining the trigger does not add a counter or deal damage")
    void declinesTrigger() {
        Permanent wand = addReadyWand();
        harness.setHand(player1, List.of(new Staggershock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(wand.getCounterCount(CounterType.CHARGE)).isZero();
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Damage includes the charge counter put on by the same trigger")
    void damageIncludesNewChargeCounter() {
        Permanent wand = addReadyWand();
        wand.setCounterCount(CounterType.CHARGE, 2);
        harness.setHand(player1, List.of(new Staggershock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(wand.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("A sorcery triggers the Wand and its damage can target a creature")
    void sorceryTriggersDamageToCreature() {
        Permanent wand = addReadyWand();
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new SporecapSpider());
        harness.setHand(player1, List.of(new FlameSlash()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, spider.getId());
        harness.handlePermanentChosen(player1, spider.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(wand.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(spider.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Sporecap Spider");
    }

    @Test
    @DisplayName("Casting a creature does not trigger the Wand")
    void creatureDoesNotTrigger() {
        Permanent wand = addReadyWand();

        harness.castFromHand(player1, new SporecapSpider(), "{2}{G}");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(wand.getCounterCount(CounterType.CHARGE)).isZero();
        harness.assertOnBattlefield(player1, "Sporecap Spider");
    }

    @Test
    @DisplayName("An opponent's instant does not trigger the Wand")
    void opponentsInstantDoesNotTrigger() {
        Permanent wand = addReadyWand();
        harness.setHand(player2, List.of(new Staggershock()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(wand.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Removing the Wand in response prevents its conditional damage")
    void removedWandDoesNotDealDamage() {
        Permanent wand = addReadyWand();
        wand.setCounterCount(CounterType.CHARGE, 2);
        harness.setHand(player1, List.of(new Staggershock()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, wand.getId());
        harness.assertInGraveyard(player1, "Sphinx-Bone Wand");

        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An illegal damage target prevents the charge counter as well")
    void illegalTargetPreventsCounter() {
        Permanent wand = addReadyWand();
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new SporecapSpider());
        spider.setMarkedDamage(3);
        harness.setHand(player1, List.of(new Staggershock()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, spider.getId());

        harness.setHand(player2, List.of(new Staggershock()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, spider.getId());
        harness.assertInGraveyard(player2, "Sporecap Spider");
        harness.passBothPriorities();

        assertThat(wand.getCounterCount(CounterType.CHARGE)).isZero();
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    private Permanent addReadyWand() {
        return harness.addToBattlefieldAndReturn(player1, new SphinxBoneWand());
    }
}
