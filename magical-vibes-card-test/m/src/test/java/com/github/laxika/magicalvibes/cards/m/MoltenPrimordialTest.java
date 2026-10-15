package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArmoredTransport;
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

@CardUsed({MoltenPrimordial.class, ArmoredTransport.class})
class MoltenPrimordialTest extends BaseCardTest {

    @Test
    @DisplayName("Steals, untaps and hastes the targeted opponent creature")
    void stealsUntapsAndHastesTarget() {
        Permanent bear = addCreatureReady(player2, new ArmoredTransport());
        bear.tap();

        castMoltenPrimordial(List.of(bear.getId()));

        assertThat(bear.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(bear.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(bear.getId()));
        assertThat(bear.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(bear.getId())).isTrue();
    }

    @Test
    @DisplayName("Control and haste wear off at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent bear = addCreatureReady(player2, new ArmoredTransport());

        castMoltenPrimordial(List.of(bear.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(bear.getId()));
        assertThat(bear.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(bear.getId())).isFalse();
    }

    @Test
    @DisplayName("Up to one target — no target may be chosen")
    void canChooseNoTargets() {
        addCreatureReady(player2, new ArmoredTransport());

        castMoltenPrimordial(List.of());

        harness.assertOnBattlefield(player1, "Molten Primordial");
    }

    @Test
    @DisplayName("Cannot steal two creatures controlled by the same opponent")
    void cannotChooseTwoCreaturesOfSameOpponent() {
        Permanent firstBear = addCreatureReady(player2, new ArmoredTransport());
        Permanent secondBear = addCreatureReady(player2, new ArmoredTransport());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                List.of(firstBear.getId(), secondBear.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    @Test
    @DisplayName("Cannot target a creature its controller already controls")
    void cannotTargetOwnCreature() {
        Permanent ownBear = addCreatureReady(player1, new ArmoredTransport());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(ownBear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enters normally when the opponent has no creatures")
    void entersWithNoOpponentCreatures() {
        castMoltenPrimordial(List.of());

        harness.assertOnBattlefield(player1, "Molten Primordial");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Unchosen creatures are not stolen, untapped or granted haste")
    void leavesUnchosenCreatureUnaffected() {
        Permanent chosen = addCreatureReady(player2, new ArmoredTransport());
        Permanent unchosen = addCreatureReady(player2, new ArmoredTransport());
        chosen.tap();
        unchosen.tap();

        castMoltenPrimordial(List.of(chosen.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(chosen).doesNotContain(unchosen);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(unchosen).doesNotContain(chosen);
        assertThat(chosen.isTapped()).isFalse();
        assertThat(chosen.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(unchosen.isTapped()).isTrue();
        assertThat(unchosen.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(unchosen.getId())).isFalse();
    }

    private void castMoltenPrimordial(List<UUID> targetIds) {
        prepareCast();
        harness.castCreature(player1, 0, targetIds);
        resolveAllTriggers();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new MoltenPrimordial()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
