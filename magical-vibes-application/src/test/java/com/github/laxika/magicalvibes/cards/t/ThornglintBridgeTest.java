package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({ThornglintBridge.class, StoneRain.class})
class ThornglintBridgeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new ThornglintBridge()));

        harness.playLand(player1, 0);

        Permanent bridge = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bridge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds green or white mana")
    void manaAbilityAddsGreenOrWhiteMana() {
        Permanent bridge = harness.addToBattlefieldAndReturn(player1, new ThornglintBridge());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(bridge.isTapped()).isTrue();

        bridge.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.WHITE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Indestructible keeps it on the battlefield through a destroy effect")
    void survivesDestruction() {
        harness.addToBattlefield(player2, new ThornglintBridge());
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Thornglint Bridge");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Thornglint Bridge");
    }

    @Test
    @DisplayName("Enters tapped when put onto the battlefield without being played")
    void entersTappedWithoutBeingPlayed() {
        Permanent bridge = harness.enterBattlefieldAndReturn(player1, new ThornglintBridge());

        assertThat(bridge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped bridge cannot pay its mana ability's tap cost")
    void tappedBridgeCannotProduceMana() {
        harness.setHand(player1, List.of(new ThornglintBridge()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("A newly controlled noncreature bridge produces mana immediately after untapping")
    void newlyControlledBridgeProducesManaWithoutUsingStack() {
        Permanent bridge = harness.enterBattlefieldAndReturn(player1, new ThornglintBridge());
        bridge.untap();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.WHITE.name());

        assertThat(bridge.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
