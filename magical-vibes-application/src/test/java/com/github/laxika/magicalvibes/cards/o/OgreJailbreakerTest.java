package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CatacombSlug;
import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OgreJailbreaker.class, RakdosGuildgate.class, CatacombSlug.class})
class OgreJailbreakerTest extends BaseCardTest {

    private Permanent readyJailbreaker() {
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new OgreJailbreaker());
        ogre.setSummoningSick(false);
        harness.addToBattlefield(player2, new CatacombSlug());
        return ogre;
    }

    @Test
    @DisplayName("Cannot attack while you control no Gate")
    void cannotAttackWithoutGate() {
        Permanent ogre = readyJailbreaker();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ogre);

        assertThatThrownBy(() -> declareAttackers(List.of(index)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Can attack while you control a Gate")
    void canAttackWithGate() {
        Permanent ogre = readyJailbreaker();
        harness.addToBattlefield(player1, new RakdosGuildgate());
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ogre);

        declareAttackers(List.of(index));

        assertThat(ogre.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Gate does not let it attack")
    void opponentGateDoesNotHelp() {
        Permanent ogre = readyJailbreaker();
        harness.addToBattlefield(player2, new RakdosGuildgate());
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ogre);

        assertThatThrownBy(() -> declareAttackers(List.of(index)))
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

        assertThatThrownBy(() -> declareAttackers(List.of(index)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("A tapped Gate still permits attacking")
    void canAttackWithTappedGate() {
        Permanent ogre = readyJailbreaker();
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new RakdosGuildgate());
        gate.tap();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(ogre)));

        assertThat(ogre.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Controlling a Gate does not bypass summoning sickness")
    void cannotAttackWithSummoningSicknessEvenWithGate() {
        Permanent ogre = readyJailbreaker();
        ogre.setSummoningSick(true);
        harness.addToBattlefield(player1, new RakdosGuildgate());
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ogre);

        assertThatThrownBy(() -> declareAttackers(List.of(index)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("After changing control, the former controller's Gate does not permit attacking")
    void gatePermissionFollowsCurrentController() {
        Permanent ogre = readyJailbreaker();
        harness.addToBattlefield(player1, new RakdosGuildgate());
        gd.playerBattlefields.get(player1.getId()).remove(ogre);
        gd.playerBattlefields.get(player2.getId()).add(ogre);
        ogre.setSummoningSick(false);
        int index = gd.playerBattlefields.get(player2.getId()).indexOf(ogre);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(index)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }
}
