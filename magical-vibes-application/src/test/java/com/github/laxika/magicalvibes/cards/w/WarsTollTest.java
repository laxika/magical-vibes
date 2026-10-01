package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AssaultZeppelid;
import com.github.laxika.magicalvibes.cards.m.MasterOfCruelties;
import com.github.laxika.magicalvibes.cards.n.NovijenHeartOfProgress;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarsToll.class, NovijenHeartOfProgress.class, AssaultZeppelid.class, MasterOfCruelties.class})
class WarsTollTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent tapping a land for mana eventually taps all their lands")
    void opponentLandTapTapsAllTheirLands() {
        harness.addToBattlefield(player1, new WarsToll());
        Permanent tappedLand = harness.addToBattlefieldAndReturn(player2, new NovijenHeartOfProgress());
        Permanent otherLand = harness.addToBattlefieldAndReturn(player2, new NovijenHeartOfProgress());
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(tappedLand.isTapped()).isTrue();
        assertThat(otherLand.isTapped()).isFalse();

        resolveAllTriggers();

        assertThat(tappedLand.isTapped()).isTrue();
        assertThat(otherLand.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Tapping your own land does not trigger War's Toll")
    void ownLandTapDoesNotTrigger() {
        harness.addToBattlefield(player1, new WarsToll());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new NovijenHeartOfProgress());

        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
    }

    @Test
    @DisplayName("When one opponent creature attacks, all of that opponent's able creatures must attack")
    void opponentCreaturesMustAttackTogether() {
        harness.addToBattlefield(player1, new WarsToll());
        Permanent first = addReadyCreature(player2);
        Permanent second = addReadyCreature(player2);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(second.getCard().getName());
        assertThat(first.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("All able opponent creatures can attack together")
    void allAbleOpponentCreaturesCanAttackTogether() {
        harness.addToBattlefield(player1, new WarsToll());
        Permanent first = addReadyCreature(player2);
        Permanent second = addReadyCreature(player2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0, 1)));

        assertThat(first.isAttacking()).isTrue();
        assertThat(second.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("A creature unable to attack is not forced to attack")
    void creatureUnableToAttackIsNotForced() {
        harness.addToBattlefield(player1, new WarsToll());
        Permanent attacker = addReadyCreature(player2);
        Permanent summoningSick = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        summoningSick.setSummoningSick(true);

        assertThatCode(() -> harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)))).doesNotThrowAnyException();

        assertThat(attacker.isAttacking()).isTrue();
        assertThat(summoningSick.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("A creature that can attack only alone is not forced to join another attack")
    void creatureThatCanOnlyAttackAloneIsNotForcedToJoin() {
        harness.addToBattlefield(player1, new WarsToll());
        Permanent attacker = addReadyCreature(player2);
        Permanent canOnlyAttackAlone = addReadyCreature(player2, new MasterOfCruelties());

        assertThatCode(() -> harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2,
                        List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker)))))
                .doesNotThrowAnyException();

        assertThat(attacker.isAttacking()).isTrue();
        assertThat(canOnlyAttackAlone.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("War's Toll does not force its controller's creatures to attack together")
    void controllerCreaturesAreNotForcedTogether() {
        harness.addToBattlefield(player1, new WarsToll());
        Permanent first = addReadyCreature(player1);
        Permanent second = addReadyCreature(player1);

        assertThatCode(() -> harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(
                        gd.playerBattlefields.get(player1.getId()).indexOf(first)))))
                .doesNotThrowAnyException();
        assertThat(first.isAttacking()).isTrue();
        assertThat(second.isAttacking()).isFalse();
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        return addReadyCreature(player, new AssaultZeppelid());
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player,
                                       com.github.laxika.magicalvibes.model.Card card) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, card);
        creature.setSummoningSick(false);
        return creature;
    }
}
