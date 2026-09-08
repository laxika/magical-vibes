package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(prowler)));
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(prowler))));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(prowler);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }
}
