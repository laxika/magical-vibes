package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OgreJailbreakerTest extends BaseCardTest {

    private Permanent readyJailbreaker() {
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new OgreJailbreaker());
        ogre.setSummoningSick(false);
        harness.addToBattlefield(player2, new GrizzlyBears());
        return ogre;
    }

    private void beginDeclareAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(player1.getId()));
    }

    @Test
    @DisplayName("Cannot attack while you control no Gate")
    void cannotAttackWithoutGate() {
        Permanent ogre = readyJailbreaker();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ogre);
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Can attack while you control a Gate")
    void canAttackWithGate() {
        Permanent ogre = readyJailbreaker();
        harness.addToBattlefield(player1, new RakdosGuildgate());
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ogre);
        beginDeclareAttackers();

        gs.declareAttackers(gd, player1, List.of(index));

        assertThat(ogre.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Gate does not let it attack")
    void opponentGateDoesNotHelp() {
        Permanent ogre = readyJailbreaker();
        harness.addToBattlefield(player2, new RakdosGuildgate());
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ogre);
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Cannot attack after the Gate leaves the battlefield")
    void cannotAttackAfterGateLeaves() {
        Permanent ogre = readyJailbreaker();
        harness.addToBattlefield(player1, new RakdosGuildgate());
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Rakdos Guildgate"));

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ogre);
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }
}
