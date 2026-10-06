package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JinSakaiGhostOfTsushima.class, GrizzlyBears.class})
class JinSakaiGhostOfTsushimaTest extends BaseCardTest {

    @Test
    void standoffModeGrantsDoubleStrikeToTheAttacker() {
        Permanent jin = addCreatureReady(player1, new JinSakaiGhostOfTsushima());

        declareAttackers(player1, List.of(indexOf(player1, jin)));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "Standoff — It gains double strike until end of turn");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, jin, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void ghostModeMakesTheAttackerUnblockable() {
        addCreatureReady(player1, new JinSakaiGhostOfTsushima());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(indexOf(player1, attacker)));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Ghost — It can't be blocked this turn");
        harness.passBothPriorities();
        assertThat(attacker.isCantBeBlocked()).isTrue();

        assertThat(gqs.hasCantBeBlocked(gd, attacker)).isTrue();
    }

    @Test
    void modeDoesNotTriggerWhenAnotherCreatureAttacksTheSamePlayer() {
        addCreatureReady(player1, new JinSakaiGhostOfTsushima());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    void combatDamageDrawsACard() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent jin = addCreatureReady(player1, new JinSakaiGhostOfTsushima());
        jin.setAttacking(true);
        jin.setAttackTarget(player2.getId());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void attackAbilityDoesNothingIfAnotherCreatureIsAttackingThatPlayerOnResolution() {
        addCreatureReady(player1, new JinSakaiGhostOfTsushima());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(indexOf(player1, attacker)));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Ghost — It can't be blocked this turn");

        Permanent additionalAttacker = addCreatureReady(player1, new GrizzlyBears());
        additionalAttacker.tap();
        additionalAttacker.setAttacking(true);
        additionalAttacker.setAttackTarget(player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, attacker)).isFalse();
    }

    @Test
    void anotherCreaturesCombatDamageDoesNotDrawACard() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCreatureReady(player1, new JinSakaiGhostOfTsushima());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player2, 18);
    }

    @Test
    void opponentsAttackDoesNotTriggerEitherMode() {
        addCreatureReady(player1, new JinSakaiGhostOfTsushima());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
