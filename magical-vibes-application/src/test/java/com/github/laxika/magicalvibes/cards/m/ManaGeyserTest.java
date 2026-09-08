package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ManaGeyserTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one red mana for each tapped land controlled by an opponent")
    void addsRedManaForEachTappedOpponentLand() {
        Permanent ownTappedLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        ownTappedLand.tap();
        Permanent tappedOpponentLand = harness.addToBattlefieldAndReturn(player2, new Mountain());
        tappedOpponentLand.tap();
        Permanent secondTappedOpponentLand = harness.addToBattlefieldAndReturn(player2, new Mountain());
        secondTappedOpponentLand.tap();
        harness.addToBattlefield(player2, new Mountain());
        Permanent tappedOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        tappedOpponentCreature.tap();

        harness.setHand(player1, List.of(new ManaGeyser()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts tapped lands when the spell resolves")
    void countsTappedLandsAtResolution() {
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.setHand(player1, List.of(new ManaGeyser()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, 0);
        opponentLand.tap();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }
}
