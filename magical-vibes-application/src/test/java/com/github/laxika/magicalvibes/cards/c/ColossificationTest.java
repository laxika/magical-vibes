package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({Colossification.class, FountainOfYouth.class, GrizzlyBears.class})
class ColossificationTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Colossification targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent creature = addCreature(player1);
        harness.setHand(player1, List.of(new Colossification()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Colossification attaches, taps the enchanted creature, and gives it +20/+20")
    void resolvesAndAppliesEffects() {
        Permanent creature = addCreature(player1);
        harness.setHand(player1, List.of(new Colossification()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(creature.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(22);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(22);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Colossification
                        && permanent.isAttached()
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Colossification's boost ends when it leaves the battlefield")
    void boostStopsWhenRemoved() {
        Permanent creature = addCreature(player1);
        Permanent aura = new Permanent(new Colossification());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(22);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(22);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Colossification fizzles if its target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = addCreature(player1);
        harness.setHand(player1, List.of(new Colossification()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Colossification");
        harness.assertNotOnBattlefield(player1, "Colossification");
    }

    @Test
    @DisplayName("Colossification cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Colossification()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }
}
