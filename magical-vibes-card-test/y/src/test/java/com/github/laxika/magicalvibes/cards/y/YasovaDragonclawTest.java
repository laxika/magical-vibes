package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YasovaDragonclaw.class, GrizzlyBears.class, HillGiant.class})
class YasovaDragonclawTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    private Permanent findPermanent(Player owner, UUID id) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getId().equals(id))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Paying the hybrid ability cost steals, untaps and hastes a smaller creature")
    void paysAndStealsSmallerCreature() {
        harness.addToBattlefield(player1, new YasovaDragonclaw());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        findPermanent(player2, bearsId).tap();

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, bearsId);
        assertThat(bears.isTapped()).isFalse();
        assertThat(bears.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Declining the payment leaves the target under its owner's control")
    void decliningPaymentDoesNothing() {
        harness.addToBattlefield(player1, new YasovaDragonclaw());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player2, bearsId)).isNotNull();
        assertThat(findPermanent(player2, bearsId).hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Control and haste expire at end of turn")
    void controlAndHasteExpireAtEndOfTurn() {
        harness.addToBattlefield(player1, new YasovaDragonclaw());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 3);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, bearsId).hasKeyword(Keyword.HASTE)).isTrue();

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TurnCleanupService.class)
                .applyCleanupResets(gd));

        assertThat(findPermanent(player2, bearsId).hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature whose power is not less than Yasova's")
    void rejectsCreatureWithTooMuchPower() {
        harness.addToBattlefield(player1, new YasovaDragonclaw());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        giant.setPowerModifier(1);

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        harness.addToBattlefield(player1, new YasovaDragonclaw());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A smaller creature controlled by Yasova's controller is not a legal target")
    void rejectsOwnCreature() {
        harness.addToBattlefield(player1, new YasovaDragonclaw());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The hybrid cost can be paid with one blue, one red and one green mana")
    void paysWithMixedHybridColors() {
        harness.addToBattlefield(player1, new YasovaDragonclaw());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, bears.getId()).hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("A target that grows to Yasova's power makes the ability fail before payment")
    void rechecksTargetPowerBeforePayment() {
        harness.addToBattlefield(player1, new YasovaDragonclaw());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.tap();

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        bears.setPowerModifier(2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player2, bears.getId()).isTapped()).isTrue();
        assertThat(bears.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Yasova's current power is checked again before offering payment")
    void rechecksSourcePowerBeforePayment() {
        Permanent yasova = harness.addToBattlefieldAndReturn(player1, new YasovaDragonclaw());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, giant.getId());
        yasova.setPowerModifier(-1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player2, giant.getId()).hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
