package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AjanisPresence;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PheresBandThunderhoof.class, Shock.class, GiantGrowth.class, AjanisPresence.class})
class PheresBandThunderhoofTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Pheres-Band Thunderhoof puts two +1/+1 counters on it")
    void castingSpellThatTargetsThunderhoofTriggersHeroic() {
        harness.addToBattlefield(player1, new PheresBandThunderhoof());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID thunderhoofId = harness.getPermanentId(player1, "Pheres-Band Thunderhoof");
        harness.castAndResolveInstant(player1, 0, thunderhoofId);
        harness.passBothPriorities();

        Permanent thunderhoof = findPermanent(player1, "Pheres-Band Thunderhoof");
        assertThat(thunderhoof.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger Pheres-Band Thunderhoof's Heroic")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new PheresBandThunderhoof());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        Permanent thunderhoof = findPermanent(player1, "Pheres-Band Thunderhoof");
        assertThat(thunderhoof.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's spell that targets Pheres-Band Thunderhoof does not trigger it")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new PheresBandThunderhoof());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        UUID thunderhoofId = harness.getPermanentId(player1, "Pheres-Band Thunderhoof");
        harness.castAndResolveInstant(player2, 0, thunderhoofId);

        Permanent thunderhoof = findPermanent(player1, "Pheres-Band Thunderhoof");
        assertThat(thunderhoof.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void heroicResolvesBeforeTheTargetingSpell() {
        Permanent thunderhoof = harness.addToBattlefieldAndReturn(player1, new PheresBandThunderhoof());
        harness.setHand(player1, List.of(new AjanisPresence()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, thunderhoof.getId());

        assertThat(thunderhoof.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(thunderhoof.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(thunderhoof.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Ajani's Presence");
    }

    @Test
    void multiTargetSpellTriggersOnlyTheThunderhoofWhoseControllerCastIt() {
        Permanent ownThunderhoof = harness.addToBattlefieldAndReturn(player1, new PheresBandThunderhoof());
        Permanent opposingThunderhoof = harness.addToBattlefieldAndReturn(player2, new PheresBandThunderhoof());
        harness.setHand(player1, List.of(new AjanisPresence()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0,
                List.of(opposingThunderhoof.getId(), ownThunderhoof.getId()));
        harness.passBothPriorities();

        assertThat(ownThunderhoof.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opposingThunderhoof.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void spellWithNoTargetsDoesNotTriggerHeroic() {
        Permanent thunderhoof = harness.addToBattlefieldAndReturn(player1, new PheresBandThunderhoof());
        harness.setHand(player1, List.of(new AjanisPresence()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, List.of());

        assertThat(thunderhoof.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eachTargetingSpellTriggersHeroicEvenDuringAnOpponentsTurn() {
        Permanent thunderhoof = harness.addToBattlefieldAndReturn(player1, new PheresBandThunderhoof());
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new AjanisPresence(), new AjanisPresence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, thunderhoof.getId());
        harness.passBothPriorities();
        assertThat(thunderhoof.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.castAndResolveInstant(player1, 0, thunderhoof.getId());
        harness.passBothPriorities();

        assertThat(thunderhoof.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void targetingAnotherThunderhoofDoesNotTriggerThisOne() {
        Permanent firstThunderhoof = harness.addToBattlefieldAndReturn(player1, new PheresBandThunderhoof());
        Permanent secondThunderhoof = harness.addToBattlefieldAndReturn(player1, new PheresBandThunderhoof());
        harness.setHand(player1, List.of(new AjanisPresence()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, secondThunderhoof.getId());
        harness.passBothPriorities();

        assertThat(firstThunderhoof.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(secondThunderhoof.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
