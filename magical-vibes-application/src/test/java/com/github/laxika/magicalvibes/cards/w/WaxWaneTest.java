package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CollectiveRestraint;
import com.github.laxika.magicalvibes.cards.k.KavuTitan;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WaxWane.class, KavuTitan.class, CollectiveRestraint.class})
class WaxWaneTest extends BaseCardTest {

    @Test
    @DisplayName("Wax gives a target creature +2/+2 until end of turn")
    void waxBoostsCreatureUntilEndOfTurn() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new KavuTitan());
        harness.setHand(player1, List.of(new WaxWane()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, 0, kavu.getId());
        harness.passBothPriorities();

        assertThat(kavu.getPowerModifier()).isEqualTo(2);
        assertThat(kavu.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(kavu.getPowerModifier()).isZero();
        assertThat(kavu.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Wax can target an opponent's creature")
    void waxBoostsOpponentsCreature() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new KavuTitan());
        harness.setHand(player1, List.of(new WaxWane()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, 0, kavu.getId());
        harness.passBothPriorities();

        assertThat(kavu.getPowerModifier()).isEqualTo(2);
        assertThat(kavu.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Wane destroys the targeted enchantment")
    void waneDestroysEnchantment() {
        harness.addToBattlefield(player2, new CollectiveRestraint());
        harness.setHand(player1, List.of(new WaxWane()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Collective Restraint");
        harness.castInstant(player1, 0, 1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Collective Restraint");
        harness.assertInGraveyard(player2, "Collective Restraint");
    }

    @Test
    @DisplayName("Wane can target your own enchantment")
    void waneDestroysYourOwnEnchantment() {
        harness.addToBattlefield(player1, new CollectiveRestraint());
        harness.setHand(player1, List.of(new WaxWane()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Collective Restraint");
        harness.castInstant(player1, 0, 1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Collective Restraint");
        harness.assertInGraveyard(player1, "Collective Restraint");
    }

    @Test
    @DisplayName("Wax cannot target a noncreature permanent")
    void waxCannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new CollectiveRestraint());
        harness.setHand(player1, List.of(new WaxWane()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Collective Restraint");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Wane cannot target a non-enchantment permanent")
    void waneCannotTargetNonEnchantmentPermanent() {
        harness.addToBattlefield(player2, new KavuTitan());
        harness.setHand(player1, List.of(new WaxWane()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Kavu Titan");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
