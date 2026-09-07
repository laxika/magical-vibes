package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoomBlade.class, ElephantGuide.class, GrizzlyBears.class})
class ElephantGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +3/+3")
    void enchantedCreatureGetsBoost() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castElephantGuide(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("When the enchanted creature dies, its controller creates a 3/3 green Elephant")
    void createsElephantWhenEnchantedCreatureDies() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castElephantGuide(bears);

        destroyCreature(bears);

        Permanent elephant = findPermanent(player1, "Elephant");
        assertThat(elephant.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(gqs.getEffectivePower(gd, elephant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elephant)).isEqualTo(3);
    }

    @Test
    @DisplayName("The death trigger does not trigger for another creature")
    void doesNotTriggerForAnotherCreature() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        castElephantGuide(enchanted);

        destroyCreature(other);

        assertThat(countPermanents(player1, "Elephant")).isZero();
    }

    private void castElephantGuide(Permanent target) {
        harness.setHand(player1, java.util.List.of(new ElephantGuide()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void destroyCreature(Permanent target) {
        harness.setHand(player2, java.util.List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
