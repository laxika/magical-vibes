package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(HighlandWeald.class)
class HighlandWealdTest extends BaseCardTest {

    @Test
    @DisplayName("Highland Weald enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new HighlandWeald()));
        harness.playLand(player1, 0);

        Permanent weald = gd.playerBattlefields.get(player1.getId()).getFirst();

        assertThat(weald.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Highland Weald adds one red mana when red is chosen")
    void addsRedMana() {
        addsChosenMana(ManaColor.RED, ManaColor.GREEN);
    }

    @Test
    @DisplayName("Highland Weald adds one green mana when green is chosen")
    void addsGreenMana() {
        addsChosenMana(ManaColor.GREEN, ManaColor.RED);
    }

    @Test
    @DisplayName("Highland Weald cannot activate its mana ability while tapped after entry")
    void cannotActivateWhileTapped() {
        harness.setHand(player1, List.of(new HighlandWeald()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Highland Weald enters tapped when put directly onto the battlefield")
    void entersTappedWithoutBeingPlayed() {
        Permanent weald = harness.enterBattlefieldAndReturn(player1, new HighlandWeald());

        assertThat(weald.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Highland Weald produces mana after untapping without using the stack")
    void producesManaAfterUntapping() {
        harness.setHand(player1, List.of(new HighlandWeald()));
        harness.playLand(player1, 0);
        harness.performUntapStep(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Red mana produced by Highland Weald is tagged for snow costs")
    void redManaIsSnowMana() {
        addsChosenMana(ManaColor.RED, ManaColor.GREEN);

        assertThat(gd.playerManaPools.get(player1.getId()).getSnowMana(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Green mana produced by Highland Weald is tagged for snow costs")
    void greenManaIsSnowMana() {
        addsChosenMana(ManaColor.GREEN, ManaColor.RED);

        assertThat(gd.playerManaPools.get(player1.getId()).getSnowMana(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isEqualTo(1);
    }

    private void addsChosenMana(ManaColor chosenColor, ManaColor otherColor) {
        Permanent weald = harness.addToBattlefieldAndReturn(player1, new HighlandWeald());
        weald.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("RED", "GREEN");
        harness.handleListChoice(player1, chosenColor.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(chosenColor)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(otherColor)).isZero();
        assertThat(weald.isTapped()).isTrue();
    }
}
