package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TanglepoolBridge.class, StoneRain.class})
class TanglepoolBridgeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new TanglepoolBridge()));

        harness.playLand(player1, 0);

        Permanent bridge = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bridge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds green or blue mana")
    void manaAbilityAddsGreenOrBlueMana() {
        Permanent bridge = addReadyBridge();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(bridge.isTapped()).isTrue();

        bridge.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Indestructible keeps it on the battlefield through a destroy effect")
    void survivesDestruction() {
        harness.addToBattlefield(player2, new TanglepoolBridge());
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Tanglepool Bridge");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Tanglepool Bridge");
    }

    private Permanent addReadyBridge() {
        Permanent bridge = new Permanent(new TanglepoolBridge());
        bridge.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bridge);
        return bridge;
    }
}
