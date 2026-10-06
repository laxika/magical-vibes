package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeaGateLoremaster.class, Forest.class, GrizzlyBears.class, StoneworkPuma.class, IntoTheRoil.class})
class SeaGateLoremasterTest extends BaseCardTest {

    @Test
    @DisplayName("Draws one card for each Ally you control")
    void drawsForEachAllyYouControl() {
        Permanent loremaster = addCreatureReady(player1, new SeaGateLoremaster());
        harness.addToBattlefield(player1, new SeaGateLoremaster());
        harness.addToBattlefield(player1, new SeaGateLoremaster());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SeaGateLoremaster());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(loremaster.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void countsAlliesThatEnterBeforeResolution() {
        addCreatureReady(player1, new SeaGateLoremaster());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.enterBattlefieldAndReturn(player1, new StoneworkPuma());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void resolvesAfterSourceLeavesAndCountsOnlyRemainingAllies() {
        Permanent loremaster = addCreatureReady(player1, new SeaGateLoremaster());
        harness.addToBattlefield(player1, new StoneworkPuma());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, loremaster.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Sea Gate Loremaster");
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).contains(loremaster.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawsNothingWhenNoAlliesRemainAtResolution() {
        Permanent loremaster = addCreatureReady(player1, new SeaGateLoremaster());
        harness.addToBattlefield(player2, new StoneworkPuma());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, loremaster.getId());
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(loremaster.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent loremaster = harness.addToBattlefieldAndReturn(player1, new SeaGateLoremaster());
        loremaster.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(loremaster.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent loremaster = addCreatureReady(player1, new SeaGateLoremaster());
        loremaster.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
    }
}
