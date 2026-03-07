package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PristineTalismanTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Pristine Talisman adds one colorless mana and gains 1 life")
    void tapForManaAndLifeGain() {
        harness.addToBattlefield(player1, new PristineTalisman());
        harness.setLife(player1, 20);

        Permanent talisman = gd.playerBattlefields.get(player1.getId()).getFirst();
        talisman.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(talisman.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability resolves as mana ability — does not use the stack")
    void manaAbilityDoesNotUseStack() {
        harness.addToBattlefield(player1, new PristineTalisman());
        harness.setLife(player1, 20);

        Permanent talisman = gd.playerBattlefields.get(player1.getId()).getFirst();
        talisman.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can activate multiple times across turns")
    void multipleActivations() {
        harness.addToBattlefield(player1, new PristineTalisman());
        harness.setLife(player1, 20);

        Permanent talisman = gd.playerBattlefields.get(player1.getId()).getFirst();
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
}
