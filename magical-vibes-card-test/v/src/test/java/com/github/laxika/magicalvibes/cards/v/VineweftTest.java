package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.r.RummagingGoblin;
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

@CardUsed({Vineweft.class, RuneclawBear.class, DarksteelCitadel.class, RummagingGoblin.class})
class VineweftTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Vineweft attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        harness.setHand(player1, List.of(new Vineweft()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Vineweft")
                        && bears.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+1")
    void enchantedCreatureGetsBoost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Vineweft());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creature returns to base stats when Vineweft leaves the battlefield")
    void boostStopsWhenRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Vineweft());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new RuneclawBear());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DarksteelCitadel());
        harness.setHand(player1, List.of(new Vineweft()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Graveyard ability returns Vineweft from the graveyard to hand")
    void graveyardAbilityReturnsToHand() {
        harness.setGraveyard(player1, List.of(new Vineweft()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Vineweft");
        harness.assertNotInGraveyard(player1, "Vineweft");
    }

    @Test
    @DisplayName("Cannot activate the graveyard ability without enough mana")
    void cannotActivateGraveyardAbilityWithoutMana() {
        harness.setGraveyard(player1, List.of(new Vineweft()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canEnchantAndBoostOpponentsCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new Vineweft()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof Vineweft && bear.getId().equals(p.getAttachedTo()));
    }

    @Test
    void graveyardAbilityReturnsOnlyItsOwnCopy() {
        Vineweft source = new Vineweft();
        Vineweft other = new Vineweft();
        RuneclawBear bear = new RuneclawBear();
        Vineweft opponentsCopy = new Vineweft();
        harness.setGraveyard(player1, List.of(source, other, bear));
        harness.setGraveyard(player2, List.of(opponentsCopy));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(source).doesNotContain(other, bear);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other, bear);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCopy);
    }

    @Test
    void graveyardAbilityRequiresGreenMana() {
        harness.setGraveyard(player1, List.of(new Vineweft()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Vineweft");
        harness.assertNotInHand(player1, "Vineweft");
    }

    @Test
    void olderActivationCannotReturnVineweftAfterItLeavesAndReentersGraveyard() {
        addCreatureReady(player1, new RummagingGoblin());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new RuneclawBear(), new RuneclawBear()));
        harness.setGraveyard(player1, List.of(new Vineweft()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Vineweft");
        assertThat(gd.stack).hasSize(1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vineweft");
        harness.assertNotInHand(player1, "Vineweft");
        assertThat(gd.stack).isEmpty();
    }
}
