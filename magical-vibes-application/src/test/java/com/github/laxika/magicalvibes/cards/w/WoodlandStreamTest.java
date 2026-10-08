package com.github.laxika.magicalvibes.cards.w;

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

@CardUsed({WoodlandStream.class})
class WoodlandStreamTest extends BaseCardTest {

    @Test
    @DisplayName("Woodland Stream enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new WoodlandStream()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent stream = findPermanent(player1, "Woodland Stream");
        assertThat(stream.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        addStreamReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        addStreamReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when put onto the battlefield without being played")
    void entersTappedWithoutBeingPlayed() {
        Permanent stream = harness.enterBattlefieldAndReturn(player1, new WoodlandStream());

        assertThat(stream.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Neither mana ability can be activated while tapped")
    void cannotProduceManaWhileTapped() {
        harness.enterBattlefieldAndReturn(player1, new WoodlandStream());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("An untapped land can immediately produce only the chosen color without using the stack")
    void producesOnlyChosenColorImmediately() {
        Permanent stream = harness.addToBattlefieldAndReturn(player1, new WoodlandStream());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(stream.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Can produce blue mana after untapping without using the stack")
    void producesManaAfterUntapping() {
        Permanent stream = harness.enterBattlefieldAndReturn(player1, new WoodlandStream());
        harness.performUntapStep(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(stream.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addStreamReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new WoodlandStream());
        perm.setSummoningSick(false);
        return perm;
    }
}
