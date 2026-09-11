package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MassMutiny.class, GrizzlyBears.class})
class MassMutinyTest extends BaseCardTest {

    @Test
    @DisplayName("Steals, untaps and hastes up to one creature per opponent")
    void stealsUntapsAndHastesTargetedCreatures() {
        Permanent bear = addCreatureReady(player2);
        bear.tap();

        castMassMutiny(List.of(bear.getId()));

        assertThat(bear.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        assertThat(bear.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(bear.getId())).isTrue();
    }

    @Test
    @DisplayName("Control and haste wear off at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent bear = addCreatureReady(player2);

        castMassMutiny(List.of(bear.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
        assertThat(bear.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(bear.getId())).isFalse();
    }

    @Test
    @DisplayName("Allows choosing no creatures")
    void canChooseNoTargets() {
        castMassMutiny(List.of());

        harness.assertInGraveyard(player1, "Mass Mutiny");
    }

    @Test
    @DisplayName("Cannot choose two creatures controlled by the same opponent")
    void cannotChooseTwoCreaturesOfSameOpponent() {
        Permanent firstBear = addCreatureReady(player2);
        Permanent secondBear = addCreatureReady(player2);
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(firstBear.getId(), secondBear.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    @Test
    @DisplayName("Cannot target a creature its controller already controls")
    void cannotTargetOwnCreature() {
        Permanent ownBear = addCreatureReady(player1);
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(ownBear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    private void castMassMutiny(List<UUID> targetIds) {
        prepareCast();
        harness.castSorcery(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new MassMutiny()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
