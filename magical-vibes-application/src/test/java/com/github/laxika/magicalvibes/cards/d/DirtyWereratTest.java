package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DirtyWereratTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card grants a regeneration shield")
    void discardCardRegenerates() {
        Permanent wererat = harness.addToBattlefieldAndReturn(player1, new DirtyWererat());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(wererat.getRegenerationShield()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Threshold grants +2/+2 and prevents blocking")
    void thresholdBoostsAndPreventsBlocking() {
        Permanent attacker = addAttacker();
        Permanent wererat = addWererat();
        int basePower = gqs.getEffectivePower(gd, wererat);
        int baseToughness = gqs.getEffectiveToughness(gd, wererat);

        harness.setGraveyard(player2, graveyardWithSevenCards());

        assertThat(gqs.getEffectivePower(gd, wererat)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, wererat)).isEqualTo(baseToughness + 2);

        prepareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(wererat),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Threshold effects disappear below seven graveyard cards")
    void thresholdEffectsDisappearBelowSevenCards() {
        Permanent wererat = addWererat();
        int basePower = gqs.getEffectivePower(gd, wererat);
        int baseToughness = gqs.getEffectiveToughness(gd, wererat);
        harness.setGraveyard(player2, graveyardWithSevenCards());

        assertThat(gqs.getEffectivePower(gd, wererat)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, wererat)).isEqualTo(baseToughness + 2);

        gd.playerGraveyards.get(player2.getId()).removeFirst();

        assertThat(gqs.getEffectivePower(gd, wererat)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, wererat)).isEqualTo(baseToughness);
    }

    private Permanent addAttacker() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        return attacker;
    }

    private Permanent addWererat() {
        return harness.addToBattlefieldAndReturn(player2, new DirtyWererat());
    }

    private void prepareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private List<Card> graveyardWithSevenCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
