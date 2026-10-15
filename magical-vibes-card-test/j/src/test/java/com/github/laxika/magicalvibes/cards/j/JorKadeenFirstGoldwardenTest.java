package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.cards.h.HexgoldHalberd;
import com.github.laxika.magicalvibes.cards.m.MirranBardiche;
import com.github.laxika.magicalvibes.cards.v.VanishIntoEternity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JorKadeenFirstGoldwarden.class, CopperLonglegs.class, HexgoldHalberd.class,
        MirranBardiche.class, VanishIntoEternity.class})
class JorKadeenFirstGoldwardenTest extends BaseCardTest {

    @Test
    void getsBoostForEachEquippedCreatureAndDrawsAtFourPower() {
        Permanent jor = addCreatureReady(player1, new JorKadeenFirstGoldwarden());
        Permanent spider = addCreatureReady(player1, new CopperLonglegs());
        attachEquipment(jor);
        attachEquipment(spider);
        Card drawn = new CopperLonglegs();
        setDeck(drawn);

        attackWith(jor);

        assertThat(gqs.getEffectivePower(gd, jor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, jor)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void doesNotDrawWhenTheBoostLeavesJorBelowFourPower() {
        Permanent jor = addCreatureReady(player1, new JorKadeenFirstGoldwarden());
        attachEquipment(jor);
        setDeck(new CopperLonglegs());

        attackWith(jor);

        assertThat(gqs.getEffectivePower(gd, jor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, jor)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void unattachedEquipmentDoesNotCount() {
        Permanent jor = addCreatureReady(player1, new JorKadeenFirstGoldwarden());
        harness.addToBattlefield(player1, new HexgoldHalberd());
        setDeck(new CopperLonglegs());

        attackWith(jor);

        assertThat(gqs.getEffectivePower(gd, jor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, jor)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void multipleEquipmentOnOneCreatureCountOnlyOnce() {
        Permanent jor = addCreatureReady(player1, new JorKadeenFirstGoldwarden());
        attachEquipment(jor);
        attachEquipment(jor);
        setDeck(new CopperLonglegs());

        attackWith(jor);

        assertThat(gqs.getEffectivePower(gd, jor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, jor)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentsEquippedCreatureDoesNotCount() {
        Permanent jor = addCreatureReady(player1, new JorKadeenFirstGoldwarden());
        Permanent opponentCreature = addCreatureReady(player2, new CopperLonglegs());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new HexgoldHalberd());
        equipment.setAttachedTo(opponentCreature.getId());
        setDeck(new CopperLonglegs());

        attackWith(jor);

        assertThat(gqs.getEffectivePower(gd, jor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, jor)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void equipmentControllerDoesNotMatterForYourEquippedCreature() {
        Permanent jor = addCreatureReady(player1, new JorKadeenFirstGoldwarden());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new HexgoldHalberd());
        equipment.setAttachedTo(jor.getId());
        setDeck(new CopperLonglegs());

        attackWith(jor);

        assertThat(gqs.getEffectivePower(gd, jor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, jor)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void existingPowerBoostCanEnableDrawWithoutEquippedCreatures() {
        Permanent jor = addCreatureReady(player1, new JorKadeenFirstGoldwarden());
        jor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Card drawn = new CopperLonglegs();
        setDeck(drawn);

        attackWith(jor);

        assertThat(gqs.getEffectivePower(gd, jor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, jor)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void countsEquippedCreaturesWhenTriggerResolves() {
        Permanent jor = addCreatureReady(player1, new JorKadeenFirstGoldwarden());
        attachEquipment(jor);
        Permanent spider = addCreatureReady(player1, new CopperLonglegs());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new HexgoldHalberd());
        equipment.setAttachedTo(spider.getId());
        setDeck(new CopperLonglegs());
        harness.setHand(player2, List.of(new VanishIntoEternity()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        declareAttackers(player1, List.of(0));
        harness.castAndResolveInstant(player2, 0, equipment.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, jor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, jor)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void resolvedBoostDoesNotChangeWhenEquipmentLeaves() {
        Permanent jor = addCreatureReady(player1, new JorKadeenFirstGoldwarden());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new HexgoldHalberd());
        equipment.setAttachedTo(jor.getId());
        setDeck(new CopperLonglegs());
        harness.setHand(player2, List.of(new VanishIntoEternity()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        attackWith(jor);
        harness.castAndResolveInstant(player2, 0, equipment.getId());

        assertThat(gqs.getEffectivePower(gd, jor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, jor)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsUsingLastKnownPowerWhenEquippedJorLeavesBeforeResolution() {
        Permanent jor = addCreatureReady(player1, new JorKadeenFirstGoldwarden());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new MirranBardiche());
        equipment.setAttachedTo(jor.getId());
        Card drawn = new CopperLonglegs();
        setDeck(drawn);
        harness.setHand(player2, List.of(new VanishIntoEternity()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        declareAttackers(player1, List.of(0));
        harness.castAndResolveInstant(player2, 0, jor.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(jor);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    private void attackWith(Permanent creature) {
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        harness.passBothPriorities();
    }

    private void attachEquipment(Permanent creature) {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new HexgoldHalberd());
        equipment.setAttachedTo(creature.getId());
    }

    private void setDeck(Card card) {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(card));
    }
}
