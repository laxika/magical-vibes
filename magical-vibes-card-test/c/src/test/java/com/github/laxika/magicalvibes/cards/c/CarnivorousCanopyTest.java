package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AtmosphereSurgeon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.i.IchorplateGolem;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CarnivorousCanopy.class, AirElemental.class, GrizzlyBears.class, Millstone.class,
        IchorplateGolem.class, PhyrexianArena.class, AtmosphereSurgeon.class})
class CarnivorousCanopyTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a low-mana-value artifact and proliferates")
    void destroysLowManaValueArtifactAndProliferates() {
        Permanent creatureWithCounter = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creatureWithCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefield(player2, new Millstone());
        UUID targetId = harness.getPermanentId(player2, "Millstone");

        castCarnivorousCanopy(targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertInGraveyard(player2, "Millstone");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(creatureWithCounter.getId()));

        assertThat(creatureWithCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Destroys a high-mana-value flying creature without proliferating")
    void destroysHighManaValueFlyingCreatureWithoutProliferating() {
        Permanent creatureWithCounter = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creatureWithCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefield(player2, new AirElemental());
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        castCarnivorousCanopy(targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(creatureWithCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new CarnivorousCanopy()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castCarnivorousCanopy(UUID targetId) {
        harness.setHand(player1, List.of(new CarnivorousCanopy()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, targetId);
    }

    @Test
    void destroysLowManaValueFlyingCreatureAndProliferates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AtmosphereSurgeon());
        target.setCounterCount(CounterType.FLYING, 1);
        gd.playerPoisonCounters.put(player2.getId(), 1);

        castCarnivorousCanopy(target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Atmosphere Surgeon");
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    void doesNotProliferateWhenCreatureLosesFlyingBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AtmosphereSurgeon());
        target.setCounterCount(CounterType.FLYING, 1);
        gd.playerPoisonCounters.put(player2.getId(), 1);
        castCarnivorousCanopy(target.getId());
        target.setCounterCount(CounterType.FLYING, 0);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Atmosphere Surgeon");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
    }

    @Test
    void destroysEnchantmentAtManaValueThreeAndProliferatesPlayer() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianArena());
        gd.playerPoisonCounters.put(player2.getId(), 2);

        castCarnivorousCanopy(target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Phyrexian Arena");
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
    }

    @Test
    void destroysNonflyingArtifactCreatureAtManaValueThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IchorplateGolem());
        gd.playerPoisonCounters.put(player2.getId(), 1);

        castCarnivorousCanopy(target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Ichorplate Golem");
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void proliferatesEvenWhenLowManaValueTargetIsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IchorplateGolem());
        target.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        target.setCounterCount(CounterType.OIL, 2);

        castCarnivorousCanopy(target.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Ichorplate Golem");
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        assertThat(target.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.OIL)).isEqualTo(3);
    }

    @Test
    void doesNotProliferateWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IchorplateGolem());
        gd.playerPoisonCounters.put(player2.getId(), 2);
        castCarnivorousCanopy(target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        harness.assertInGraveyard(player1, "Carnivorous Canopy");
    }
}
