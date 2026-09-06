package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HotSprings.class, Forest.class, Incinerate.class, BalduvianBears.class})
class HotSpringsTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land's granted ability taps the land and prevents damage to a target player")
    void grantedAbilityPreventsNextDamageToTarget() {
        Permanent forest = attach(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.globalDamagePreventionShield).isZero();
    }

    @Test
    @DisplayName("The granted prevention ability requires an any-target choice")
    void grantedAbilityRequiresTarget() {
        attach(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The prevention shield protects only the chosen target")
    void preventsOnlyDamageToChosenTarget() {
        attach(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        castIncinerateAt(player2);
        castIncinerateAt(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
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

    @Test
    @DisplayName("Cannot enchant a nonland permanent you control")
    void cannotEnchantNonlandPermanent() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new HotSprings()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bear.getId()))
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

    private void castIncinerateAt(Player target) {
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
