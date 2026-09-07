package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HitRun.class, Bonesplitter.class, GrizzlyBears.class})
class HitRunTest extends BaseCardTest {

    private static final int HIT = 0;
    private static final int RUN = 1;

    @Test
    @DisplayName("Hit makes the target player sacrifice an artifact and deals its mana value as damage")
    void hitSacrificesArtifactAndDealsManaValueDamage() {
        harness.addToBattlefield(player2, new Bonesplitter());
        harness.setHand(player1, List.of(new HitRun()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, HIT, player2.getId(), null, List.of(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Bonesplitter");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Hit lets the target player choose an artifact or creature before determining damage")
    void hitLetsTargetPlayerChoosePermanent() {
        harness.addToBattlefield(player2, new Bonesplitter());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HitRun()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, HIT, player2.getId(), null, List.of(), List.of());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();

        harness.handlePermanentChosen(player2, bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Bonesplitter");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Run boosts each attacking creature by the number of other attacking creatures you control")
    void runBoostsOwnAttackersOnly() {
        Permanent ownAttacker = addCreatureReady(player1, new GrizzlyBears());
        ownAttacker.setAttacking(true);
        Permanent secondOwnAttacker = addCreatureReady(player1, new GrizzlyBears());
        secondOwnAttacker.setAttacking(true);
        Permanent ownNonAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentAttacker = addCreatureReady(player2, new GrizzlyBears());
        opponentAttacker.setAttacking(true);

        harness.setHand(player1, List.of(new HitRun()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalInstant(player1, 0, RUN, List.of());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownAttacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondOwnAttacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownNonAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentAttacker)).isEqualTo(2);
    }
}
