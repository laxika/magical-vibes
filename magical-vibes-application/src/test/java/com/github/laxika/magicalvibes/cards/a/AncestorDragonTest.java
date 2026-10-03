package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.b.BurstLightning;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AncestorDragon.class, BearCub.class, BurstLightning.class})
class AncestorDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life for each creature attacking")
    void gainsLifeForEachAttackingCreature() {
        harness.addToBattlefield(player1, new AncestorDragon());

        addCreatureReady(player1, new BearCub());
        addCreatureReady(player1, new BearCub());

        harness.setLife(player1, 20);
        declareAttackers(List.of(1, 2));

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Does not trigger when no creatures attack")
    void doesNotTriggerWithoutAttackers() {
        harness.addToBattlefield(player1, new AncestorDragon());
        harness.addToBattlefieldAndReturn(player1, new BearCub());

        harness.setLife(player1, 20);
        declareAttackers(List.of());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The Dragon counts itself when it attacks")
    void countsItselfWhenAttacking() {
        addCreatureReady(player1, new AncestorDragon());
        harness.setLife(player1, 20);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Opposing creatures attacking do not trigger the Dragon")
    void doesNotTriggerForOpponentAttack() {
        harness.addToBattlefield(player1, new AncestorDragon());
        addCreatureReady(player2, new BearCub());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each Dragon triggers once for the whole attacking group")
    void multipleDragonsEachGainLife() {
        harness.addToBattlefield(player1, new AncestorDragon());
        harness.addToBattlefield(player1, new AncestorDragon());
        addCreatureReady(player1, new BearCub());
        addCreatureReady(player1, new BearCub());
        harness.setLife(player1, 20);

        declareAttackers(List.of(2, 3));
        resolveAllTriggers();

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("An attacker destroyed in response no longer counts for life gain")
    void countsAttackersAtResolution() {
        harness.addToBattlefield(player1, new AncestorDragon());
        Permanent attacker = addCreatureReady(player1, new BearCub());
        addCreatureReady(player1, new BearCub());
        harness.setHand(player2, List.of(new BurstLightning()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        declareAttackers(List.of(1, 2));
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player2, 0, attacker.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Bear Cub");
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("No life is gained if the only attacker is destroyed in response")
    void gainsNoLifeAfterLastAttackerLeaves() {
        harness.addToBattlefield(player1, new AncestorDragon());
        Permanent attacker = addCreatureReady(player1, new BearCub());
        harness.setHand(player2, List.of(new BurstLightning()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player2, 0, attacker.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Bear Cub");
        harness.assertLife(player1, 20);
    }

}
