package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StealEnchantmentTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Steal Enchantment gains control of the enchanted enchantment")
    void resolvingStealsEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        Permanent anthem = findPermanent(player2, "Glorious Anthem");

        harness.setHand(player1, List.of(new StealEnchantment()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, anthem.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(anthem.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(anthem.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Steal Enchantment")
                        && p.isAttached()
                        && p.getAttachedTo().equals(anthem.getId()));
    }

    @Test
    @DisplayName("Stolen Glorious Anthem pumps the new controller's creatures instead")
    void stolenAnthemBoostsNewController() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        Permanent anthem = findPermanent(player2, "Glorious Anthem");
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent myBears = findPermanent(player1, "Grizzly Bears");
        Permanent theirBears = findPermanent(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new StealEnchantment()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, anthem.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, myBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, myBears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, theirBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, theirBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enchantment returns to its owner when Steal Enchantment leaves the battlefield")
    void enchantmentReturnsWhenAuraDestroyed() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        Permanent anthem = findPermanent(player2, "Glorious Anthem");

        harness.setHand(player1, List.of(new StealEnchantment()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, anthem.getId());
        harness.passBothPriorities();

        Permanent auraPerm = findPermanent(player1, "Steal Enchantment");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, auraPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(anthem.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(anthem.getId()));
    }

    @Test
    @DisplayName("Steal Enchantment fizzles if the target enchantment is gone")
    void fizzlesIfTargetGone() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        Permanent anthem = findPermanent(player2, "Glorious Anthem");

        harness.setHand(player1, List.of(new StealEnchantment()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, anthem.getId());
        gd.playerBattlefields.get(player2.getId()).remove(anthem);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Steal Enchantment");
    }

    @Test
    @DisplayName("Cannot target a nonenchantment permanent")
    void cannotTargetNonEnchantment() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new GloriousAnthem());
        Permanent artifact = findPermanent(player2, "Fountain of Youth");

        harness.setHand(player1, List.of(new StealEnchantment()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchantment");
    }
}
