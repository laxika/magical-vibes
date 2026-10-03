package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmoryGuard.class, RakdosGuildgate.class, AxebaneStag.class})
class ArmoryGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Has no vigilance while you control no Gate")
    void noVigilanceWithoutGate() {
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new ArmoryGuard());
        harness.addToBattlefield(player1, new AxebaneStag());

        assertThat(gqs.hasKeyword(gd, guard, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Has vigilance while you control a Gate")
    void vigilanceWithGate() {
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new ArmoryGuard());
        harness.addToBattlefield(player1, new RakdosGuildgate());

        assertThat(gqs.hasKeyword(gd, guard, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's Gate does not grant vigilance")
    void opponentGateDoesNotGrantVigilance() {
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new ArmoryGuard());
        harness.addToBattlefield(player2, new RakdosGuildgate());

        assertThat(gqs.hasKeyword(gd, guard, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Vigilance updates as Gates enter and the last Gate leaves")
    void vigilanceUpdatesWithGatePresence() {
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new ArmoryGuard());
        assertThat(gqs.hasKeyword(gd, guard, Keyword.VIGILANCE)).isFalse();

        Permanent firstGate = harness.addToBattlefieldAndReturn(player1, new RakdosGuildgate());
        firstGate.tap();
        assertThat(gqs.hasKeyword(gd, guard, Keyword.VIGILANCE)).isTrue();

        Permanent secondGate = harness.addToBattlefieldAndReturn(player1, new RakdosGuildgate());
        gd.playerBattlefields.get(player1.getId()).remove(firstGate);
        assertThat(gqs.hasKeyword(gd, guard, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(secondGate);
        assertThat(gqs.hasKeyword(gd, guard, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Vigilance depends on the Guard's current controller")
    void vigilanceUpdatesWhenControllerChanges() {
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new ArmoryGuard());
        harness.addToBattlefield(player1, new RakdosGuildgate());
        assertThat(gqs.hasKeyword(gd, guard, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(guard);
        gd.playerBattlefields.get(player2.getId()).add(guard);
        assertThat(gqs.hasKeyword(gd, guard, Keyword.VIGILANCE)).isFalse();

        harness.addToBattlefield(player2, new RakdosGuildgate());
        assertThat(gqs.hasKeyword(gd, guard, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("With a Gate, only Armory Guard stays untapped when attacking")
    void vigilanceAppliesOnlyToGuardDuringAttack() {
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new ArmoryGuard());
        Permanent stag = harness.addToBattlefieldAndReturn(player1, new AxebaneStag());
        harness.addToBattlefield(player1, new RakdosGuildgate());
        guard.setSummoningSick(false);
        stag.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0, 1));

        assertThat(guard.isTapped()).isFalse();
        assertThat(stag.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Without a Gate, attacking taps Armory Guard")
    void attacksTapGuardWithoutGate() {
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new ArmoryGuard());
        guard.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(guard.isTapped()).isTrue();
    }
}
