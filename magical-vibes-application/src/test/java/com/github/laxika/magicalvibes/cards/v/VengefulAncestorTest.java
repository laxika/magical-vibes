package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VengefulAncestor.class, GrizzlyBears.class})
class VengefulAncestorTest extends BaseCardTest {

    @Test
    void entersAndGoadsTargetCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castVengefulAncestor(target);

        assertThat(gqs.isGoaded(gd, target)).isTrue();
    }

    @Test
    void goadedCreatureDealsDamageToItsControllerWhenItAttacks() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castVengefulAncestor(target);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private void castVengefulAncestor(Permanent target) {
        harness.setHand(player1, List.of(new VengefulAncestor()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
