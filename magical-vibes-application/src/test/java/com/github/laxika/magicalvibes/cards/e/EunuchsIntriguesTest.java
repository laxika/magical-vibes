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
        Permanent attacker = addCreatureReady(player1, new ShuFootSoldiers());
        Permanent kept = addCreatureReady(player2, new ShuFootSoldiers());
        addCreatureReady(player2, new ShuFootSoldiers()); // the restricted blocker (index 1)

        castEunuchsIntrigues();
        harness.handleMultiplePermanentsChosen(player2, List.of(kept.getId()));

        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The chosen creature can be declared as a blocker")
    void chosenCreatureCanBeDeclaredAsBlocker() {
        Permanent attacker = addCreatureReady(player1, new ShuFootSoldiers());
        Permanent kept = addCreatureReady(player2, new ShuFootSoldiers());
        addCreatureReady(player2, new ShuFootSoldiers());

        castEunuchsIntrigues();
        harness.handleMultiplePermanentsChosen(player2, List.of(kept.getId()));

        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A creature entering later this turn also can't block")
    void laterCreatureCantBlock() {
        Permanent attacker = addCreatureReady(player1, new ShuFootSoldiers());
        Permanent kept = addCreatureReady(player2, new ShuFootSoldiers());

        castEunuchsIntrigues();
        harness.handleMultiplePermanentsChosen(player2, List.of(kept.getId()));

        addCreatureReady(player2, new ShuFootSoldiers());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

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

    private void castEunuchsIntrigues() {
        harness.setHand(player1, List.of(new EunuchsIntrigues()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

}
