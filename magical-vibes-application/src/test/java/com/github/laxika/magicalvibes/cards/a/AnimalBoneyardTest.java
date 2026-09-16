package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
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

@CardUsed({AnimalBoneyard.class, Forest.class, GiantSpider.class})
class AnimalBoneyardTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land sacrifices a creature and gains life equal to its toughness")
    void sacrificesCreatureAndGainsLifeEqualToToughness() {
        Permanent forest = attachToForest();
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        int lifeBefore = gd.getLife(player1.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, null);
        if (gd.interaction.activeInteraction() != null) {
            harness.handlePermanentChosen(player1, spider.getId());
        }
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
        assertThat(forest.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Giant Spider");
    }

    @Test
    @DisplayName("Animal Boneyard can enchant a land")
    void canEnchantLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new AnimalBoneyard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Animal Boneyard");
        assertThat(aura.getAttachedTo()).isEqualTo(forest.getId());
    }

    @Test
    @DisplayName("Animal Boneyard cannot enchant a nonland permanent")
    void cannotEnchantNonland() {
        harness.addToBattlefield(player1, new Forest());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        harness.setHand(player1, List.of(new AnimalBoneyard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, spider.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("The controller of the enchanted land controls the granted ability")
    void enchantedLandControllerGetsGrantedAbility() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AnimalBoneyard());
        aura.setAttachedTo(forest.getId());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2, 0, 0, null, null);
        if (gd.interaction.activeInteraction() != null) {
            harness.handlePermanentChosen(player2, spider.getId());
        }
        harness.passBothPriorities();

        harness.assertLife(player1, player1LifeBefore);
        harness.assertLife(player2, player2LifeBefore + 4);
        assertThat(forest.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("The granted ability cannot be activated without a creature to sacrifice")
    void cannotActivateWithoutCreature() {
        Permanent forest = attachToForest();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An activated ability still resolves after Animal Boneyard leaves")
    void activatedAbilityResolvesAfterAuraLeaves() {
        Permanent forest = attachToForest();
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        int lifeBefore = gd.getLife(player1.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, null);
        if (gd.interaction.activeInteraction() != null) {
            harness.handlePermanentChosen(player1, spider.getId());
        }

        Permanent aura = findPermanent(player1, "Animal Boneyard");
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 4);
        assertThat(forest.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Giant Spider");
    }

    @Test
    @DisplayName("Granted ability disappears when Animal Boneyard leaves the battlefield")
    void grantedAbilityDisappearsWhenAuraLeaves() {
        Permanent forest = attachToForest();
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof AnimalBoneyard)
                .findFirst()
                .orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    private Permanent attachToForest() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AnimalBoneyard());
        aura.setAttachedTo(forest.getId());
        return forest;
    }
}
