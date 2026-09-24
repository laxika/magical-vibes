package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TriceratonRegenta.class, GrizzlyBears.class})
class TriceratonRegentaTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers another creature as a target and untaps it")
    void attackTriggerUntapsAnotherCreature() {
        Permanent regenta = addCreatureReady(player1, new TriceratonRegenta());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        ally.tap();
        opponent.tap();

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(ally.getId(), opponent.getId())
                .doesNotContain(regenta.getId());

        harness.handlePermanentChosen(player1, opponent.getId());
        harness.passBothPriorities();

        assertThat(opponent.isTapped()).isFalse();
        assertThat(ally.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The attack trigger cannot target Triceraton Regenta itself")
    void cannotTargetItself() {
        Permanent regenta = addCreatureReady(player1, new TriceratonRegenta());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, regenta.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }
}
