package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GraftedSkullcap;
import com.github.laxika.magicalvibes.cards.r.Rescind;
import com.github.laxika.magicalvibes.cards.w.WizardMentor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZephidsEmbrace.class, CoralMerfolk.class, GraftedSkullcap.class, Rescind.class,
        WizardMentor.class, Zephid.class})
class ZephidsEmbraceTest extends BaseCardTest {

    @Test
    void resolvingAttachesToTargetCreature() {
        Permanent creature = addCreatureReady(player1, new CoralMerfolk());
        harness.setHand(player1, List.of(new ZephidsEmbrace()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isAttached()
                        && permanent.getAttachedTo().equals(creature.getId()));
    }

    @Test
    void enchantedCreatureGetsBoostFlyingAndShroud() {
        Permanent creature = addCreatureReady(player1, new CoralMerfolk());
        attachAura(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
    }

    @Test
    void effectsEndWhenAuraLeavesBattlefield() {
        Permanent creature = addCreatureReady(player1, new CoralMerfolk());
        Permanent aura = attachAura(creature);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
    }

    @Test
    void shroudPreventsTargetingEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new CoralMerfolk());
        attachAura(creature);
        harness.setHand(player1, List.of(new Rescind()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    void cannotEnchantNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GraftedSkullcap());
        harness.setHand(player1, List.of(new ZephidsEmbrace()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void effectsApplyToOpponentCreature() {
        Permanent creature = addCreatureReady(player2, new CoralMerfolk());
        harness.setHand(player1, List.of(new ZephidsEmbrace()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
    }

    @Test
    void doesNotAffectOtherCreatures() {
        Permanent enchantedCreature = addCreatureReady(player1, new CoralMerfolk());
        Permanent otherCreature = addCreatureReady(player1, new CoralMerfolk());
        attachAura(enchantedCreature);

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.SHROUD)).isFalse();
    }

    @Test
    void cannotTargetShroudedCreatureWithAura() {
        Permanent creature = addCreatureReady(player1, new Zephid());
        harness.setHand(player1, List.of(new ZephidsEmbrace()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    void shroudPreventsTargetingEnchantedCreatureWithAbility() {
        addCreatureReady(player1, new WizardMentor());
        Permanent creature = addCreatureReady(player1, new CoralMerfolk());
        attachAura(creature);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    private Permanent attachAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ZephidsEmbrace());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
