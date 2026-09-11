package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IvoryCup;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DivineTransformation.class, GrizzlyBears.class, IvoryCup.class})
class DivineTransformationTest extends BaseCardTest {

    // ===== +3/+3 boost =====

    @Test
    @DisplayName("Enchanted creature gets +3/+3")
    void enchantedCreatureGetsBoost() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new DivineTransformation());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(5);
    }

    // ===== Effects stop when removed =====

    @Test
    @DisplayName("Creature loses boost when Divine Transformation is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new DivineTransformation());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolving Divine Transformation attaches to and boosts an opponent's creature")
    void resolvingAttachesAndBoostsOpponentCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        DivineTransformation aura = new DivineTransformation();
        harness.setHand(player1, List.of(aura));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(aura.getId())
                        && bears.getId().equals(permanent.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Divine Transformation goes to its owner's graveyard when its target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        DivineTransformation aura = new DivineTransformation();
        harness.setHand(player1, List.of(aura));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(aura.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(aura.getId()));
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Can target a creature")
    void canTargetCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DivineTransformation()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IvoryCup());
        harness.setHand(player1, List.of(new DivineTransformation()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Does not affect other creatures =====

    @Test
    @DisplayName("Divine Transformation does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent otherBears = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new DivineTransformation());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherBears)).isEqualTo(2);
    }
}
