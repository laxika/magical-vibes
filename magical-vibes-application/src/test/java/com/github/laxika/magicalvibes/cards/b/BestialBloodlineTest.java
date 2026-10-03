package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed({BestialBloodline.class, FountainOfYouth.class, GrizzlyBears.class})
class BestialBloodlineTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Bestial Bloodline attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BestialBloodline()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof BestialBloodline
                        && bears.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new BestialBloodline());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Bestial Bloodline stops boosting when it leaves the battlefield")
    void boostStopsWhenRemoved() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new BestialBloodline());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bestial Bloodline cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player1.getId()).add(artifact);
        harness.setHand(player1, List.of(new BestialBloodline()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Bestial Bloodline returns from the graveyard to its owner's hand")
    void graveyardAbilityReturnsToHand() {
        harness.setGraveyard(player1, List.of(new BestialBloodline()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Bestial Bloodline");
        harness.assertNotInGraveyard(player1, "Bestial Bloodline");
    }

    @Test
    @DisplayName("Bestial Bloodline's graveyard ability requires five mana including green")
    void cannotActivateGraveyardAbilityWithoutEnoughMana() {
        harness.setGraveyard(player1, List.of(new BestialBloodline()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Bestial Bloodline can enchant and boost an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BestialBloodline()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Bestial Bloodline").getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("The graveyard ability returns only the copy that was activated")
    void returnsOnlyActivatedCopy() {
        BestialBloodline activated = new BestialBloodline();
        BestialBloodline other = new BestialBloodline();
        harness.setGraveyard(player1, List.of(other, activated));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(activated).doesNotContain(other);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }

    @Test
    @DisplayName("The graveyard ability cannot be paid with only colorless mana")
    void graveyardAbilityRequiresGreenMana() {
        harness.setGraveyard(player1, List.of(new BestialBloodline()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Bestial Bloodline");
    }
}
