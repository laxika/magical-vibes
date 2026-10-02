package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.StabWound;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PerforatorCrocodile.class, HillGiant.class, StabWound.class})
class PerforatorCrocodileTest extends BaseCardTest {

    @Test
    void conjuresAndAttachesStabWoundToEachOpposingCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent firstOpponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent secondOpponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.setHand(player1, List.of(new PerforatorCrocodile()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        List<Permanent> wounds = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Stab Wound"))
                .toList();
        assertThat(wounds).hasSize(2);
        assertThat(wounds).allMatch(Permanent::isAttached);
        assertThat(wounds).extracting(Permanent::getAttachedTo)
                .containsExactlyInAnyOrder(firstOpponentCreature.getId(), secondOpponentCreature.getId());
        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, firstOpponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, secondOpponentCreature)).isEqualTo(1);
    }

    @Test
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new PerforatorCrocodile()));
        harness.setLibrary(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Perforator Crocodile");
        harness.assertInHand(player1, "Hill Giant");
    }
}
