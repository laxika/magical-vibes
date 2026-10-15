package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoldmireBridge.class, StoneRain.class})
class GoldmireBridgeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new GoldmireBridge()));

        harness.playLand(player1, 0);

        Permanent bridge = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bridge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when put onto the battlefield without being played")
    void entersTappedWithoutBeingPlayed() {
        Permanent bridge = harness.enterBattlefieldAndReturn(player1, new GoldmireBridge());

        assertThat(bridge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds white or black mana")
    void manaAbilityAddsWhiteOrBlackMana() {
        Permanent bridge = harness.addToBattlefieldAndReturn(player1, new GoldmireBridge());
        bridge.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.WHITE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(bridge.isTapped()).isTrue();

        bridge.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLACK.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Indestructible keeps it on the battlefield through a destroy effect")
    void survivesDestruction() {
        harness.addToBattlefield(player2, new GoldmireBridge());
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Goldmire Bridge");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Goldmire Bridge");
    }

    @Test
    @DisplayName("Cannot activate the mana ability while tapped after entering")
    void cannotActivateWhileTapped() {
        harness.setHand(player1, List.of(new GoldmireBridge()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("A newly controlled noncreature land produces mana immediately when untapped")
    void newlyControlledLandProducesManaWithoutUsingStack() {
        harness.setHand(player1, List.of(new GoldmireBridge()));
        harness.playLand(player1, 0);
        Permanent bridge = findPermanent(player1, "Goldmire Bridge");
        bridge.untap();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLACK.name());

        assertThat(bridge.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }
}
