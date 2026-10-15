package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MakeAStand;
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

@CardUsed({ImmolatingGlare.class, GrizzlyBears.class, MakeAStand.class})
class ImmolatingGlareTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target attacking creature")
    void destroysAttackingCreature() {
        Permanent attacker = addAttacker(player2);

        prepareGlare();
        harness.castAndResolveInstant(player1, 0, attacker.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        prepareGlare();

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

    @Test
    @DisplayName("Can destroy its controller's own attacking creature")
    void destroysOwnAttackingCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        prepareGlare();
        harness.castAndResolveInstant(player1, 0, attacker.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not destroy an attacker that gains indestructible in response")
    void respectsIndestructibleGainedInResponse() {
        Permanent attacker = addAttacker(player2);
        castGlare(attacker);

        harness.setHand(player2, List.of(new MakeAStand()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Immolating Glare");
        assertThat(gd.stack).isEmpty();
    }

    private void prepareGlare() {
        harness.setHand(player1, List.of(new ImmolatingGlare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castGlare(Permanent target) {
        prepareGlare();
        harness.castInstant(player1, 0, target.getId());
    }

    private Permanent addAttacker(Player owner) {
        Permanent attacker = addCreatureReady(owner, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }
}
