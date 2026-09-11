package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiantStrength.class, GrizzlyBears.class, TrainedArmodon.class, LotusPetal.class})
class GiantStrengthTest extends BaseCardTest {
    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent bearsPerm = addCreatureReady(player1, new TrainedArmodon());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new GiantStrength());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(5);
    }

    @Test
    @DisplayName("Resolving Giant Strength attaches it and boosts the enchanted creature")
    void resolvingAttachesAndBoostsEnchantedCreature() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new TrainedArmodon());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new TrainedArmodon());
        harness.setHand(player1, List.of(new GiantStrength()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof GiantStrength
                        && enchanted.getId().equals(permanent.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
    }
    @Test
    @DisplayName("Creature loses boost when Giant Strength is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new TrainedArmodon());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new GiantStrength());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Giant Strength stops boosting its creature when it becomes unattached")
    void effectsStopWhenUnattached() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new GiantStrength());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);

        auraPerm.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);

        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Giant Strength");
    }

    @Test
    @DisplayName("Giant Strength is put into its owner's graveyard when its creature leaves")
    void isPutIntoGraveyardWhenEnchantedCreatureLeaves() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new GiantStrength());
        auraPerm.setAttachedTo(bearsPerm.getId());

        gd.playerBattlefields.get(player1.getId()).remove(bearsPerm);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Giant Strength");
        harness.assertInGraveyard(player1, "Giant Strength");
    }

    @Test
    @DisplayName("Giant Strength does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = addCreatureReady(player1, new TrainedArmodon());
        Permanent otherBears = addCreatureReady(player1, new TrainedArmodon());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new GiantStrength());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherBears)).isEqualTo(3);
    }
    @Test
    @DisplayName("Can target a creature with Giant Strength")
    void canTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new TrainedArmodon());
        harness.setHand(player1, List.of(new GiantStrength()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Giant Strength can enchant a creature an opponent controls")
    void canEnchantOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new TrainedArmodon());
        harness.setHand(player1, List.of(new GiantStrength()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Giant Strength fizzles if the target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new TrainedArmodon());
        harness.setHand(player1, List.of(new GiantStrength()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Giant Strength");
        harness.assertNotOnBattlefield(player1, "Giant Strength");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Giant Strength")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LotusPetal());
        harness.setHand(player1, List.of(new GiantStrength()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
