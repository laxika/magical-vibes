package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ColossusOfSardia;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.p.PreyUpon;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed({
        MaarikaBrutalGladiator.class,
        ColossusOfSardia.class,
        Forest.class,
        GrizzlyBears.class,
        HowlingMine.class,
        PreyUpon.class,
        WrathOfGod.class
})
class MaarikaBrutalGladiatorTest extends BaseCardTest {

    @Test
    @DisplayName("Excess damage makes the damaged creature's controller sacrifice a noncreature nonland permanent")
    void sacrificesNoncreatureNonlandPermanentAfterExcessDamage() {
        harness.addToBattlefield(player1, new MaarikaBrutalGladiator());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HowlingMine());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new PreyUpon()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);

        UUID maarikaId = harness.getPermanentId(player1, "Maarika, Brutal Gladiator");
        UUID grizzlyBearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(maarikaId, grizzlyBearsId));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Howling Mine");
        harness.assertInGraveyard(player2, "Howling Mine");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("The trigger does not fire when the damage is not excess")
    void doesNotTriggerWithoutExcessDamage() {
        harness.addToBattlefield(player1, new MaarikaBrutalGladiator());
        harness.addToBattlefield(player2, new ColossusOfSardia());
        harness.addToBattlefield(player2, new HowlingMine());
        harness.setHand(player1, List.of(new PreyUpon()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);

        UUID maarikaId = harness.getPermanentId(player1, "Maarika, Brutal Gladiator");
        UUID colossusId = harness.getPermanentId(player2, "Colossus of Sardia");
        harness.castSorcery(player1, 0, List.of(maarikaId, colossusId));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Colossus of Sardia");
        harness.assertOnBattlefield(player2, "Howling Mine");
    }

    @Test
    @DisplayName("Indestructible applies during Maarika's turn")
    void indestructibleAppliesDuringOwnTurn() {
        harness.addToBattlefield(player1, new MaarikaBrutalGladiator());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Maarika, Brutal Gladiator");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Indestructible does not apply during an opponent's turn")
    void indestructibleDoesNotApplyDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new MaarikaBrutalGladiator());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Maarika, Brutal Gladiator");
        harness.assertInGraveyard(player1, "Maarika, Brutal Gladiator");
    }
}
