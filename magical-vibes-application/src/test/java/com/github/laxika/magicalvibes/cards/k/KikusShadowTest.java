package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.h.HandOfCruelty;
import com.github.laxika.magicalvibes.cards.m.MikokoroCenterOfTheSea;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KikusShadow.class, HandOfCruelty.class, MikokoroCenterOfTheSea.class})
class KikusShadowTest extends BaseCardTest {

    @Test
    @DisplayName("Kiku's Shadow destroys a creature when its power is lethal")
    void destroysCreatureWhenPowerIsLethal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HandOfCruelty());
        harness.setHand(player1, List.of(new KikusShadow()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hand of Cruelty");
        harness.assertInGraveyard(player2, "Hand of Cruelty");
    }

    @Test
    @DisplayName("Kiku's Shadow marks damage equal to the target's power")
    void marksDamageEqualToPower() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HandOfCruelty());
        harness.setHand(player1, List.of(new KikusShadow()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Kiku's Shadow can target a creature its caster controls")
    void canTargetCreatureItsCasterControls() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HandOfCruelty());
        harness.setHand(player1, List.of(new KikusShadow()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Kiku's Shadow cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new MikokoroCenterOfTheSea());
        harness.setHand(player1, List.of(new KikusShadow()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a creature");

        harness.assertOnBattlefield(player2, "Mikokoro, Center of the Sea");
    }
}
