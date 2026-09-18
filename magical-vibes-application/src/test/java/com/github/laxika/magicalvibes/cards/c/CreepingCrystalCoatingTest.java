package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CreepingCrystalCoating.class, FountainOfYouth.class, GrizzlyBears.class})
class CreepingCrystalCoatingTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +0/+3")
    void enchantedCreatureGetsToughnessBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new CreepingCrystalCoating());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Attacking with the enchanted creature creates a Food token")
    void attackCreatesFoodToken() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new CreepingCrystalCoating());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    @DisplayName("The created Food token can be sacrificed for life")
    void foodTokenCanBeSacrificedForLife() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new CreepingCrystalCoating());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Creeping Crystal Coating cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new CreepingCrystalCoating()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
