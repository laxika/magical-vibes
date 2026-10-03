package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CausticTar.class, RuneclawBear.class, Forest.class, Naturalize.class})
class CausticTarTest extends BaseCardTest {

    @Test
    @DisplayName("Caustic Tar attaches to a land when it resolves")
    void attachesToLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new CausticTar()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof CausticTar
                        && forest.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Caustic Tar cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        Permanent nonland = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new CausticTar()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonland.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Enchanted land's ability makes target player lose 3 life")
    void enchantedLandMakesTargetPlayerLoseLife() {
        Permanent forest = attachedTar();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Enchanted land's ability can target its controller")
    void enchantedLandCanTargetItsController() {
        attachedTar();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("The controller of an enchanted opponent land can activate its ability")
    void enchantedOpponentLandControllerCanActivateAbility() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CausticTar());
        aura.setAttachedTo(forest.getId());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("A tapped enchanted land cannot pay the granted ability's tap cost")
    void tappedLandCannotActivateAbility() {
        Permanent forest = attachedTar();
        forest.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The granted ability requires a player target")
    void cannotTargetPermanentWithGrantedAbility() {
        Permanent forest = attachedTar();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(forest.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Destroying Caustic Tar removes the land's granted ability")
    void destroyingAuraRemovesGrantedAbility() {
        Permanent forest = attachedTar();
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Caustic Tar"));

        harness.assertInGraveyard(player1, "Caustic Tar");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An activated land ability resolves after Caustic Tar is destroyed")
    void activatedAbilitySurvivesAuraDestruction() {
        Permanent forest = attachedTar();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        assertThat(forest.isTapped()).isTrue();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Caustic Tar"));
        harness.assertInGraveyard(player1, "Caustic Tar");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent attachedTar() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CausticTar());
        aura.setAttachedTo(forest.getId());
        return forest;
    }
}
