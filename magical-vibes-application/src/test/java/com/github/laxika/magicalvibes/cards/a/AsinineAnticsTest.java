package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PolisCrusher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AsinineAntics.class, GrizzlyBears.class, PolisCrusher.class})
class AsinineAnticsTest extends BaseCardTest {

    @Test
    void createsCursedRoleAttachedToEachOpposingCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castNormally();

        List<Permanent> roles = findPermanents(player1, "Cursed");
        assertThat(roles).hasSize(2);
        assertThat(roles).extracting(Permanent::getAttachedTo)
                .containsExactlyInAnyOrder(firstOpponentCreature.getId(), secondOpponentCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstOpponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, secondOpponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    void doesNotCreateRoleThatCannotEnchantAnOpposingCreature() {
        harness.addToBattlefieldAndReturn(player2, new PolisCrusher());
        Permanent legalCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castNormally();

        assertThat(findPermanents(player1, "Cursed")).singleElement()
                .extracting(Permanent::getAttachedTo)
                .isEqualTo(legalCreature.getId());
    }

    @Test
    void canBeCastAtInstantSpeedForTwoMoreMana() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new AsinineAntics()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castWithAlternateCost(player1, 0, List.of());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Cursed")).singleElement()
                .extracting(Permanent::getAttachedTo)
                .isEqualTo(opponentCreature.getId());
    }

    private void castNormally() {
        harness.setHand(player1, List.of(new AsinineAntics()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
    }
}
