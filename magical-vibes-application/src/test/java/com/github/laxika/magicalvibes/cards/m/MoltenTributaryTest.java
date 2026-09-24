package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MoltenTributary.class)
class MoltenTributaryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new MoltenTributary()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Molten Tributary").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Choosing blue adds one blue mana")
    void choosingBlueAddsMana() {
        Permanent tributary = addReadyTributary(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(tributary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Choosing red adds one red mana")
    void choosingRedAddsMana() {
        Permanent tributary = addReadyTributary(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(tributary.isTapped()).isTrue();
    }

    private Permanent addReadyTributary(Player player) {
        Permanent perm = new Permanent(new MoltenTributary());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
