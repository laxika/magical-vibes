package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FiendBinder.class})
class FiendBinderTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking taps the chosen defending creature")
    void attackTriggerTapsTargetCreature() {
        addCreatureReady(player1, new FiendBinder());
        Permanent target = addCreatureReady(player2, new FiendBinder());

        declareAttackers(player1, List.of(0));
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature the attacking player controls is not a legal target")
    void ownCreatureIsIllegalTarget() {
        addCreatureReady(player1, new FiendBinder());
        Permanent ownCreature = addCreatureReady(player1, new FiendBinder());
        Permanent target = addCreatureReady(player2, new FiendBinder());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(ownCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("No target selection occurs when the defending player controls no creatures")
    void noLegalTargetSkipsTrigger() {
        addCreatureReady(player1, new FiendBinder());

        declareAttackers(player1, List.of(0));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
    }

    @Test
    @DisplayName("An already tapped defending creature remains a legal target")
    void tappedCreatureIsLegalTarget() {
        addCreatureReady(player1, new FiendBinder());
        Permanent target = addCreatureReady(player2, new FiendBinder());
        target.tap();

        declareAttackers(player1, List.of(0));
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger taps only the chosen creature")
    void onlyChosenCreatureIsTapped() {
        addCreatureReady(player1, new FiendBinder());
        Permanent target = addCreatureReady(player2, new FiendBinder());
        Permanent other = addCreatureReady(player2, new FiendBinder());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The attack trigger resolves after its source leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent source = addCreatureReady(player1, new FiendBinder());
        Permanent target = addCreatureReady(player2, new FiendBinder());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        assertThat(target.isTapped()).isTrue();
    }
}
