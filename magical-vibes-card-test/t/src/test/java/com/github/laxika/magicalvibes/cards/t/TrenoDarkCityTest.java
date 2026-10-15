package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TrenoDarkCity.class)
class TrenoDarkCityTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new TrenoDarkCity()));

        harness.playLand(player1, 0);

        Permanent treno = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(treno.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds blue mana when blue is chosen")
    void manaAbilityAddsBlueMana() {
        Permanent treno = addReadyTreno();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(treno.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds black mana when black is chosen")
    void manaAbilityAddsBlackMana() {
        Permanent treno = addReadyTreno();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(treno.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped Treno cannot activate its mana ability")
    void tappedLandCannotProduceMana() {
        harness.setHand(player1, List.of(new TrenoDarkCity()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A newly entered noncreature land can produce mana after being untapped")
    void newlyEnteredLandCanProduceManaAfterUntapping() {
        harness.setHand(player1, List.of(new TrenoDarkCity()));
        harness.playLand(player1, 0);
        Permanent treno = gd.playerBattlefields.get(player1.getId()).getFirst();
        treno.untap();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(treno.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mana goes only to the activating controller and cannot be produced twice without untapping")
    void manaGoesToControllerAndRequiresUntappingToActivateAgain() {
        Permanent treno = harness.addToBattlefieldAndReturn(player2, new TrenoDarkCity());

        harness.activateAbility(player2, 0, 0, null, null);
        harness.handleListChoice(player2, "BLACK");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(treno.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
    }

    private Permanent addReadyTreno() {
        Permanent treno = harness.addToBattlefieldAndReturn(player1, new TrenoDarkCity());
        treno.setSummoningSick(false);
        return treno;
    }
}
