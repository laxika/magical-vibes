package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlamesOfTheRazeBoarTest extends BaseCardTest {

    @Test
    void dealsFourDamageToTargetAndTwoToEachOtherCreatureWhenControllerHasPowerFourCreature() {
        Permanent target = addReadyCreature(player2, new ColossalDreadmaw());
        Permanent other = addReadyCreature(player2, new HillGiant());
        Permanent source = addReadyCreature(player1, new CrawWurm());

        castFlames(player1, target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(other.getMarkedDamage()).isEqualTo(2);
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    void onlyDealsFourDamageWhenControllerHasNoPowerFourCreature() {
        Permanent target = addReadyCreature(player2, new ColossalDreadmaw());
        Permanent other = addReadyCreature(player2, new HillGiant());

        castFlames(player1, target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(other.getMarkedDamage()).isZero();
    }

    @Test
    void canOnlyTargetAnOpponentsCreature() {
        Permanent ownCreature = addReadyCreature(player1, new HillGiant());

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FlamesOfTheRazeBoar()));
        addMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castFlames(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new FlamesOfTheRazeBoar()));
        addMana(caster);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana(Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 5);
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
