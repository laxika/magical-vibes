package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AvacynsPilgrim;
import com.github.laxika.magicalvibes.cards.w.WoodenStake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InfernalPlunge.class, AvacynsPilgrim.class, WoodenStake.class})
class InfernalPlungeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting sacrifices a creature and puts spell on stack")
    void castingSacrificesCreatureAndPutsOnStack() {
        setupAndCast();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Infernal Plunge");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.assertNotOnBattlefield(player1, "Avacyn's Pilgrim");
        harness.assertInGraveyard(player1, "Avacyn's Pilgrim");
    }

    @Test
    @DisplayName("Resolving adds three red mana to controller's mana pool")
    void resolvingAddsThreeRedMana() {
        setupAndCast();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        setupAndCast();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Infernal Plunge");
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        harness.addToBattlefield(player2, new AvacynsPilgrim());
        harness.setHand(player1, List.of(new InfernalPlunge()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AvacynsPilgrim());

        harness.setHand(player1, List.of(new InfernalPlunge()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("A tapped creature can pay the sacrifice cost")
    void canSacrificeTappedCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new AvacynsPilgrim());
        sacrifice.tap();
        harness.setHand(player1, List.of(new InfernalPlunge()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());

        harness.assertNotOnBattlefield(player1, "Avacyn's Pilgrim");
        harness.assertInGraveyard(player1, "Avacyn's Pilgrim");
        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot sacrifice a noncreature to pay the additional cost")
    void cannotSacrificeNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WoodenStake());
        harness.setHand(player1, List.of(new InfernalPlunge()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Wooden Stake");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    private void setupAndCast() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new AvacynsPilgrim());

        harness.setHand(player1, List.of(new InfernalPlunge()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
    }
}
