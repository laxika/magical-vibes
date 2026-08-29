package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemurRunemarkTest extends BaseCardTest {

    @Test
    void enchantedCreatureGetsPlusTwoPlusTwo() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attach(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void enchantedCreatureHasTrampleWhileAuraControllerControlsBluePermanent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attach(player1, creature);
        addCreatureReady(player1, new WindDrake());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void enchantedCreatureHasTrampleWhileAuraControllerControlsRedPermanent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attach(player1, creature);
        addCreatureReady(player1, new GoblinPiker());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void trampleRequiresAuraControllerToControlQualifyingPermanent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attach(player1, creature);
        addCreatureReady(player2, new WindDrake());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void trampleIsLostWhenQualifyingPermanentLeaves() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attach(player1, creature);
        Permanent redPermanent = addCreatureReady(player1, new GoblinPiker());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(redPermanent);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new TemurRunemark()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attach(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new TemurRunemark());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }
}
