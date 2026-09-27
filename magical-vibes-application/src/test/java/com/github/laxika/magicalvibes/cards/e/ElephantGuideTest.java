package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BenevolentBodyguard;
import com.github.laxika.magicalvibes.cards.c.CabalTrainee;
import com.github.laxika.magicalvibes.cards.c.Chastise;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.l.LightningSurge;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BenevolentBodyguard.class, CabalTrainee.class, Chastise.class, ElephantGuide.class, KrosanVerge.class, LightningSurge.class})
class ElephantGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +3/+3")
    void enchantedCreatureGetsBoost() {
        Permanent trainee = addCreatureReady(player1, new CabalTrainee());
        castElephantGuide(trainee);

        assertThat(gqs.getEffectivePower(gd, trainee)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, trainee)).isEqualTo(4);
    }

    @Test
    @DisplayName("When the enchanted creature dies, its controller creates a 3/3 green Elephant")
    void createsElephantWhenEnchantedCreatureDies() {
        Permanent trainee = addCreatureReady(player1, new CabalTrainee());
        castElephantGuide(trainee);

        destroyCreature(player1, trainee);

        Permanent elephant = findPermanent(player1, "Elephant");
        assertThat(elephant.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(elephant.getCard().getSubtypes()).containsExactly(CardSubtype.ELEPHANT);
        assertThat(gqs.getEffectivePower(gd, elephant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elephant)).isEqualTo(3);
    }

    @Test
    @DisplayName("The Guide's controller creates the Elephant when an opponent's enchanted creature dies")
    void guidesControllerGetsTokenWhenOpponentsCreatureDies() {
        Permanent trainee = addCreatureReady(player2, new CabalTrainee());
        castElephantGuide(trainee);

        destroyCreature(player2, trainee);

        assertThat(List.of(countPermanents(player1, "Elephant"), countPermanents(player2, "Elephant")))
                .as("Elephants controlled by player 1 then player 2")
                .containsExactly(1L, 0L);
    }

    @Test
    @DisplayName("Elephant Guide can target only a creature")
    void cannotEnchantNonCreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KrosanVerge());
        harness.setHand(player1, List.of(new ElephantGuide()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The death trigger does not trigger for another creature")
    void doesNotTriggerForAnotherCreature() {
        Permanent enchanted = addCreatureReady(player1, new CabalTrainee());
        Permanent other = addCreatureReady(player1, new CabalTrainee());
        castElephantGuide(enchanted);

        destroyCreature(player1, other);

        assertThat(countPermanents(player1, "Elephant")).isZero();
    }

    private void castElephantGuide(Permanent target) {
        harness.setHand(player1, List.of(new ElephantGuide()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void destroyCreature(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new LightningSurge()));
        harness.addMana(caster, ManaColor.RED, 2);
        harness.addMana(caster, ManaColor.COLORLESS, 3);
        harness.castSorcery(caster, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("When the enchanted creature dies, its controller creates a 3/3 green Elephant")
    void createsElephantWhenEnchantedCreatureDiesJudReview() {
        Permanent bodyguard = addCreatureReady(player1, new BenevolentBodyguard());
        castElephantGuide(bodyguard);

        destroyAttackingCreatureForJudReview(player2, bodyguard);

        Permanent elephant = findPermanent(player1, "Elephant");
        assertThat(elephant.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(elephant.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(elephant.getCard().getSubtypes()).contains(CardSubtype.ELEPHANT);
        assertThat(elephant.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, elephant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elephant)).isEqualTo(3);
    }

    @Test
    @DisplayName("The Elephant is created under the Aura controller's control when an opponent's creature dies")
    void createsElephantUnderAuraControllersControl() {
        Permanent bodyguard = addCreatureReady(player2, new BenevolentBodyguard());
        castElephantGuide(bodyguard);

        destroyAttackingCreatureForJudReview(player1, bodyguard);

        assertThat(countPermanents(player1, "Elephant")).isEqualTo(1);
        assertThat(countPermanents(player2, "Elephant")).isZero();
    }

    private void destroyAttackingCreatureForJudReview(Player caster, Permanent target) {
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
