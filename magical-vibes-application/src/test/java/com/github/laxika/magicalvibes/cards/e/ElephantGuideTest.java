package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BenevolentBodyguard;
import com.github.laxika.magicalvibes.cards.c.Chastise;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenevolentBodyguard.class, Chastise.class, ElephantGuide.class})
class ElephantGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +3/+3")
    void enchantedCreatureGetsBoost() {
        Permanent bodyguard = addCreatureReady(player1, new BenevolentBodyguard());
        castElephantGuide(bodyguard);

        assertThat(gqs.getEffectivePower(gd, bodyguard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bodyguard)).isEqualTo(4);
    }

    @Test
    @DisplayName("When the enchanted creature dies, its controller creates a 3/3 green Elephant")
    void createsElephantWhenEnchantedCreatureDies() {
        Permanent bodyguard = addCreatureReady(player1, new BenevolentBodyguard());
        castElephantGuide(bodyguard);

        destroyAttackingCreature(player2, bodyguard);

        Permanent elephant = findPermanent(player1, "Elephant");
        assertThat(elephant.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(elephant.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(elephant.getCard().getSubtypes()).contains(CardSubtype.ELEPHANT);
        assertThat(elephant.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, elephant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elephant)).isEqualTo(3);
    }

    @Test
    @DisplayName("The death trigger does not trigger for another creature")
    void doesNotTriggerForAnotherCreature() {
        Permanent enchanted = addCreatureReady(player1, new BenevolentBodyguard());
        Permanent other = addCreatureReady(player1, new BenevolentBodyguard());
        castElephantGuide(enchanted);

        destroyAttackingCreature(player2, other);

        assertThat(countPermanents(player1, "Elephant")).isZero();
    }

    @Test
    @DisplayName("The Elephant is created under the Aura controller's control when an opponent's creature dies")
    void createsElephantUnderAuraControllersControl() {
        Permanent bodyguard = addCreatureReady(player2, new BenevolentBodyguard());
        castElephantGuide(bodyguard);

        destroyAttackingCreature(player1, bodyguard);

        assertThat(countPermanents(player1, "Elephant")).isEqualTo(1);
        assertThat(countPermanents(player2, "Elephant")).isZero();
    }

    private void castElephantGuide(Permanent target) {
        harness.setHand(player1, List.of(new ElephantGuide()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void destroyAttackingCreature(Player caster, Permanent target) {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        target.setAttacking(true);
        harness.setHand(caster, List.of(new Chastise()));
        harness.addMana(caster, ManaColor.WHITE, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 3);
        harness.castInstant(caster, 0, target.getId());
        resolveAllTriggers();
    }
}
