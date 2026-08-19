package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PacificationArrayTest extends BaseCardTest {

    @Test
    void tapsTargetCreature() {
        addReadyPacificationArray(player1);
        Permanent target = addPermanent(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void tapsTargetArtifact() {
        addReadyPacificationArray(player1);
        Permanent target = addPermanent(player2, new AngelsFeather());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void cannotTargetEnchantment() {
        addReadyPacificationArray(player1);
        Permanent target = addPermanent(player2, new Pacifism());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    void activationConsumesTwoManaAndTapsSource() {
        Permanent source = addReadyPacificationArray(player1);
        Permanent target = addPermanent(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(source.isTapped()).isTrue();
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private Permanent addReadyPacificationArray(Player player) {
        return addPermanent(player, new PacificationArray(), false);
    }

    private Permanent addPermanent(Player player, Card card) {
        return addPermanent(player, card, true);
    }

    private Permanent addPermanent(Player player, Card card,
                                   boolean clearSummoningSickness) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(!clearSummoningSickness);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
