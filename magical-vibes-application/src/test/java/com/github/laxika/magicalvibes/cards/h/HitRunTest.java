package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AssaultZeppelid;
import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HitRun.class, AzoriusSignet.class, AssaultZeppelid.class})
class HitRunTest extends BaseCardTest {

    private static final int HIT = 0;
    private static final int RUN = 1;

    @Test
    @DisplayName("Hit makes the target player sacrifice an artifact and deals its mana value as damage")
    void hitSacrificesArtifactAndDealsManaValueDamage() {
        harness.addToBattlefield(player2, new AzoriusSignet());
        harness.setHand(player1, List.of(new HitRun()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, HIT, player2.getId(), null, List.of(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Azorius Signet");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Hit lets the target player choose an artifact or creature before determining damage")
    void hitLetsTargetPlayerChoosePermanent() {
        harness.addToBattlefield(player2, new AzoriusSignet());
        Permanent zeppelid = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        harness.setHand(player1, List.of(new HitRun()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, HIT, player2.getId(), null, List.of(), List.of());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();

        harness.handlePermanentChosen(player2, zeppelid.getId());

        harness.assertInGraveyard(player2, "Assault Zeppelid");
        harness.assertOnBattlefield(player2, "Azorius Signet");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Hit may target its controller")
    void hitCanTargetItsController() {
        harness.addToBattlefield(player1, new AzoriusSignet());
        harness.setHand(player1, List.of(new HitRun()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, HIT, player1.getId(), null, List.of(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Azorius Signet");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Run boosts each attacking creature by the number of other attacking creatures you control")
    void runBoostsOwnAttackersOnly() {
        Permanent ownAttacker = addCreatureReady(player1, new AssaultZeppelid());
        ownAttacker.setAttacking(true);
        Permanent secondOwnAttacker = addCreatureReady(player1, new AssaultZeppelid());
        secondOwnAttacker.setAttacking(true);
        Permanent ownNonAttacker = addCreatureReady(player1, new AssaultZeppelid());
        Permanent opponentAttacker = addCreatureReady(player2, new AssaultZeppelid());
        opponentAttacker.setAttacking(true);

        harness.setHand(player1, List.of(new HitRun()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalInstant(player1, 0, RUN, List.of());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownAttacker)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondOwnAttacker)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownNonAttacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentAttacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Run's boost wears off at end of turn")
    void runBoostExpiresAtEndOfTurn() {
        Permanent firstAttacker = addCreatureReady(player1, new AssaultZeppelid());
        firstAttacker.setAttacking(true);
        Permanent secondAttacker = addCreatureReady(player1, new AssaultZeppelid());
        secondAttacker.setAttacking(true);

        harness.setHand(player1, List.of(new HitRun()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalInstant(player1, 0, RUN, List.of());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstAttacker)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondAttacker)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstAttacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondAttacker)).isEqualTo(3);
    }
}
