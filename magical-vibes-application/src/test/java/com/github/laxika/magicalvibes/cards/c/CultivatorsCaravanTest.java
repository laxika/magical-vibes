package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CultivatorsCaravanTest extends BaseCardTest {

    @Test
    void addsManaOfAnyColor() {
        Permanent caravan = addCaravanReady(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(caravan.isTapped()).isTrue();
    }

    @Test
    void crewAnimatesCaravanAndTapsCrew() {
        Permanent caravan = addCaravanReady(player1);
        Permanent crew = addCreatureReady(player1, new SerraAngel());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(caravan.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, caravan)).isTrue();
        assertThat(crew.isTapped()).isTrue();
        assertThat(caravan.isTapped()).isFalse();
    }

    @Test
    void cannotCrewWithoutEnoughPower() {
        addCaravanReady(player1);
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    private Permanent addCaravanReady(Player player) {
        Permanent permanent = new Permanent(new CultivatorsCaravan());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
