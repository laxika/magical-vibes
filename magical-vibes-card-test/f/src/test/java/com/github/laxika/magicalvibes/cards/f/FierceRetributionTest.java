package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.t.TravelingMinister;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FierceRetribution.class, TravelingMinister.class})
class FierceRetributionTest extends BaseCardTest {

    @Test
    @DisplayName("Normal cast destroys an attacking creature")
    void normalCastDestroysAttackingCreature() {
        Permanent target = attackingCreature();
        harness.setHand(player1, List.of(new FierceRetribution()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Traveling Minister");
        harness.assertInGraveyard(player2, "Traveling Minister");
    }

    @Test
    @DisplayName("Normal cast cannot target a nonattacking creature")
    void normalCastRejectsNonattackingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TravelingMinister());
        harness.setHand(player1, List.of(new FierceRetribution()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    @Test
    @DisplayName("Cleave cast destroys a nonattacking creature")
    void cleaveCastDestroysNonattackingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TravelingMinister());
        harness.setHand(player1, List.of(new FierceRetribution()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Traveling Minister");
        harness.assertInGraveyard(player2, "Traveling Minister");
    }

    @Test
    @DisplayName("Normal cast does not destroy a creature that stops attacking before resolution")
    void normalCastRechecksAttackingStatus() {
        Permanent target = attackingCreature();
        harness.setHand(player1, List.of(new FierceRetribution()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        target.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Traveling Minister");
        harness.assertNotInGraveyard(player2, "Traveling Minister");
        harness.assertInGraveyard(player1, "Fierce Retribution");
    }

    @Test
    @DisplayName("Cleave cannot be paid with only the normal mana cost")
    void cleaveRequiresFullAlternateCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TravelingMinister());
        harness.setHand(player1, List.of(new FierceRetribution()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Traveling Minister");
        harness.assertInHand(player1, "Fierce Retribution");
    }

    @Test
    @DisplayName("Cleave can destroy a creature controlled by its caster")
    void cleaveCanDestroyOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());
        harness.setHand(player1, List.of(new FierceRetribution()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Traveling Minister");
        harness.assertInGraveyard(player1, "Traveling Minister");
    }

    private Permanent attackingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new TravelingMinister());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        return attacker;
    }
}
