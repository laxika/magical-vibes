package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinBoomKeg.class, GrizzlyBears.class, Naturalize.class})
class GoblinBoomKegTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself at upkeep and deals 3 damage to a target player")
    void sacrificesAtUpkeepAndDamagesPlayer() {
        harness.addToBattlefield(player1, new GoblinBoomKeg());
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Boom Keg");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Its graveyard trigger deals 3 damage to a target creature")
    void graveyardTriggerDamagesCreature() {
        harness.addToBattlefield(player1, new GoblinBoomKeg());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not sacrifice itself during an opponent's upkeep")
    void survivesOpponentsUpkeep() {
        harness.addToBattlefield(player1, new GoblinBoomKeg());

        advanceToUpkeep(player2);

        harness.assertOnBattlefield(player1, "Goblin Boom Keg");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Triggers when destroyed outside upkeep")
    void destructionTriggersDamage() {
        Permanent keg = harness.addToBattlefieldAndReturn(player1, new GoblinBoomKeg());
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, keg.getId());
        harness.assertInGraveyard(player1, "Goblin Boom Keg");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Can deal its damage to its own controller")
    void canTargetController() {
        harness.addToBattlefield(player1, new GoblinBoomKeg());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }
}
