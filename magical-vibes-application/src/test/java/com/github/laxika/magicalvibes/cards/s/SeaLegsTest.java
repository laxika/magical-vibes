package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SailorOfMeans;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeaLegsTest extends BaseCardTest {

    @Test
    void pirateCreatureGetsToughnessBoost() {
        Permanent pirate = new Permanent(new SailorOfMeans());
        gd.playerBattlefields.get(player1.getId()).add(pirate);

        int basePower = gqs.getEffectivePower(gd, pirate);
        int baseToughness = gqs.getEffectiveToughness(gd, pirate);
        Permanent aura = new Permanent(new SeaLegs());
        aura.setAttachedTo(pirate.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, pirate)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, pirate)).isEqualTo(baseToughness + 2);
    }

    @Test
    void nonPirateCreatureGetsPowerDebuff() {
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(creature);

        int basePower = gqs.getEffectivePower(gd, creature);
        int baseToughness = gqs.getEffectiveToughness(gd, creature);
        Permanent aura = new Permanent(new SeaLegs());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(basePower - 2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(baseToughness);
    }

    @Test
    void resolvingSeaLegsAttachesToTargetCreature() {
        Permanent creature = new Permanent(new SailorOfMeans());
        gd.playerBattlefields.get(player2.getId()).add(creature);

        harness.setHand(player1, List.of(new SeaLegs()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof SeaLegs
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new SeaLegs()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
