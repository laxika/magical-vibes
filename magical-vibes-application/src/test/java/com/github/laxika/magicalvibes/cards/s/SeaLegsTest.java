package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TravelersAmulet;
import com.github.laxika.magicalvibes.cards.c.Cacophodon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeaLegs.class, SailorOfMeans.class, Cacophodon.class, TravelersAmulet.class})
class SeaLegsTest extends BaseCardTest {

    @Test
    void pirateCreatureGetsToughnessBoost() {
        Permanent pirate = harness.addToBattlefieldAndReturn(player1, new SailorOfMeans());

        int basePower = gqs.getEffectivePower(gd, pirate);
        int baseToughness = gqs.getEffectiveToughness(gd, pirate);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SeaLegs());
        aura.setAttachedTo(pirate.getId());

        assertThat(gqs.getEffectivePower(gd, pirate)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, pirate)).isEqualTo(baseToughness + 2);
    }

    @Test
    void nonPirateCreatureGetsPowerDebuff() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Cacophodon());

        int basePower = gqs.getEffectivePower(gd, creature);
        int baseToughness = gqs.getEffectiveToughness(gd, creature);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SeaLegs());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(basePower - 2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(baseToughness);
    }

    @Test
    void resolvingSeaLegsAttachesToTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SailorOfMeans());

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
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new TravelersAmulet());
        harness.setHand(player1, List.of(new SeaLegs()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void canBeCastDuringOpponentsEndStep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SailorOfMeans());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new SeaLegs()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof SeaLegs
                        && creature.getId().equals(permanent.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    void multipleAurasStackAndDoNotAffectOtherCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Cacophodon());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new SailorOfMeans());
        harness.setHand(player1, List.of(new SeaLegs(), new SeaLegs()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);
    }
}
