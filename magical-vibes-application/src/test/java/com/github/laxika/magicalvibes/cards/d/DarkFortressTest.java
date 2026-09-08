package com.github.laxika.magicalvibes.cards.d;

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

@CardUsed({DarkFortress.class, Swamp.class})
class DarkFortressTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Dark Fortress produces colorless mana")
    void tappingProducesColorlessMana() {
        Permanent fortress = addReadyFortress();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(fortress.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Black or red mana requires a newly entered Dark Fortress or a basic land")
    void coloredManaRequiresCondition() {
        Permanent fortress = addReadyFortress();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entered this turn or if you control a basic land");
        assertThat(fortress.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A Dark Fortress that entered this turn can produce black or red mana")
    void newlyEnteredFortressProducesChosenMana() {
        Permanent fortress = harness.addToBattlefieldAndReturn(player1, new DarkFortress());
        gd.permanentsEnteredBattlefieldThisTurn.put(
                player1.getId(), new ArrayList<>(List.of(fortress.getCard())));

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(fortress.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A basic land enables Dark Fortress to produce black or red mana")
    void basicLandEnablesChosenMana() {
        Permanent fortress = addReadyFortress();
        harness.addToBattlefield(player1, new Swamp());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(fortress.isTapped()).isTrue();
    }

    private Permanent addReadyFortress() {
        Permanent fortress = new Permanent(new DarkFortress());
        fortress.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(fortress);
        return fortress;
    }
}
