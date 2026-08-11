package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
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
