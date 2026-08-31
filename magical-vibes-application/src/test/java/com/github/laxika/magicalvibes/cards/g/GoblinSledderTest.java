package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinSledder.class, GoblinPiker.class, com.github.laxika.magicalvibes.cards.g.GrizzlyBears.class,
        Forest.class})
class GoblinSledderTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Goblin gives target creature +1/+1 until end of turn")
    void sacrificingGoblinBoostsTargetCreature() {
        addSledderReady(player1);
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, goblin.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Piker");
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard() instanceof GoblinSledder);
    }

    @Test
    @DisplayName("Goblin Sledder can sacrifice itself when it is the only Goblin")
    void canSacrificeItself() {
        addSledderReady(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Sledder");
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Goblin Sledder's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addSledderReady(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Goblin Sledder cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addSledderReady(player1);
        harness.addToBattlefield(player1, new GoblinPiker());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addSledderReady(Player player) {
        GoblinSledder card = new GoblinSledder();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
