package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PristineTalisman.class})
class PristineTalismanTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Pristine Talisman adds one colorless mana and gains 1 life")
    void tapForManaAndLifeGain() {
        Permanent talisman = harness.addToBattlefieldAndReturn(player1, new PristineTalisman());
        harness.setLife(player1, 20);

        talisman.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(talisman.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability resolves as mana ability — does not use the stack")
    void manaAbilityDoesNotUseStack() {
        Permanent talisman = harness.addToBattlefieldAndReturn(player1, new PristineTalisman());
        harness.setLife(player1, 20);

        talisman.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can activate multiple times across turns")
    void multipleActivations() {
        Permanent talisman = harness.addToBattlefieldAndReturn(player1, new PristineTalisman());
        harness.setLife(player1, 20);

        talisman.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);

        // Untap and activate again
        talisman.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("A newly entered noncreature Talisman can immediately produce mana and life")
    void newlyEnteredTalismanCanActivate() {
        Permanent talisman = harness.addToBattlefieldAndReturn(player1, new PristineTalisman());
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(talisman.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Talisman cannot add mana or gain life again")
    void tappedTalismanCannotActivateAgain() {
        harness.addToBattlefield(player1, new PristineTalisman());
        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.stack).isEmpty();
    }
}
