package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.cards.s.ScrapworkRager;
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

@CardUsed({MomentOfDefiance.class, ScrapworkRager.class, EnergyRefractor.class})
class MomentOfDefianceTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +2/+1 and lifelink and the spell controller draws a card")
    void resolvesBoostLifelinkAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ScrapworkRager());
        harness.setLibrary(player1, List.of(new ScrapworkRager()));
        harness.setHand(player1, List.of(new MomentOfDefiance()));
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Boost and lifelink wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ScrapworkRager());
        harness.setHand(player1, List.of(new MomentOfDefiance()));
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Does not draw if the target is removed before resolution")
    void fizzlesAndDoesNotDrawIfTargetRemoved() {
        harness.addToBattlefield(player1, new ScrapworkRager());
        harness.setLibrary(player1, List.of(new ScrapworkRager()));
        harness.setHand(player1, List.of(new MomentOfDefiance()));
        addMana();

        UUID targetId = harness.getPermanentId(player1, "Scrapwork Rager");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Targeting an opponent's creature draws for the caster and grants lifelink to that creature")
    void opponentCreatureGetsLifelinkButCasterDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScrapworkRager());
        ScrapworkRager drawnCard = new ScrapworkRager();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new MomentOfDefiance()));
        harness.setHand(player2, List.of());
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        gd.activePlayerId = player2.getId();
        target.setSummoningSick(false);
        target.setAttacking(true);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 24);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new EnergyRefractor());
        harness.setHand(player1, List.of(new MomentOfDefiance()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
