package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TreacherousGreed.class, GrizzlyBears.class, ProdigalSorcerer.class})
class TreacherousGreedTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature that dealt damage, then draws and changes life totals")
    void sacrificesDamagingCreatureAndResolvesAllEffects() {
        Permanent sorcerer = new Permanent(new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sorcerer);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new TreacherousGreed()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, null, sorcerer.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Prodigal Sorcerer");
        harness.assertInGraveyard(player1, "Prodigal Sorcerer");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Cannot sacrifice a creature that dealt no damage this turn")
    void cannotSacrificeCreatureThatDealtNoDamage() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new TreacherousGreed()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
