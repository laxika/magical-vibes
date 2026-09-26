package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.j.JeditOjanen;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Karakas.class, JeditOjanen.class, BarbaryApes.class})
class KarakasTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Karakas adds one white mana")
    void tappingAddsWhiteMana() {
        harness.addToBattlefield(player1, new Karakas());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping Karakas returns a target legendary creature to its owner's hand")
    void returnsLegendaryCreatureToOwnersHand() {
        harness.addToBattlefield(player1, new Karakas());
        Permanent jedit = harness.addToBattlefieldAndReturn(player2, new JeditOjanen());

        harness.activateAbility(player1, 0, 1, null, jedit.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Jedit Ojanen");
        harness.assertNotOnBattlefield(player2, "Jedit Ojanen");
    }

    @Test
    @DisplayName("Karakas cannot target a nonlegendary creature")
    void cannotTargetNonlegendaryCreature() {
        harness.addToBattlefield(player1, new Karakas());
        Permanent apes = harness.addToBattlefieldAndReturn(player2, new BarbaryApes());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, apes.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    @Test
    @DisplayName("Karakas cannot target a legendary noncreature permanent")
    void cannotTargetLegendaryNoncreaturePermanent() {
        harness.addToBattlefield(player1, new Karakas());
        Permanent legendaryLand = harness.addToBattlefieldAndReturn(player2, new Karakas());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, legendaryLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }
}
