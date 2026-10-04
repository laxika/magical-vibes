package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BasriKet;
import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.r.RangersGuile;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FinishingBlow.class, AlpineWatchdog.class, BasriKet.class, Forest.class, RangersGuile.class})
class FinishingBlowTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target creature")
    void destroysTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        cast(target);

        harness.assertNotOnBattlefield(player2, "Alpine Watchdog");
        harness.assertInGraveyard(player2, "Alpine Watchdog");
    }

    @Test
    @DisplayName("Destroys a target planeswalker")
    void destroysTargetPlaneswalker() {
        Permanent target = harness.enterBattlefieldAndReturn(player2, new BasriKet());

        cast(target);

        harness.assertNotOnBattlefield(player2, "Basri Ket");
        harness.assertInGraveyard(player2, "Basri Ket");
    }

    @Test
    @DisplayName("Rejects a noncreature nonplaneswalker target")
    void rejectsNoncreatureNonplaneswalkerTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new FinishingBlow()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy its controller's creature")
    void destroysOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());

        cast(target);

        harness.assertNotOnBattlefield(player1, "Alpine Watchdog");
        harness.assertInGraveyard(player1, "Alpine Watchdog");
    }

    @Test
    @DisplayName("Does not destroy an indestructible creature")
    void doesNotDestroyIndestructibleCreature() {
        harness.enterBattlefieldAndReturn(player1, new BasriKet());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        cast(target);

        harness.assertOnBattlefield(player2, "Alpine Watchdog");
        harness.assertNotInGraveyard(player2, "Alpine Watchdog");
        harness.assertInGraveyard(player1, "Finishing Blow");
    }

    @Test
    @DisplayName("Does not resolve when its target gains hexproof in response")
    void targetGainingHexproofStopsDestruction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        harness.setHand(player1, List.of(new FinishingBlow()));
        harness.setHand(player2, List.of(new RangersGuile()));
        addMana();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Alpine Watchdog");
        harness.assertNotInGraveyard(player2, "Alpine Watchdog");
        harness.assertInGraveyard(player1, "Finishing Blow");
        harness.assertInGraveyard(player2, "Ranger's Guile");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new FinishingBlow()));
        addMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
