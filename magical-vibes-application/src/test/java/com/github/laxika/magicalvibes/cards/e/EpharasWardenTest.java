package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EpharasWardenTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability taps target creature with power 3 or less")
    void resolvingTapsEligibleCreature() {
        addReadyWarden(player1);
        Permanent target = addReady(new HillGiant(), player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating ability taps Ephara's Warden")
    void activatingTapsSelf() {
        Permanent warden = addReadyWarden(player1);
        Permanent target = addReady(new HillGiant(), player2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(warden.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 3")
    void cannotTargetHighPowerCreature() {
        addReadyWarden(player1);
        Permanent wurm = addReady(new CrawWurm(), player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, wurm.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyWarden(Player player) {
        return addReady(new EpharasWarden(), player);
    }

    private Permanent addReady(Card card, Player player) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
