package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwampsnareTrap.class, AirElemental.class, FountainOfYouth.class, GrizzlyBears.class})
class SwampsnareTrapTest extends BaseCardTest {

    @Test
    void enchantedCreatureGetsMinusFiveMinusThree() {
        Permanent creature = new Permanent(new AirElemental());
        gd.playerBattlefields.get(player1.getId()).add(creature);

        Permanent aura = new Permanent(new SwampsnareTrap());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    void costsOneLessWhenTargetingCreatureWithFlying() {
        Permanent creature = new Permanent(new AirElemental());
        gd.playerBattlefields.get(player2.getId()).add(creature);

        harness.setHand(player1, List.of(new SwampsnareTrap()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotReduceCostWhenTargetingCreatureWithoutFlying() {
        Permanent flyingCreature = new Permanent(new AirElemental());
        gd.playerBattlefields.get(player2.getId()).add(flyingCreature);
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(creature);

        harness.setHand(player1, List.of(new SwampsnareTrap()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new SwampsnareTrap()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        Permanent artifact = findPermanent(player2, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
