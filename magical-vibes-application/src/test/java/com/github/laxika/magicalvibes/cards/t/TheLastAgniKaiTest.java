package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheLastAgniKai.class, GrizzlyBears.class, HillGiant.class})
class TheLastAgniKaiTest extends BaseCardTest {

    @Test
    void fightsAndAddsRedManaEqualToExcessDamage() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheLastAgniKai()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void preservesExistingRedManaButNotOtherColorsUntilEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new TheLastAgniKai()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isEqualTo(1);

        pool.add(ManaColor.BLUE);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(pool.get(ManaColor.RED)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLUE)).isZero();
    }

    @Test
    void exactLethalDamageAddsNoManaAndBothCreaturesDie() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheLastAgniKai()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void excessDamageAccountsForPreviouslyMarkedDamage() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opposingCreature.setMarkedDamage(1);
        harness.setHand(player1, List.of(new TheLastAgniKai()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(ownCreature.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void missingOpponentTargetPreventsFightButStillPreservesRedMana() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheLastAgniKai()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(opposingCreature);
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void allTargetsMissingPreventsManaRetentionEffect() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheLastAgniKai()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(ownCreature);
        gd.playerBattlefields.get(player2.getId()).remove(opposingCreature);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void manaAddedLaterIsPreservedOnlyForCasterAndExpiresAtTurnEnd() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheLastAgniKai()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();

        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);
        gs.advanceStep(gd);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }
}
