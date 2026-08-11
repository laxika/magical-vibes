package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerfolkLooter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WanderwineFarewellTest extends BaseCardTest {

    @Test
    @DisplayName("Returns one or two target nonland permanents and creates one token per permanent with a Merfolk")
    void returnsTargetsAndCreatesMatchingTokens() {
        harness.addToBattlefield(player1, new MerfolkLooter());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castFarewell(List.of(bear.getId(), secondBear.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bear.getId()) || p.getId().equals(secondBear.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .hasSize(2);
    }

    @Test
    @DisplayName("Does not create tokens when the controller does not control a Merfolk")
    void noMerfolkMeansNoTokens() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castFarewell(List.of(bear.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .isEmpty();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castFarewell(List<UUID> targetIds) {
        prepareCast();
        harness.castInstant(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new WanderwineFarewell()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
