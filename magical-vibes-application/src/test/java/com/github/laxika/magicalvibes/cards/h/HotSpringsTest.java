package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HotSpringsTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land's granted ability sets the global damage prevention shield and taps the land")
    void grantedAbilityPreventsNextDamage() {
        Permanent forest = attach(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.globalDamagePreventionShield).isEqualTo(1);
    }

    @Test
    @DisplayName("The land itself has no such ability without the aura")
    void unenchantedLandHasNoAbility() {
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot enchant a land you do not control")
    void cannotEnchantOpponentsLand() {
        harness.addToBattlefield(player2, new Forest());
        Permanent opponentForest = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(new HotSprings()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentForest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent attach(Player player) {
        harness.addToBattlefield(player, new Forest());
        Permanent forest = gd.playerBattlefields.get(player.getId()).getFirst();
        Permanent aura = new Permanent(new HotSprings());
        aura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player.getId()).add(aura);
        return forest;
    }
}
