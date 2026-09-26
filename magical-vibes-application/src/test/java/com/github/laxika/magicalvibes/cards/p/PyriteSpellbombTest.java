package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PyriteSpellbomb.class, Ornithopter.class})
class PyriteSpellbombTest extends BaseCardTest {

    @Test
    @DisplayName("The red ability deals 2 damage to any target")
    void dealsDamageToAnyTarget() {
        harness.addToBattlefield(player1, new PyriteSpellbomb());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertInGraveyard(player1, "Pyrite Spellbomb");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pyrite Spellbomb");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The red ability can deal damage to a creature")
    void dealsDamageToCreature() {
        harness.addToBattlefield(player1, new PyriteSpellbomb());
        var target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("The red ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new PyriteSpellbomb());
        var target = harness.addToBattlefieldAndReturn(player2, new PyriteSpellbomb());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Pyrite Spellbomb");
    }

    @Test
    @DisplayName("The colorless ability draws a card")
    void drawsACard() {
        harness.addToBattlefield(player1, new PyriteSpellbomb());
        harness.setLibrary(player1, List.of(new Ornithopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pyrite Spellbomb");
        harness.assertInHand(player1, "Ornithopter");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }
}
