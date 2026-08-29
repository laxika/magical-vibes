package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        Permanent chasm = addReadyChasm();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLACK.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(chasm.isTapped()).isTrue();

        chasm.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    private Permanent addReadyChasm() {
        Permanent chasm = new Permanent(new WoodlandChasm());
        chasm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(chasm);
        return chasm;
    }
}
