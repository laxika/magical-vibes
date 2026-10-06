package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AbattoirGhoul;
import com.github.laxika.magicalvibes.cards.s.StitchersApprentice;
import com.github.laxika.magicalvibes.cards.s.SkaabRuinator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RooftopStorm.class, AbattoirGhoul.class, StitchersApprentice.class, SkaabRuinator.class})
class RooftopStormTest extends BaseCardTest {


    @Test
    @DisplayName("Casting puts it on the stack as an enchantment spell")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new RooftopStorm(), "{5}{U}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Rooftop Storm");
    }

    @Test
    @DisplayName("Resolving puts Rooftop Storm onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.castFromHand(player1, new RooftopStorm(), "{5}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Rooftop Storm");
    }


    @Test
    @DisplayName("Zombie creature can be cast for free with Rooftop Storm on battlefield")
    void zombieCreatureCastForFree() {
        harness.addToBattlefield(player1, new RooftopStorm());
        // Abattoir Ghoul costs {3}{B} — with Rooftop Storm it should cost {0}
        harness.setHand(player1, List.of(new AbattoirGhoul()));
        // No mana added — should still be castable

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Abattoir Ghoul");
    }

    @Test
    @DisplayName("Zombie creature cast for free does not spend any mana")
    void zombieCreatureDoesNotSpendMana() {
        harness.addToBattlefield(player1, new RooftopStorm());
        harness.setHand(player1, List.of(new AbattoirGhoul()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);

        // All 5 black mana should still be in the pool
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(5);
    }

    @Test
    @DisplayName("Non-Zombie creature is not affected by Rooftop Storm")
    void nonZombieCreatureNotAffected() {
        harness.addToBattlefield(player1, new RooftopStorm());
        // Stitcher's Apprentice costs {1}{U} and is not a Zombie.
        harness.setHand(player1, List.of(new StitchersApprentice()));
        // No mana — should not be castable

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-creature spells are not affected by Rooftop Storm")
    void nonCreatureSpellNotAffected() {
        harness.addToBattlefield(player1, new RooftopStorm());
        // Another Rooftop Storm costs {5}{U} — enchantment, not a creature
        harness.setHand(player1, List.of(new RooftopStorm()));
        // No mana — should not be castable

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent's Zombie creatures are not affected by your Rooftop Storm")
    void opponentZombieNotAffected() {
        harness.addToBattlefield(player1, new RooftopStorm());
        // Opponent tries to cast a Zombie creature without mana
        harness.setHand(player2, List.of(new AbattoirGhoul()));
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rooftop Storm effect is removed when it leaves the battlefield")
    void effectRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new RooftopStorm());
        harness.setHand(player1, List.of(new AbattoirGhoul()));

        // Remove Rooftop Storm from battlefield
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Rooftop Storm"));

        // Now Zombie creature should not be free
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void multipleZombiesCanBeCastForFree() {
        harness.addToBattlefield(player1, new RooftopStorm());
        harness.setHand(player1, List.of(new AbattoirGhoul(), new AbattoirGhoul()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof AbattoirGhoul).hasSize(2);
    }

    @Test
    void mandatoryAdditionalExileCostStillRequired() {
        harness.addToBattlefield(player1, new RooftopStorm());
        harness.setHand(player1, List.of(new SkaabRuinator()));
        harness.setGraveyard(player1, List.of(new AbattoirGhoul(), new AbattoirGhoul()));
        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Skaab Ruinator");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void handCastPaysExileCostButNoMana() {
        harness.addToBattlefield(player1, new RooftopStorm());
        harness.setHand(player1, List.of(new SkaabRuinator()));
        harness.setGraveyard(player1, List.of(new AbattoirGhoul(), new AbattoirGhoul(), new AbattoirGhoul()));
        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Skaab Ruinator");
    }

    @Test
    void zeroCostAppliesToPermittedGraveyardCast() {
        harness.addToBattlefield(player1, new RooftopStorm());
        harness.setGraveyard(player1, List.of(new SkaabRuinator(),
                new AbattoirGhoul(), new AbattoirGhoul(), new AbattoirGhoul()));
        harness.castFromGraveyard(player1, 0, List.of(0, 1, 2));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Skaab Ruinator");
    }

    @Test
    void zeroCostDoesNotGrantGraveyardPermission() {
        harness.addToBattlefield(player1, new RooftopStorm());
        harness.setGraveyard(player1, List.of(new AbattoirGhoul()));
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Abattoir Ghoul");
    }
}
