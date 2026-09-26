package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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
        Permanent jin = addReady(player1, new JinSakaiGhostOfTsushima());

        declareAttackers(player1, List.of(indexOf(player1, jin)));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "Standoff — It gains double strike until end of turn");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, jin, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void ghostModeMakesTheAttackerUnblockable() {
        addReady(player1, new JinSakaiGhostOfTsushima());
        Permanent attacker = addReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(indexOf(player1, attacker)));
        harness.passBothPriorities();
        assertThat(gd.pendingEffectResolutionEntry).isNotNull();
        assertThat(gd.pendingEffectResolutionEntry.getTriggeringPermanentId()).isEqualTo(attacker.getId());
        harness.handleListChoice(player1, "Ghost — It can't be blocked this turn");
        assertThat(attacker.isCantBeBlocked()).isTrue();

        assertThat(gqs.hasCantBeBlocked(gd, attacker)).isTrue();
    }

    @Test
    void modeDoesNotTriggerWhenAnotherCreatureAttacksTheSamePlayer() {
        addReady(player1, new JinSakaiGhostOfTsushima());
        addReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    void combatDamageDrawsACard() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent jin = addReady(player1, new JinSakaiGhostOfTsushima());
        jin.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
