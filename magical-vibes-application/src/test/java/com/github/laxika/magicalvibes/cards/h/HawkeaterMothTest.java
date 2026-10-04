package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.w.WizardMentor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HawkeaterMoth.class, HeatRay.class, WizardMentor.class})
class HawkeaterMothTest extends BaseCardTest {

    @Test
    @DisplayName("A creature without flying or reach cannot block Hawkeater Moth")
    void nonflyingCreatureCannotBlock() {
        addCreatureReady(player1, new HawkeaterMoth());
        addCreatureReady(player2, new WizardMentor());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flying creatures can block Hawkeater Moth despite shroud")
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new HawkeaterMoth());
        Permanent blocker = addCreatureReady(player2, new HawkeaterMoth());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Hawkeater Moth cannot be targeted by a spell")
    void cannotBeTargetedBySpell() {
        Permanent moth = harness.addToBattlefieldAndReturn(player1, new HawkeaterMoth());
        harness.setHand(player2, List.of(new HeatRay()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 1, moth.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Shroud also prevents its controller's spells from targeting Hawkeater Moth")
    void cannotBeTargetedByControllerSpell() {
        Permanent moth = harness.addToBattlefieldAndReturn(player1, new HawkeaterMoth());
        harness.setHand(player1, List.of(new HeatRay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, moth.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Hawkeater Moth cannot be targeted by an activated ability")
    void cannotBeTargetedByAbility() {
        addCreatureReady(player1, new WizardMentor());
        Permanent moth = addCreatureReady(player1, new HawkeaterMoth());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, moth.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
