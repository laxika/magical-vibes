package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LilianasDevoteeTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts Zombies you control by +1/+0")
    void boostsOwnZombies() {
        harness.addToBattlefield(player1, new LilianasDevotee());
        harness.addToBattlefield(player1, new Gravecrawler());

        Permanent zombie = findPermanent(player1, "Gravecrawler");

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not boost an opponent's Zombie")
    void doesNotBoostOpponentsZombie() {
        harness.addToBattlefield(player1, new LilianasDevotee());
        harness.addToBattlefield(player2, new Gravecrawler());

        Permanent zombie = findPermanent(player2, "Gravecrawler");

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger at the end step when no creature died")
    void doesNotTriggerWithoutMorbid() {
        harness.addToBattlefield(player1, new LilianasDevotee());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    @DisplayName("Pays {1}{B} at the end step to create a Zombie")
    void paysToCreateZombie() {
        harness.addToBattlefield(player1, new LilianasDevotee());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    @Test
    @DisplayName("Declining the payment creates no Zombie")
    void declinesToCreateZombie() {
        harness.addToBattlefield(player1, new LilianasDevotee());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
