package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EunuchsIntrigues.class, ShuFootSoldiers.class})
class EunuchsIntriguesTest extends BaseCardTest {

    @Test
    @DisplayName("Chosen creature can still block; the target opponent's other creatures can't")
    void chosenCreatureCanBlockOthersCant() {
        Permanent kept = addCreatureReady(player2, new ShuFootSoldiers());
        Permanent other = addCreatureReady(player2, new ShuFootSoldiers());

        castEunuchsIntrigues();
        harness.handleMultiplePermanentsChosen(player2, List.of(kept.getId()));

        assertThat(kept.isCantBlockThisTurn()).isFalse();
        assertThat(other.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("A lone creature is never restricted (no other creatures)")
    void singleCreatureNotRestricted() {
        Permanent only = addCreatureReady(player2, new ShuFootSoldiers());

        castEunuchsIntrigues();
        harness.handleMultiplePermanentsChosen(player2, List.of(only.getId()));

        assertThat(only.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Resolves harmlessly when the target opponent controls no creatures")
    void noCreaturesResolvesHarmlessly() {
        castEunuchsIntrigues();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The controller's own creatures are unaffected")
    void controllersCreaturesUnaffected() {
        Permanent own = addCreatureReady(player1, new ShuFootSoldiers());
        Permanent kept = addCreatureReady(player2, new ShuFootSoldiers());
        Permanent other = addCreatureReady(player2, new ShuFootSoldiers());

        castEunuchsIntrigues();
        harness.handleMultiplePermanentsChosen(player2, List.of(kept.getId()));

        assertThat(own.isCantBlockThisTurn()).isFalse();
        assertThat(other.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("A restricted creature can't be declared as a blocker")
    void restrictedCreatureCantBlock() {
        addCreatureReady(player1, new ShuFootSoldiers());
        Permanent kept = addCreatureReady(player2, new ShuFootSoldiers());
        addCreatureReady(player2, new ShuFootSoldiers()); // the restricted blocker (index 1)

        castEunuchsIntrigues();
        harness.handleMultiplePermanentsChosen(player2, List.of(kept.getId()));

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The chosen creature can be declared as a blocker")
    void chosenCreatureCanBeDeclaredAsBlocker() {
        addCreatureReady(player1, new ShuFootSoldiers());
        Permanent kept = addCreatureReady(player2, new ShuFootSoldiers());
        addCreatureReady(player2, new ShuFootSoldiers());

        castEunuchsIntrigues();
        harness.handleMultiplePermanentsChosen(player2, List.of(kept.getId()));

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A creature entering later this turn also can't block")
    void laterCreatureCantBlock() {
        addCreatureReady(player1, new ShuFootSoldiers());
        Permanent kept = addCreatureReady(player2, new ShuFootSoldiers());

        castEunuchsIntrigues();
        harness.handleMultiplePermanentsChosen(player2, List.of(kept.getId()));

        addCreatureReady(player2, new ShuFootSoldiers());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can't target the caster's own player")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new EunuchsIntrigues()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent with creatures must choose exactly one")
    void cannotDeclineCreatureChoice() {
        Permanent kept = addCreatureReady(player2, new ShuFootSoldiers());
        addCreatureReady(player2, new ShuFootSoldiers());

        castEunuchsIntrigues();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player2, List.of(kept.getId()));
    }

    @Test
    @DisplayName("Creatures entering after resolution with no creatures still can't block")
    void laterCreatureCantBlockWhenNoCreatureWasChosen() {
        addCreatureReady(player1, new ShuFootSoldiers());
        castEunuchsIntrigues();
        addCreatureReady(player2, new ShuFootSoldiers());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castEunuchsIntrigues() {
        harness.setHand(player1, List.of(new EunuchsIntrigues()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

}
