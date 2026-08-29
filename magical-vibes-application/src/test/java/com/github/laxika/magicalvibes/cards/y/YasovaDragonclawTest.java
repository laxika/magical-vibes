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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class YasovaDragonclawTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
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
        harness.addMana(player1, ManaColor.BLUE, 3);

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
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
        harness.addMana(player1, ManaColor.RED, 3);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, bearsId).hasKeyword(Keyword.HASTE)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

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
}
