package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PincherBeetles.class, Boomerang.class, GrizzlyBears.class, ProdigalPyromancer.class, WrathOfGod.class})
class PincherBeetlesTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Pincher Beetles puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new PincherBeetles()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard()).isInstanceOf(PincherBeetles.class);
    }

    @Test
    @DisplayName("Opponent spells cannot target Pincher Beetles")
    void opponentSpellsCannotTarget() {
        harness.forceActivePlayer(player2);
        Permanent beetles = harness.addToBattlefieldAndReturn(player1, new PincherBeetles());
        // Add valid target so spell is playable
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, beetles.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Opponent activated abilities cannot target Pincher Beetles")
    void opponentAbilitiesCannotTarget() {
        Permanent beetles = addCreatureReady(player1, new PincherBeetles());
        addCreatureReady(player2, new ProdigalPyromancer());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, beetles.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Untargeted effects can affect Pincher Beetles")
    void untargetedEffectsCanAffect() {
        harness.addToBattlefield(player1, new PincherBeetles());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pincher Beetles");
        harness.assertInGraveyard(player1, "Pincher Beetles");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Your own spells cannot target Pincher Beetles")
    void ownSpellsCannotTarget() {
        Permanent beetles = harness.addToBattlefieldAndReturn(player1, new PincherBeetles());
        // Add valid target so spell is playable
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, beetles.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Your own activated abilities cannot target Pincher Beetles")
    void ownAbilitiesCannotTarget() {
        Permanent beetles = addCreatureReady(player1, new PincherBeetles());
        addCreatureReady(player1, new ProdigalPyromancer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, beetles.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
