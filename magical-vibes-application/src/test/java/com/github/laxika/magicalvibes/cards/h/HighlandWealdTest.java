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
