package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
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

    @Test
    @DisplayName("Requires the opponent's creature target even when none is available")
    void rejectsMissingOpponentCreatureTarget() {
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SuperCombo()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(source.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires a creature you control as the damage source")
    void rejectsOpponentCreatureAsSource() {
        Permanent source = addCreatureReady(player2, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new SuperCombo()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deals no damage when the source leaves the battlefield before resolution")
    void dealsNoDamageWithoutSource() {
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());
        castSuperCombo(source, target, List.of());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Damage is one-sided and does not cause the opponent's creature to fight back")
    void sourceDoesNotTakeDamage() {
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());
        castSuperCombo(source, target, List.of());

        harness.passBothPriorities();

        assertThat(source.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Two replicate payments create exactly two copies in addition to the original")
    void twoPaymentsCreateExactlyTwoCopies() {
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());
        castSuperCombo(source, target, List.of("{2}", "{2}"));

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Replicate requires two additional mana beyond the spell's cost")
    void rejectsReplicateWithoutAdditionalMana() {
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new SuperCombo()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorceryWithRepeatedCosts(player1, 0, List.of("{2}"),
                List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private void castSuperCombo(Permanent source, Permanent target, List<String> replicatePayments) {
        harness.setHand(player1, List.of(new SuperCombo()));
        harness.addMana(player1, ManaColor.GREEN, 2 + replicatePayments.size() * 2);
        harness.castSorceryWithRepeatedCosts(player1, 0, replicatePayments,
                List.of(source.getId(), target.getId()));
    }
}
