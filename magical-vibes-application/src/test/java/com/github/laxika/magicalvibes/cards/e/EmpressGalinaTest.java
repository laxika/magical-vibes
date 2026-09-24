package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CaptainSisay;
import com.github.laxika.magicalvibes.cards.d.DreamThrush;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmpressGalina.class, CaptainSisay.class, DreamThrush.class})
class EmpressGalinaTest extends BaseCardTest {

    @Test
    @DisplayName("Gains permanent control of target legendary permanent")
    void gainsControlOfLegendaryPermanent() {
        Permanent empress = addEmpressGalina();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CaptainSisay());

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, battlefieldIndex(player1, empress), null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Cannot target a nonlegendary permanent")
    void cannotTargetNonlegendaryPermanent() {
        Permanent empress = addEmpressGalina();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DreamThrush());

        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, empress), null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a legendary permanent");
    }

    private Permanent addEmpressGalina() {
        return addCreatureReady(player1, new EmpressGalina());
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
