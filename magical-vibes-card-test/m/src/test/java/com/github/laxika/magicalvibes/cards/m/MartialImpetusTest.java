package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MartialImpetus.class, GrizzlyBears.class, Mountain.class})
class MartialImpetusTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 and is goaded")
    void enchantedCreatureGetsBoostAndIsGoaded() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castMartialImpetus(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(als.getMustAttackRequirementCount(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("When enchanted creature attacks, other creatures attacking an opponent get +1/+1")
    void boostsOtherAttackers() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        castMartialImpetus(enchanted);

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
    }

    @Test
    @DisplayName("Martial Impetus cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new MartialImpetus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Creatures that are not attacking do not receive the triggered boost")
    void doesNotBoostNonAttackers() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent idle = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());
        castMartialImpetus(enchanted);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, idle)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, idle)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking the Aura's controller does not qualify for its triggered boost")
    void doesNotBoostCreaturesAttackingAuraController() {
        Permanent enchanted = addCreatureReady(player2, new GrizzlyBears());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());
        castMartialImpetus(enchanted);

        declareAttackers(player2, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("An able goaded creature cannot be omitted from the attack declaration")
    void goadedCreatureMustAttackIfAble() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        castMartialImpetus(enchanted);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A pending attack trigger still excludes the enchanted creature after its Aura leaves")
    void pendingTriggerUsesAuraLastKnownAttachment() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        castMartialImpetus(enchanted);
        Permanent aura = findPermanent(player1, "Martial Impetus");

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0, 1)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
        assertThat(gqs.isGoaded(gd, enchanted)).isFalse();
    }
    private void castMartialImpetus(Permanent creature) {
        harness.setHand(player1, List.of(new MartialImpetus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
