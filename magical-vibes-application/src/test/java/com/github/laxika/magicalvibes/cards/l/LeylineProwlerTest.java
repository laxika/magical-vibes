package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeylineProwler.class, ColossalDreadmaw.class})
class LeylineProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Leyline Prowler adds one mana of the chosen color")
    void addsManaOfAnyColor() {
        Permanent prowler = addCreatureReady(player1, new LeylineProwler());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(prowler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Lifelink gains life from combat damage")
    void lifelinkGainsLife() {
        Permanent prowler = addCreatureReady(player1, new LeylineProwler());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(prowler)));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deathtouch destroys a larger blocker in combat")
    void deathtouchDestroysLargerBlocker() {
        Permanent prowler = addCreatureReady(player1, new LeylineProwler());
        Permanent blocker = addCreatureReady(player2, new ColossalDreadmaw());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(prowler)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(prowler))));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(prowler);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "GREEN"})
    void addsOtherColorsWithoutUsingTheStack(ManaColor color) {
        Permanent prowler = addCreatureReady(player1, new LeylineProwler());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(prowler.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        Permanent prowler = harness.addToBattlefieldAndReturn(player1, new LeylineProwler());
        prowler.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(prowler.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent prowler = addCreatureReady(player1, new LeylineProwler());
        prowler.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    void deathtouchAndLifelinkApplyWhileBlockingEvenWhenBothCreaturesDie() {
        Permanent attacker = addCreatureReady(player1, new LeylineProwler());
        Permanent blocker = addCreatureReady(player2, new LeylineProwler());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
    }
}
