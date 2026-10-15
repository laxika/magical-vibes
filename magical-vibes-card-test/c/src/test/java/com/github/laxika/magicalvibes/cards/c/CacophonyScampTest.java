package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HexgoldSlash;
import com.github.laxika.magicalvibes.cards.m.MirranBardiche;
import com.github.laxika.magicalvibes.cards.p.PredationSteward;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CacophonyScamp.class, HexgoldSlash.class, MirranBardiche.class, PredationSteward.class})
class CacophonyScampTest extends BaseCardTest {

    @Test
    @DisplayName("May sacrifice after dealing combat damage to proliferate")
    void maySacrificeAndProliferate() {
        Permanent scamp = addCreatureReady(player1, new CacophonyScamp());
        scamp.setAttacking(true);

        Permanent bears = addCreatureReady(player1, new PredationSteward());
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
        harness.setHand(player2, List.of(new HexgoldSlash()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, scamp.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Sacrifice proliferates every counter kind on chosen permanents and players")
    void proliferatesPermanentAndPlayerCounters() {
        Permanent scamp = addCreatureReady(player1, new CacophonyScamp());
        scamp.setAttacking(true);
        Permanent steward = addCreatureReady(player2, new PredationSteward());
        steward.setCounterCount(CounterType.OIL, 2);
        steward.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 1);

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(steward.getId(), player2.getId()));

        assertThat(steward.getCounterCount(CounterType.OIL)).isEqualTo(3);
        assertThat(steward.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("May choose no objects to proliferate and still resolve the death trigger")
    void canChooseNothingToProliferate() {
        Permanent scamp = addCreatureReady(player1, new CacophonyScamp());
        scamp.setAttacking(true);
        Permanent steward = addCreatureReady(player1, new PredationSteward());
        steward.setCounterCount(CounterType.OIL, 2);

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(steward.getCounterCount(CounterType.OIL)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Cacophony Scamp");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Sacrifice is allowed even when nothing has counters")
    void canSacrificeWithoutEligibleProliferateChoices() {
        Permanent scamp = addCreatureReady(player1, new CacophonyScamp());
        scamp.setAttacking(true);

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cacophony Scamp");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Death damage can target and kill a creature")
    void deathDamageCanTargetCreature() {
        Permanent scamp = harness.addToBattlefieldAndReturn(player1, new CacophonyScamp());
        scamp.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent steward = harness.addToBattlefieldAndReturn(player2, new PredationSteward());
        harness.setHand(player2, List.of(new HexgoldSlash()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, scamp.getId());
        harness.handlePermanentChosen(player1, steward.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cacophony Scamp");
        harness.assertInGraveyard(player2, "Predation Steward");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Death damage retains the equipment bonus from immediately before death")
    void deathDamageRetainsEquipmentBonus() {
        Permanent scamp = harness.addToBattlefieldAndReturn(player1, new CacophonyScamp());
        Permanent bardiche = harness.addToBattlefieldAndReturn(player1, new MirranBardiche());
        bardiche.setAttachedTo(scamp.getId());
        harness.setHand(player2, List.of(new HexgoldSlash()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, scamp.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cacophony Scamp");
        harness.assertLife(player2, 17);
    }
}
