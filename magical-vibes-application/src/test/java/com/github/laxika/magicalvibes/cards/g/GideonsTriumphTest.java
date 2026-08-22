package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GideonsTriumph.class, GideonBlackblade.class, GrizzlyBears.class, GiantSpider.class})
class GideonsTriumphTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent chooses one creature that attacked or blocked this turn")
    void opponentChoosesOneAttackedOrBlockedCreature() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        blocker.setBlocking(true);
        Permanent untouched = addCreatureReady(player2, new GrizzlyBears());

        castTriumph(player2);

        harness.handleMultiplePermanentsChosen(player2, List.of(attacker.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker, untouched);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A Gideon planeswalker makes the opponent sacrifice two eligible creatures")
    void gideonMakesOpponentSacrificeTwo() {
        harness.addToBattlefield(player1, new GideonBlackblade());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        blocker.setBlocking(true);
        Permanent untouched = addCreatureReady(player2, new GrizzlyBears());

        castTriumph(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker, blocker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(untouched);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("The Gideon condition is checked as the spell resolves")
    void gideonConditionIsCheckedOnResolution() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        blocker.setBlocking(true);

        harness.setHand(player1, List.of(new GideonsTriumph()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new GideonBlackblade());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker, blocker);
    }

    @Test
    @DisplayName("Creatures that did not attack or block this turn are not eligible")
    void untouchedCreatureIsNotEligible() {
        Permanent untouched = addCreatureReady(player2, new GrizzlyBears());

        castTriumph(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(untouched);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Gideon's Triumph can target only an opponent")
    void onlyTargetsOpponent() {
        harness.setHand(player1, List.of(new GideonsTriumph()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castTriumph(Player targetPlayer) {
        harness.setHand(player1, List.of(new GideonsTriumph()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, targetPlayer.getId());
        harness.passBothPriorities();
    }
}
