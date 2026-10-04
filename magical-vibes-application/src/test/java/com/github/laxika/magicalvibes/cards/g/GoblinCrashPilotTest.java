package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinCrashPilot.class, DuskLegionDreadnought.class, LightningStrike.class})
class GoblinCrashPilotTest extends BaseCardTest {

    @Test
    void crewsVehicleGivesItHasteThenSacrificesItAndDealsItsPowerAsDamage() {
        Permanent pilot = addCreatureReady(player1, new GoblinCrashPilot());
        Permanent dreadnought = addCreatureReady(player1, new DuskLegionDreadnought());

        harness.activateAbility(player1, indexOf(player1, dreadnought), null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dreadnought, Keyword.HASTE)).isTrue();
        assertThat(gqs.isCreature(gd, dreadnought)).isTrue();
        assertThat(pilot.isTapped()).isTrue();

        declareAttackers(player1, List.of());
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dusk Legion Dreadnought");
        harness.assertInGraveyard(player1, "Dusk Legion Dreadnought");
        harness.assertLife(player2, 16);
    }

    @Test
    void delayedSacrificeAndDamageStillHappenAfterPilotDies() {
        Permanent pilot = addCreatureReady(player1, new GoblinCrashPilot());
        Permanent vehicle = addCreatureReady(player1, new DuskLegionDreadnought());
        harness.activateAbility(player1, indexOf(player1, vehicle), null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, pilot.getId());
        harness.assertInGraveyard(player1, "Goblin Crash Pilot");

        declareAttackers(player1, List.of());
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dusk Legion Dreadnought");
        harness.assertLife(player2, 16);
    }

    @Test
    void crewingAfterEndStepBeginsSacrificesNoncreatureNextTurnWithoutDamage() {
        addCreatureReady(player1, new GoblinCrashPilot());
        Permanent vehicle = addCreatureReady(player1, new DuskLegionDreadnought());
        declareAttackers(player1, List.of());
        harness.passUntil(player1, TurnStep.END_STEP);

        harness.activateAbility(player1, indexOf(player1, vehicle), null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.HASTE)).isTrue();
        harness.assertOnBattlefield(player1, "Dusk Legion Dreadnought");

        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.HASTE)).isFalse();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dusk Legion Dreadnought");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 20);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
