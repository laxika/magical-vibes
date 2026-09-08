package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ParadiseDruid.class, Shock.class})
class ParadiseDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Paradise Druid has hexproof only while untapped")
    void hexproofDependsOnTappedState() {
        Permanent druid = addCreatureReady(player1, new ParadiseDruid());

        assertThat(gqs.hasKeyword(gd, druid, Keyword.HEXPROOF)).isTrue();

        druid.tap();
        assertThat(gqs.hasKeyword(gd, druid, Keyword.HEXPROOF)).isFalse();

        druid.untap();
        assertThat(gqs.hasKeyword(gd, druid, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("An opponent cannot target an untapped Paradise Druid")
    void opponentCannotTargetUntappedDruid() {
        Permanent druid = addCreatureReady(player1, new ParadiseDruid());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, druid.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("A tapped Paradise Druid can be targeted by an opponent")
    void opponentCanTargetTappedDruid() {
        Permanent druid = addCreatureReady(player1, new ParadiseDruid());
        druid.tap();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, druid.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Paradise Druid");
    }

    @Test
    @DisplayName("Paradise Druid adds one mana of the chosen color")
    void addsChosenColorMana() {
        Permanent druid = addCreatureReady(player1, new ParadiseDruid());
        int druidIndex = gd.playerBattlefields.get(player1.getId()).indexOf(druid);

        harness.activateAbility(player1, druidIndex, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }
}
