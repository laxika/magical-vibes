package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SanctuaryLockdown.class, EliteVanguard.class, GrizzlyBears.class})
class SanctuaryLockdownTest extends BaseCardTest {

    @Test
    @DisplayName("Humans you control get +1/+1")
    void buffsHumansYouControl() {
        Permanent human = addReady(player1, new EliteVanguard());
        Permanent nonHuman = addReady(player1, new GrizzlyBears());
        Permanent opponentHuman = addReady(player2, new EliteVanguard());
        addReady(player1, new SanctuaryLockdown());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, nonHuman)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonHuman)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentHuman)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentHuman)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping two Humans taps an opponent's target creature")
    void tapsTwoHumansAndOpponentCreature() {
        Permanent lockdown = addReady(player1, new SanctuaryLockdown());
        Permanent human1 = addReady(player1, new EliteVanguard());
        Permanent human2 = addReady(player1, new EliteVanguard());
        Permanent target = addReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lockdown), null, target.getId());
        harness.passBothPriorities();

        assertThat(human1.isTapped()).isTrue();
        assertThat(human2.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(lockdown.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        Permanent lockdown = addReady(player1, new SanctuaryLockdown());
        Permanent human1 = addReady(player1, new EliteVanguard());
        Permanent human2 = addReady(player1, new EliteVanguard());
        Permanent ownTarget = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(lockdown),
                null,
                ownTarget.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(human1.isTapped()).isFalse();
        assertThat(human2.isTapped()).isFalse();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
