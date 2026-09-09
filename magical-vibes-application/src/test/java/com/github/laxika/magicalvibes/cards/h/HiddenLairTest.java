package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HiddenLair.class, Swamp.class})
class HiddenLairTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Hidden Lair produces colorless mana")
    void tappingProducesColorlessMana() {
        Permanent lair = addReadyLair();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(lair.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Blue or black mana requires a newly entered Hidden Lair or a basic land")
    void coloredManaRequiresCondition() {
        Permanent lair = addReadyLair();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entered this turn or if you control a basic land");
        assertThat(lair.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A Hidden Lair that entered this turn can produce blue or black mana")
    void newlyEnteredLairProducesChosenMana() {
        Permanent lair = harness.addToBattlefieldAndReturn(player1, new HiddenLair());
        gd.permanentsEnteredBattlefieldThisTurn.put(
                player1.getId(), new ArrayList<>(List.of(lair.getCard())));

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(lair.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A basic land enables Hidden Lair to produce blue or black mana")
    void basicLandEnablesChosenMana() {
        Permanent lair = addReadyLair();
        harness.addToBattlefield(player1, new Swamp());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(lair.isTapped()).isTrue();
    }

    private Permanent addReadyLair() {
        Permanent lair = new Permanent(new HiddenLair());
        lair.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(lair);
        return lair;
    }
}
