package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MantleOfTheWolf.class, FountainOfYouth.class, GrizzlyBears.class})
class MantleOfTheWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Mantle of the Wolf attaches to a creature and gives it +4/+4")
    void attachesAndBoostsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MantleOfTheWolf()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Mantle of the Wolf");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    @DisplayName("Mantle of the Wolf's boost ends when the Aura is put into a graveyard")
    void boostEndsWhenAuraIsRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MantleOfTheWolf());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Mantle of the Wolf is put into a graveyard, it creates two Wolf tokens")
    void createsTwoWolvesWhenPutIntoGraveyard() {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MantleOfTheWolf());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        List<Permanent> wolves = findPermanents(player1, "Wolf");
        assertThat(wolves).hasSize(2);
        assertThat(wolves).allSatisfy(wolf -> {
            assertThat(wolf.getCard().isToken()).isTrue();
            assertThat(wolf.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(wolf.getCard().getSubtypes()).contains(CardSubtype.WOLF);
            assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Mantle of the Wolf cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new MantleOfTheWolf()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
