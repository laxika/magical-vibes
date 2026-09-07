package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.s.SearingBlaze;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImodaneThePyrohammer.class, GrizzlyBears.class, Pyroclasm.class, SearingBlaze.class, Shock.class})
class ImodaneThePyrohammerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals the creature damage to each opponent when a single-target creature spell deals damage")
    void singleTargetCreatureSpellDealsDamageToEachOpponent() {
        harness.addToBattlefield(player1, new ImodaneThePyrohammer());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not trigger when an instant or sorcery targets a player")
    void playerTargetDoesNotTrigger() {
        harness.addToBattlefield(player1, new ImodaneThePyrohammer());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not trigger for an untargeted damage spell")
    void untargetedDamageDoesNotTrigger() {
        harness.addToBattlefield(player1, new ImodaneThePyrohammer());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger when the damage spell has another target")
    void multipleTargetsDoNotTrigger() {
        harness.addToBattlefield(player1, new ImodaneThePyrohammer());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SearingBlaze()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, List.of(player2.getId(), creature.getId()));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }
}
