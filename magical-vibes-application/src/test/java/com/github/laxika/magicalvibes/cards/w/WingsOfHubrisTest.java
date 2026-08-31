package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WingsOfHubris.class, GrizzlyBears.class})
class WingsOfHubrisTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has flying")
    void equippedCreatureHasFlying() {
        Permanent creature = addCreatureReady(player1);
        Permanent wings = addWingsReady(player1);
        wings.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Sacrifice ability makes the equipped creature unable to be blocked and sacrifices the Wings")
    void sacrificeAbilityMakesEquippedCreatureUnblockable() {
        Permanent creature = addCreatureReady(player1);
        Permanent wings = addWingsReady(player1);
        wings.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(creature.isCantBeBlocked()).isTrue();
        harness.assertInGraveyard(player1, "Wings of Hubris");
        harness.assertNotOnBattlefield(player1, "Wings of Hubris");
    }

    @Test
    @DisplayName("Equipped creature is sacrificed at the beginning of the next end step")
    void equippedCreatureIsSacrificedAtNextEndStep() {
        Permanent creature = addCreatureReady(player1);
        Permanent wings = addWingsReady(player1);
        wings.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The equipped creature is not sacrificed if its controller is an opponent")
    void doesNotSacrificeCreatureControlledByOpponent() {
        Permanent creature = addCreatureReady(player2);
        Permanent wings = addWingsReady(player1);
        wings.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()));
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    private Permanent addCreatureReady(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addWingsReady(Player player) {
        Permanent permanent = new Permanent(new WingsOfHubris());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
