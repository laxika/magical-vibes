package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuperCombo.class, GrizzlyBears.class, HillGiant.class, Plains.class})
class SuperComboTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the controlled creature's power")
    void dealsPowerDamageToOpponentCreature() {
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());
        castSuperCombo(source, target, List.of());

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Replicate creates one copy for each replicate payment")
    void replicateCreatesCopiesForEachPayment() {
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());
        castSuperCombo(source, target, List.of("{2}", "{2}"));

        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Requires an opponent's creature as the second target")
    void rejectsCreatureYouControlAsSecondTarget() {
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of(new SuperCombo()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void rejectsNoncreatureTarget() {
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new SuperCombo()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSuperCombo(Permanent source, Permanent target, List<String> replicatePayments) {
        harness.setHand(player1, List.of(new SuperCombo()));
        harness.addMana(player1, ManaColor.GREEN, 2 + replicatePayments.size() * 2);
        gs.playCard(gd, player1, 0, 0, null, (Map<UUID, Integer>) null,
                List.of(source.getId(), target.getId()), List.<UUID>of(), false, null, null,
                List.<UUID>of(), null, List.<Integer>of(), false, null, List.<Integer>of(),
                List.<UUID>of(), List.<UUID>of(), replicatePayments, false);
    }
}
