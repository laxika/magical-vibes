package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImmolatingGlare.class, GrizzlyBears.class})
class ImmolatingGlareTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target attacking creature")
    void destroysAttackingCreature() {
        Permanent attacker = addAttacker(player2);

        castGlare(attacker);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ImmolatingGlare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    @Test
    @DisplayName("Fizzles if the target stops attacking before resolution")
    void fizzlesIfTargetStopsAttacking() {
        Permanent attacker = addAttacker(player2);

        castGlare(attacker);
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
    }

    private void castGlare(Permanent target) {
        harness.setHand(player1, List.of(new ImmolatingGlare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
    }

    private Permanent addAttacker(Player owner) {
        Permanent attacker = addCreatureReady(owner, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }
}
