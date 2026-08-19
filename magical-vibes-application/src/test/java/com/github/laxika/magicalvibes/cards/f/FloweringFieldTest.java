package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FloweringFieldTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land can tap to prevent the next damage to a player")
    void enchantedLandPreventsNextDamageToPlayer() {
        Permanent forest = setUpEnchantedForest();
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new HillGiant());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Granted ability disappears when Flowering Field leaves the battlefield")
    void grantedAbilityDisappearsWhenAuraLeaves() {
        Permanent forest = setUpEnchantedForest();
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof FloweringField);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(forest), 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Flowering Field cannot enchant a creature")
    void cannotEnchantCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.setHand(player1, List.of(new FloweringField()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    private Permanent setUpEnchantedForest() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = findPermanent(player1, "Forest");
        Permanent aura = new Permanent(new FloweringField());
        aura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return forest;
    }
}
