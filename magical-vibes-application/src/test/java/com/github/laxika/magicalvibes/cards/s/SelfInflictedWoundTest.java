package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SelfInflictedWound.class, GrizzlyBears.class, SavannahLions.class, AirElemental.class})
class SelfInflictedWoundTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent sacrifices a green or white creature and loses 2 life")
    void sacrificesGreenOrWhiteCreatureAndLosesLife() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent lions = harness.addToBattlefieldAndReturn(player2, new SavannahLions());
        harness.addToBattlefield(player2, new AirElemental());

        cast();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(bears.getId(), lions.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(lions.getId()));

        harness.assertInGraveyard(player2, "Savannah Lions");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Does not cause life loss when the target opponent controls no green or white creature")
    void noMatchingCreatureMeansNoLifeLoss() {
        harness.addToBattlefield(player2, new AirElemental());

        cast();

        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new SelfInflictedWound()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast() {
        harness.setHand(player1, List.of(new SelfInflictedWound()));
        addMana();
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
