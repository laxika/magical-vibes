package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.l.LuxaRiverShrine;
import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.u.Unburden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BontusMonument.class, DuneBeetle.class, BitterbladeWarrior.class, LuxaRiverShrine.class, Unburden.class})
class BontusMonumentTest extends BaseCardTest {

    @Test
    @DisplayName("Black creature spells cost {1} less")
    void blackCreatureSpellsCostOneLess() {
        harness.addToBattlefield(player1, new BontusMonument());
        // Dune Beetle costs {1}{B} - with the {1} reduction it should cost just {B}
        harness.setHand(player1, List.of(new DuneBeetle()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Dune Beetle"));
    }

    @Test
    @DisplayName("Cannot cast a black creature without enough mana even with the reduction")
    void cannotCastBlackCreatureWithoutEnoughMana() {
        harness.addToBattlefield(player1, new BontusMonument());
        // Dune Beetle costs {1}{B} - with {1} reduction needs {B}; no mana is not enough
        harness.setHand(player1, List.of(new DuneBeetle()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-black creature spells are not reduced")
    void nonBlackCreaturesNotReduced() {
        harness.addToBattlefield(player1, new BontusMonument());
        // Bitterblade Warrior costs {1}{G} - not black, so only {G} is not enough
        harness.setHand(player1, List.of(new BitterbladeWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting a creature drains each opponent 1 and gains the controller 1 life")
    void castingCreatureDrainsAndGainsLife() {
        harness.addToBattlefield(player1, new BontusMonument());
        harness.setHand(player1, List.of(new BitterbladeWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player1, 0);
        // Trigger is on top of the stack (LIFO) - resolve it
        harness.passBothPriorities();

        harness.assertLife(player2, p2Before - 1);
        harness.assertLife(player1, p1Before + 1);
    }

    @Test
    @DisplayName("Casting a creature puts the drain trigger on the stack")
    void castingCreatureTriggers() {
        harness.addToBattlefield(player1, new BontusMonument());
        harness.setHand(player1, List.of(new BitterbladeWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Bontu's Monument"));
    }

    @Test
    @DisplayName("Casting a non-creature spell does not trigger the drain")
    void nonCreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new BontusMonument());
        // Luxa River Shrine is a {3} artifact - not a creature spell
        harness.setHand(player1, List.of(new LuxaRiverShrine()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);

        // Only the artifact spell on the stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    void opponentsBlackCreatureGetsNeitherReductionNorTrigger() {
        harness.addToBattlefield(player1, new BontusMonument());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DuneBeetle()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void blackNoncreatureGetsNeitherReductionNorTrigger() {
        harness.addToBattlefield(player1, new BontusMonument());
        harness.setHand(player1, List.of(new Unburden()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
    }

    @Test
    void drainResolvesAfterMonumentLeavesBattlefield() {
        harness.addToBattlefield(player1, new BontusMonument());
        harness.setHand(player1, List.of(new DuneBeetle()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Dune Beetle");
    }
}
