package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.d.DragonSniper;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JeskaiRevelation.class, Island.class, Shock.class, DragonSniper.class})
class JeskaiRevelationTest extends BaseCardTest {

    @Test
    void returnsAnyPermanentDealsDamageCreatesMonksDrawsAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player1, List.of(new JeskaiRevelation()));
        addJeskaiRevelationMana();

        harness.castInstant(player1, 0, target.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Island");
        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertLife(player2, 16);
        assertThat(countPermanents(player1, "Monk")).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void returnsTargetSpellAndStillResolvesTheOtherEffects() {
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        java.util.UUID shockId = gd.stack.getFirst().getCard().getId();

        harness.setHand(player1, List.of(new JeskaiRevelation()));
        addJeskaiRevelationMana();
        harness.forceActivePlayer(player1);
        harness.castInstant(player1, 0, shockId, player2.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Shock");
        harness.assertNotInGraveyard(player2, "Shock");
        harness.assertLife(player2, 16);
        assertThat(countPermanents(player1, "Monk")).isEqualTo(2);
    }

    @Test
    void createdMonksHaveProwess() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player1, List.of(new JeskaiRevelation()));
        addJeskaiRevelationMana();

        harness.castInstant(player1, 0, target.getId(), player2.getId());
        harness.passBothPriorities();
        List<Permanent> monks = findPermanents(player1, "Monk");
        assertThat(monks).hasSize(2);
        for (Permanent monk : monks) {
            assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(1);
        }

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        for (Permanent monk : monks) {
            assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(2);
        }
    }

    @Test
    void gainsFourLifeAndDealsDamageToASeparateCreature() {
        Permanent bounceTarget = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent damageTarget = harness.addToBattlefieldAndReturn(player2, new DragonSniper());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new JeskaiRevelation()));
        addJeskaiRevelationMana();

        harness.castInstant(player1, 0, bounceTarget.getId(), damageTarget.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Island");
        harness.assertInGraveyard(player2, "Dragon Sniper");
        harness.assertNotOnBattlefield(player2, "Dragon Sniper");
        harness.assertLife(player1, 24);
        assertThat(countPermanents(player1, "Monk")).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void canReturnAndTargetTheSameCreatureWithoutDamagingItAfterItLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DragonSniper());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new JeskaiRevelation()));
        addJeskaiRevelationMana();

        harness.castInstant(player1, 0, target.getId(), target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Dragon Sniper");
        harness.assertNotInGraveyard(player2, "Dragon Sniper");
        harness.assertLife(player1, 24);
        assertThat(countPermanents(player1, "Monk")).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void stillResolvesWhenOnlyTheReturnTargetBecomesIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DragonSniper());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new JeskaiRevelation()));
        addJeskaiRevelationMana();
        harness.castInstant(player1, 0, target.getId(), player2.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Dragon Sniper");
        harness.assertNotInHand(player2, "Dragon Sniper");
        harness.assertLife(player2, 16);
        harness.assertLife(player1, 24);
        assertThat(countPermanents(player1, "Monk")).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void stillResolvesWhenOnlyTheDamageTargetBecomesIllegal() {
        Permanent bounceTarget = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent damageTarget = harness.addToBattlefieldAndReturn(player2, new DragonSniper());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new JeskaiRevelation()));
        addJeskaiRevelationMana();
        harness.castInstant(player1, 0, bounceTarget.getId(), damageTarget.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, damageTarget.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Island");
        harness.assertInGraveyard(player2, "Dragon Sniper");
        harness.assertLife(player1, 24);
        assertThat(countPermanents(player1, "Monk")).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void doesNotResolveWhenBothTargetsBecomeIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DragonSniper());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new JeskaiRevelation()));
        addJeskaiRevelationMana();
        harness.castInstant(player1, 0, target.getId(), target.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jeskai Revelation");
        harness.assertLife(player1, 20);
        assertThat(countPermanents(player1, "Monk")).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void creatureSpellsDoNotTriggerTheCreatedMonks() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new JeskaiRevelation()));
        addJeskaiRevelationMana();
        harness.castInstant(player1, 0, target.getId(), player2.getId());
        harness.passBothPriorities();

        harness.castFromHand(player1, new DragonSniper(), "{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dragon Sniper");
        assertThat(findPermanents(player1, "Monk")).hasSize(2).allSatisfy(monk -> {
            assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(1);
        });
    }

    private void addJeskaiRevelationMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
