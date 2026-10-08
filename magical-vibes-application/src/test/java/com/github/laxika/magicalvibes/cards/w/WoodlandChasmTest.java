package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WoodlandChasm.class})
class WoodlandChasmTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new WoodlandChasm()));

        harness.playLand(player1, 0);

        Permanent chasm = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(chasm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds black or green mana")
    void manaAbilityAddsBlackOrGreenMana() {
        Permanent chasm = harness.addToBattlefieldAndReturn(player1, new WoodlandChasm());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLACK.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(chasm.isTapped()).isTrue();

        chasm.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enters tapped when put onto the battlefield without being played")
    void entersTappedWithoutBeingPlayed() {
        Permanent chasm = harness.enterBattlefieldAndReturn(player1, new WoodlandChasm());

        assertThat(chasm.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot produce mana while tapped, but can after untapping")
    void producesManaAfterUntapping() {
        harness.setHand(player1, List.of(new WoodlandChasm()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.performUntapStep(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowMana(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
