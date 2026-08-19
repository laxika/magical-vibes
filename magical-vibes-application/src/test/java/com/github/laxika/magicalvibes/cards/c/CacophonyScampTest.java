package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CacophonyScampTest extends BaseCardTest {

    @Test
    @DisplayName("May sacrifice after dealing combat damage to proliferate")
    void maySacrificeAndProliferate() {
        Permanent scamp = addCreatureReady(player1, new CacophonyScamp());
        scamp.setAttacking(true);

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cacophony Scamp");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the combat-damage sacrifice keeps Cacophony Scamp on the battlefield")
    void maySacrificeCanBeDeclined() {
        Permanent scamp = addCreatureReady(player1, new CacophonyScamp());
        scamp.setAttacking(true);

        resolveCombat();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Cacophony Scamp");
    }

    @Test
    @DisplayName("Death trigger deals damage equal to its last known power")
    void deathTriggerDealsLastKnownPowerDamage() {
        Permanent scamp = harness.addToBattlefieldAndReturn(player1, new CacophonyScamp());
        scamp.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, scamp.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
