package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeepwoodLegate.class, Forest.class, Swamp.class})
class DeepwoodLegateTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for free when an opponent controls a Forest and you control a Swamp")
    void castsForAlternateCost() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new DeepwoodLegate()));

        harness.castWithAlternateCost(player1, 0, (UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Deepwood Legate");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Free alternate cast does not spend mana")
    void alternateCostDoesNotSpendMana() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setHand(player1, List.of(new DeepwoodLegate()));

        harness.castWithAlternateCost(player1, 0, (UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Deepwood Legate");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot use the alternate cost without an opponent-controlled Forest")
    void alternateCostRequiresOpponentForest() {
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new DeepwoodLegate()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot use the alternate cost without a controller-controlled Swamp")
    void alternateCostRequiresControllerSwamp() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new DeepwoodLegate()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot use the alternate cost when the required lands are controlled by the wrong players")
    void alternateCostRequiresCorrectControllers() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Swamp());
        harness.setHand(player1, List.of(new DeepwoodLegate()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving the ability gives Deepwood Legate +1/+1")
    void resolvingAbilityBoosts() {
        Permanent legate = harness.addToBattlefieldAndReturn(player1, new DeepwoodLegate());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(legate.getEffectivePower()).isEqualTo(2);
        assertThat(legate.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Activated ability cannot be paid with colorless mana")
    void abilityRequiresBlackMana() {
        harness.addToBattlefieldAndReturn(player1, new DeepwoodLegate());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be cast normally for its mana cost")
    void castsNormally() {
        harness.castFromHand(player1, new DeepwoodLegate(), "{3}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Deepwood Legate");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
