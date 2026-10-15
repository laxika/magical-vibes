package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanguinaryPriest.class, GrizzlyBears.class, Shock.class, Murder.class})
class SanguinaryPriestTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to any target when another creature you control dies")
    void alliedCreatureDeathDealsDamageToTargetPlayer() {
        harness.addToBattlefield(player1, new SanguinaryPriest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        harness.setHand(player1, java.util.List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger when Sanguinary Priest itself dies")
    void selfDeathDoesNotTrigger() {
        Permanent priest = harness.addToBattlefieldAndReturn(player1, new SanguinaryPriest());
        harness.setLife(player2, 20);

        harness.setHand(player1, java.util.List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, priest.getId());

        harness.assertInGraveyard(player1, "Sanguinary Priest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Blood Chalice can damage a creature and gains life through lifelink")
    void creatureTargetAndLifelink() {
        harness.addToBattlefield(player1, new SanguinaryPriest());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.setHand(player1, java.util.List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, ally.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 11);
    }

    @Test
    @DisplayName("An opposing creature dying does not trigger Blood Chalice")
    void opposingCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new SanguinaryPriest());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setHand(player1, java.util.List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, opponent.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Blood Chalice still deals damage with lifelink after its source dies")
    void triggerResolvesAfterSourceDies() {
        Permanent priest = harness.addToBattlefieldAndReturn(player1, new SanguinaryPriest());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setHand(player1, java.util.List.of(new Shock(), new Murder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, ally.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.castAndResolveInstant(player1, 0, priest.getId());
        harness.assertInGraveyard(player1, "Sanguinary Priest");
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 11);
    }

    @Test
    @DisplayName("Blood Chalice gains no life when its only target becomes illegal")
    void removedTargetPreventsDamageAndLifelink() {
        harness.addToBattlefield(player1, new SanguinaryPriest());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.setHand(player1, java.util.List.of(new Shock(), new Murder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, ally.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 10);
        assertThat(gd.stack).isEmpty();
    }
}
