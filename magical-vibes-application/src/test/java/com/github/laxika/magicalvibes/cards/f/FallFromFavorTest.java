package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FallFromFavor.class, GrizzlyBears.class})
class FallFromFavorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters, taps the enchanted creature, and makes its controller the monarch")
    void entersTapsAndMakesControllerMonarch() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castFallFromFavor(player1, creature);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Keeps the enchanted creature tapped while its controller is not the monarch")
    void locksEnchantedCreatureWhileItsControllerIsNotMonarch() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();

        castFallFromFavor(player1, creature);
        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Allows the enchanted creature to untap when its controller becomes the monarch")
    void allowsUntapWhenEnchantedControllerIsMonarch() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();

        castFallFromFavor(player1, creature);
        gd.monarchPlayerId = player2.getId();
        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enchanting your own creature taps it but permits untapping while you are monarch")
    void ownCreatureUntapsWhileAuraControllerIsMonarch() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castFallFromFavor(player1, creature);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        harness.performUntapStep(player1);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The untap restriction returns when the enchanted creature's controller loses the monarchy")
    void losingMonarchyRestoresLockOnlyForEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFallFromFavor(player1, creature);
        gd.monarchPlayerId = player2.getId();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();

        creature.tap();
        otherCreature.tap();
        gd.monarchPlayerId = player1.getId();
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
        assertThat(otherCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The enter trigger still taps the enchanted creature after the Aura leaves")
    void enterTriggerUsesLastKnownAttachmentAfterAuraLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FallFromFavor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Fall from Favor");
        assertThat(creature.isTapped()).isFalse();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    private void castFallFromFavor(Player caster, Permanent creature) {
        harness.setHand(caster, List.of(new FallFromFavor()));
        harness.addMana(caster, ManaColor.BLUE, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 2);
        harness.castEnchantment(caster, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        Player nextPlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.passUntil(nextPlayer, TurnStep.UPKEEP);
    }
}
