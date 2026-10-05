package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SpiritedCompanion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JukaiNaturalist.class, GhostlyPrison.class, GrizzlyBears.class,
        SpiritedCompanion.class, JukaiPreserver.class})
class JukaiNaturalistTest extends BaseCardTest {

    @Test
    void enchantmentSpellsCostOneLess() {
        harness.addToBattlefield(player1, new JukaiNaturalist());
        harness.setHand(player1, List.of(new GhostlyPrison()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Ghostly Prison"));
    }

    @Test
    void nonEnchantmentSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new JukaiNaturalist());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentEnchantmentSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new JukaiNaturalist());
        harness.setHand(player2, List.of(new GhostlyPrison()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enchantmentCreatureSpellsCostOneLess() {
        harness.addToBattlefield(player1, new JukaiNaturalist());
        harness.setHand(player1, List.of(new SpiritedCompanion()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Spirited Companion"));
    }

    @Test
    void multipleNaturalistsStackTheirReductions() {
        harness.addToBattlefield(player1, new JukaiNaturalist());
        harness.addToBattlefield(player1, new JukaiNaturalist());
        harness.setHand(player1, List.of(new JukaiPreserver()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Jukai Preserver"));
    }

    @Test
    void excessReductionCannotPayColoredMana() {
        harness.addToBattlefield(player1, new JukaiNaturalist());
        harness.addToBattlefield(player1, new JukaiNaturalist());
        harness.setHand(player1, List.of(new JukaiNaturalist()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void naturalistInHandDoesNotReduceOtherSpells() {
        harness.setHand(player1, List.of(new SpiritedCompanion(), new JukaiNaturalist()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void channelAbilitiesAreNotReduced() {
        var naturalist = harness.addToBattlefieldAndReturn(player1, new JukaiNaturalist());
        harness.setHand(player1, List.of(new JukaiPreserver()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateHandAbilityWithMultiTargets(
                player1, 0, List.of(naturalist.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void combatDamageGainsLifeThroughLifelink() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new JukaiNaturalist()).setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }
}
