package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnexpectedRequest.class, GrizzlyBears.class, LeoninScimitar.class})
class UnexpectedRequestTest extends BaseCardTest {

    @Test
    @DisplayName("Steals, untaps, and grants haste before offering the Equipment choice")
    void stealsUntapsAndGrantsHaste() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        harness.addToBattlefield(player1, new LeoninScimitar());

        castUnexpectedRequest(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();

        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Attaches a chosen Equipment and unattaches it at the next end step")
    void attachesAndUnattachesAtNextEndStep() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        castUnexpectedRequest(target);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(equipment.getAttachedTo()).isEqualTo(target.getId());

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Chooses one of multiple controlled Equipment")
    void choosesOneOfMultipleEquipment() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent firstEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent secondEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        castUnexpectedRequest(target);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, secondEquipment.getId());

        assertThat(secondEquipment.getAttachedTo()).isEqualTo(target.getId());
        assertThat(firstEquipment.getAttachedTo()).isNull();
    }

    private void castUnexpectedRequest(Permanent target) {
        harness.setHand(player1, List.of(new UnexpectedRequest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, target.getId());
    }
}
