package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.cards.m.MonasterySiege;
import com.github.laxika.magicalvibes.cards.o.OutpostSiege;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemurRunemark.class, GrizzlyBears.class, WindDrake.class, GoblinPiker.class,
        FountainOfYouth.class, MonasterySiege.class, OutpostSiege.class})
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

    @Test
    void castingAuraOnOpponentsCreatureAppliesBothBonuses() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new GoblinPiker());
        harness.setHand(player1, List.of(new TemurRunemark()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Temur Runemark").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void blueNoncreaturePermanentEnablesTrample() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attach(player1, creature);
        harness.addToBattlefield(player1, new MonasterySiege());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void redNoncreaturePermanentEnablesTrample() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attach(player1, creature);
        harness.addToBattlefield(player1, new OutpostSiege());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void enchantedBlueCreatureCanItselfSatisfyTheCondition() {
        Permanent creature = addCreatureReady(player1, new WindDrake());
        attach(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    void greenAndColorlessPermanentsAndCardsInHandDoNotEnableTrample() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attach(player1, creature);
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new WindDrake(), new GoblinPiker()));

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void trampleRemainsWhileAnotherQualifyingPermanentIsControlled() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attach(player1, creature);
        Permanent redPermanent = addCreatureReady(player1, new GoblinPiker());
        addCreatureReady(player1, new WindDrake());

        gd.playerBattlefields.get(player1.getId()).remove(redPermanent);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void removingAuraRemovesBothBonusesOnlyFromEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attach(player1, creature);
        addCreatureReady(player1, new GoblinPiker());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent attach(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new TemurRunemark());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
