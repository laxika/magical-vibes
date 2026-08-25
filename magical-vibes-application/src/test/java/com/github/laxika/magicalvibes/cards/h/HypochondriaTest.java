package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Hypochondria.class, GrizzlyBears.class})
class HypochondriaTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card prevents the next 3 damage to a target")
    void discardAbilityPreventsThreeDamage() {
        addHypochondria();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(3);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing Hypochondria prevents the next 3 damage to a target")
    void sacrificeAbilityPreventsThreeDamage() {
        addHypochondria();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(3);
        harness.assertInGraveyard(player1, "Hypochondria");
    }

    @Test
    @DisplayName("Hypochondria cannot target an enchantment")
    void cannotTargetEnchantment() {
        Permanent hypochondria = addHypochondria();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, hypochondria.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addHypochondria() {
        return harness.addToBattlefieldAndReturn(player1, new Hypochondria());
    }
}
