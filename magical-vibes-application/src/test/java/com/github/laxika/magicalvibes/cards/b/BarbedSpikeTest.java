package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ChatterfangSquirrelGeneral;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarbedSpike.class, GrizzlyBears.class, BreakTies.class, ChatterfangSquirrelGeneral.class})
class BarbedSpikeTest extends BaseCardTest {

    @Test
    void createsAndEquipsAThopter() {
        castAndResolveBarbedSpike();

        Permanent spike = findPermanent(player1, "Barbed Spike");
        Permanent thopter = findPermanent(player1, "Thopter");

        assertThat(spike.getAttachedTo()).isEqualTo(thopter.getId());
        assertThat(thopter.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
    }

    @Test
    void equipMovesBarbedSpikeOffItsThopter() {
        castAndResolveBarbedSpike();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        Permanent spike = findPermanent(player1, "Barbed Spike");
        Permanent thopter = findPermanent(player1, "Thopter");

        assertThat(spike.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(thopter.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
    }

    @Test
    void controllerCanChooseThopterWhenChatterfangAddsASquirrel() {
        harness.addToBattlefield(player1, new ChatterfangSquirrelGeneral());
        castAndResolveBarbedSpike();

        Permanent spike = findPermanent(player1, "Barbed Spike");
        Permanent thopter = findPermanent(player1, "Thopter");
        Permanent squirrel = findPermanent(player1, "Squirrel");
        assertThat(countPermanents(player1, "Thopter")).isEqualTo(1);
        assertThat(countPermanents(player1, "Squirrel")).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, thopter.getId());
        resolveAllTriggers();
        assertThat(spike.getAttachedTo()).isEqualTo(thopter.getId());
        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, squirrel)).isEqualTo(1);
    }

    private void castAndResolveBarbedSpike() {
        harness.setHand(player1, List.of(new BarbedSpike()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
    }

    @Test
    void createsThopterEvenWhenEquipmentIsDestroyedBeforeTriggerResolves() {
        harness.setHand(player1, List.of(new BarbedSpike()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent spike = findPermanent(player1, "Barbed Spike");
        harness.setHand(player2, List.of(new BreakTies()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castModalInstant(player2, 0, 0, List.of(spike.getId()));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Barbed Spike");
        harness.assertInGraveyard(player1, "Barbed Spike");
        assertThat(countPermanents(player1, "Thopter")).isEqualTo(1);
        Permanent thopter = findPermanent(player1, "Thopter");
        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
    }
}
