package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.q.QuietPurity;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HearthKami.class, HondenOfInfiniteRage.class, HondenOfLifesWeb.class, QuietPurity.class})
class HondenOfInfiniteRageTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger deals damage to a player equal to the number of Shrines controlled")
    void dealsDamageToPlayerForEachShrine() {
        harness.addToBattlefield(player1, new HondenOfInfiniteRage());
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new HondenOfLifesWeb());
        resolveDamageTo(player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Upkeep trigger can deal its damage to a creature")
    void dealsDamageToCreature() {
        harness.addToBattlefield(player1, new HondenOfInfiniteRage());
        Permanent hearthKami = harness.addToBattlefieldAndReturn(player2, new HearthKami());

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new HondenOfLifesWeb());
        resolveDamageTo(hearthKami.getId());

        assertThat(gqs.findPermanentById(gd, hearthKami.getId())).isNull();
    }

    @Test
    @DisplayName("Shrines an opponent controls are not counted")
    void ignoresOpponentShrines() {
        harness.addToBattlefield(player1, new HondenOfInfiniteRage());
        harness.addToBattlefield(player2, new HondenOfLifesWeb());
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        resolveDamageTo(player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new HondenOfInfiniteRage());
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The trigger still deals damage after its source leaves, counting only remaining Shrines")
    void sourceLeavingDoesNotRemoveTrigger() {
        Permanent honden = harness.addToBattlefieldAndReturn(player1, new HondenOfInfiniteRage());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new QuietPurity()));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.addToBattlefield(player1, new HondenOfLifesWeb());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, honden.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Honden of Infinite Rage");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The trigger deals no damage when no Shrines remain at resolution")
    void noRemainingShrinesMeansZeroDamage() {
        Permanent honden = harness.addToBattlefieldAndReturn(player1, new HondenOfInfiniteRage());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new QuietPurity()));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, honden.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Honden of Infinite Rage");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller can be chosen as the damage target")
    void canTargetItsController() {
        harness.addToBattlefield(player1, new HondenOfInfiniteRage());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        resolveDamageTo(player1.getId());

        harness.assertLife(player1, 19);
    }

    private void resolveDamageTo(UUID targetId) {
        harness.handlePermanentChosen(player1, targetId);
        resolveAllTriggers();
    }
}
