package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.k.KjeldoranWarrior;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoldeviGolem.class, IcyManipulator.class, KjeldoranWarrior.class})
class SoldeviGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the upkeep trigger untaps the opponent's creature and the Golem")
    void acceptUntapsBoth() {
        Permanent golem = addReadyGolem(player1);
        golem.tap();
        Permanent target = addCreatureReady(player2, new KjeldoranWarrior());
        target.tap();

        triggerUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
        assertThat(golem.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the upkeep trigger leaves both creatures tapped")
    void declineLeavesBothTapped() {
        Permanent golem = addReadyGolem(player1);
        golem.tap();
        Permanent target = addCreatureReady(player2, new KjeldoranWarrior());
        target.tap();

        triggerUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isTrue();
        assertThat(golem.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Golem does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent golem = addReadyGolem(player1);
        golem.tap();
        Permanent other = addCreatureReady(player1, new KjeldoranWarrior());
        other.tap();

        advanceToUpkeep(player1);

        assertThat(golem.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An untapped creature an opponent controls is not a legal target")
    void untappedOpponentCreatureIsNotLegal() {
        Permanent golem = addReadyGolem(player1);
        golem.tap();
        Permanent untapped = addCreatureReady(player2, new KjeldoranWarrior());

        triggerUpkeep(player1);
        harness.passBothPriorities();

        assertThat(golem.isTapped()).isTrue();
        assertThat(untapped.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped creature you control is not a legal target")
    void ownTappedCreatureIsNotLegal() {
        Permanent golem = addReadyGolem(player1);
        golem.tap();
        Permanent own = addCreatureReady(player1, new KjeldoranWarrior());
        own.tap();

        triggerUpkeep(player1);
        harness.passBothPriorities();

        assertThat(golem.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped noncreature is not a legal target")
    void noncreatureIsNotLegalTarget() {
        Permanent golem = addReadyGolem(player1);
        golem.tap();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        artifact.tap();

        triggerUpkeep(player1);
        harness.passBothPriorities();

        assertThat(golem.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("If the target becomes untapped before resolution, the Golem stays tapped")
    void targetBecomesUntappedBeforeResolution() {
        Permanent golem = addReadyGolem(player1);
        golem.tap();
        Permanent target = addCreatureReady(player2, new KjeldoranWarrior());
        target.tap();

        triggerUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        target.untap();
        harness.passBothPriorities();

        assertThat(golem.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The upkeep ability does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent golem = addReadyGolem(player1);
        golem.tap();
        Permanent target = addCreatureReady(player2, new KjeldoranWarrior());

        harness.performUntapStep(player2);
        target.tap();
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(golem.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyGolem(Player player) {
        Permanent perm = new Permanent(new SoldeviGolem());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void triggerUpkeep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
