package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AvenTrooper;
import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Hypochondria.class, AvenTrooper.class, FieryTemper.class})
class HypochondriaTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card prevents the next 3 damage to a target")
    void discardAbilityPreventsThreeDamage() {
        addHypochondria();
        harness.setHand(player1, List.of(
                new Hypochondria(), new FieryTemper(), new FieryTemper()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player1, "Hypochondria");
    }

    @Test
    @DisplayName("Sacrificing Hypochondria prevents the next 3 damage to a target")
    void sacrificeAbilityPreventsThreeDamage() {
        addHypochondria();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvenTrooper());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new FieryTemper()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Aven Trooper");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Hypochondria");
    }

    @Test
    @DisplayName("Discard ability requires a card in hand")
    void discardAbilityRequiresCardInHand() {
        addHypochondria();
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Hypochondria cannot target an enchantment")
    void cannotTargetEnchantment() {
        Permanent hypochondria = addHypochondria();
        harness.setHand(player1, List.of(new Hypochondria()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, hypochondria.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addHypochondria() {
        return harness.addToBattlefieldAndReturn(player1, new Hypochondria());
    }
}
