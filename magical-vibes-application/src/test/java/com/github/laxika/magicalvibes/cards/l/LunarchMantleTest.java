package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LunarchMantleTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Lunarch Mantle attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LunarchMantle()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        gs.playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Lunarch Mantle")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent bears = attachMantleToBears();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Granted ability sacrifices a permanent and grants flying until end of turn")
    void grantedAbilityGrantsFlying() {
        Permanent bears = attachMantleToBears();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        if (gd.interaction.activeInteraction() != null) {
            harness.handlePermanentChosen(player1, forest.getId());
        }
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Flying from the granted ability wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent bears = attachMantleToBears();
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        if (gd.interaction.activeInteraction() != null) {
            Permanent forest = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Forest"))
                    .findFirst()
                    .orElseThrow();
            harness.handlePermanentChosen(player1, forest.getId());
        }
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Boost and granted ability end when Lunarch Mantle is removed")
    void effectsStopWhenRemoved() {
        Permanent bears = attachMantleToBears();
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Lunarch Mantle"))
                .findFirst()
                .orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new LunarchMantle()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst()
                .orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attachMantleToBears() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent aura = new Permanent(new LunarchMantle());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return bears;
    }
}
