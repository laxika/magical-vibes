package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FireAmbush;
import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeavyFog.class, ForestBear.class, FireAmbush.class})
class HeavyFogTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage from an attacking creature to you is prevented")
    void preventsCombatDamageFromAttacker() {
        addCreatureReady(player1, new ForestBear());
        harness.setHand(player2, List.of(new HeavyFog()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        declareAttackers(player1, List.of(0));

        int defenderLifeBefore = gd.getLife(player2.getId());

        harness.castInstant(player2, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        // Advance to combat damage: the unblocked Forest Bear would deal 2 to player2.
        resolveCombat(player1);

        // All damage from the attacking creature is prevented.
        assertThat(gd.getLife(player2.getId())).isEqualTo(defenderLifeBefore);
    }

    @Test
    @DisplayName("Does not prevent damage from a noncreature source")
    void doesNotPreventNoncreatureDamage() {
        addCreatureReady(player1, new ForestBear());
        harness.setHand(player2, List.of(new HeavyFog()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        declareAttackers(player1, List.of(0));
        harness.castAndResolveInstant(player2, 0);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new FireAmbush()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int defenderLifeBefore = gd.getLife(player2.getId());
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(defenderLifeBefore - 3);
    }

    @Test
    @DisplayName("Cannot cast if not attacked this step")
    void cannotCastWhenNotAttacked() {
        addCreatureReady(player2, new ForestBear());
        declareAttackers(player2, List.of(0));
        harness.setHand(player2, List.of(new HeavyFog()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast outside the declare attackers step")
    void cannotCastOutsideDeclareAttackers() {
        addCreatureReady(player1, new ForestBear());
        declareAttackers(player1, List.of(0));
        harness.setHand(player2, List.of(new HeavyFog()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
