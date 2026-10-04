package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FallajiVanguard.class, ArgothianSprite.class})
class FallajiVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry triggers and gives a target creature +2/+0")
    void ownEntryTriggers() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        harness.castFromHand(player1, new FallajiVanguard(), "{2}{R}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent boostedTarget = findPermanent(player2, target.getId());
        assertThat(boostedTarget.getPowerModifier()).isEqualTo(2);
        assertThat(boostedTarget.getToughnessModifier()).isZero();
        assertThat(boostedTarget.getEffectivePower()).isEqualTo(4);
        assertThat(boostedTarget.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Another creature entering under its controller's control triggers the ability")
    void anotherAllyEntryTriggers() {
        harness.addToBattlefield(player1, new FallajiVanguard());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());

        harness.castFromHand(player1, new ArgothianSprite(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The temporary power boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        harness.castFromHand(player1, new FallajiVanguard(), "{2}{R}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent boostedTarget = findPermanent(player2, target.getId());
        assertThat(boostedTarget.getPowerModifier()).isEqualTo(2);
        assertThat(boostedTarget.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        boostedTarget = findPermanent(player2, target.getId());
        assertThat(boostedTarget.getPowerModifier()).isZero();
        assertThat(boostedTarget.getToughnessModifier()).isZero();
        assertThat(boostedTarget.getEffectivePower()).isEqualTo(2);
        assertThat(boostedTarget.getEffectiveToughness()).isEqualTo(2);
    }

    private Permanent findPermanent(Player player, UUID id) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("Its own entry can target itself")
    void ownEntryCanTargetItself() {
        harness.castFromHand(player1, new FallajiVanguard(), "{2}{R}{W}");
        harness.passBothPriorities();
        UUID vanguardId = harness.getPermanentId(player1, "Fallaji Vanguard");
        harness.handlePermanentChosen(player1, vanguardId);
        harness.passBothPriorities();

        Permanent vanguard = findPermanent(player1, vanguardId);
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's creature entering does not trigger Vanguard")
    void opponentEntryDoesNotTrigger() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new FallajiVanguard());
        harness.enterBattlefieldAndReturn(player2, new ArgothianSprite());

        assertThat(gd.interaction.permanentChoiceContext()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(vanguard.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The entering ally can be chosen as the target")
    void enteringAllyCanBeTargeted() {
        harness.addToBattlefield(player1, new FallajiVanguard());
        Permanent ally = harness.enterBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(2);
    }

    @Test
    @DisplayName("First strike kills a blocker before it deals damage")
    void firstStrikePreventsBlockerDamage() {
        addCreatureReady(player1, new FallajiVanguard());
        harness.addToBattlefield(player2, new ArgothianSprite());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Argothian Sprite");
        harness.assertOnBattlefield(player1, "Fallaji Vanguard");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }
}
