package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Subterfuge.class, Forest.class, GrizzlyBears.class})
class SubterfugeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters granting flying and draws cards equal to combat damage")
    void entersAndGrantsFlyingAndCombatDamageDraw() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Subterfuge()));
        addManaForCast();

        harness.castCreature(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        bears.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("The temporary flying and draw ability expires at end of turn")
    void temporaryEffectsExpireAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Subterfuge()));
        addManaForCast();

        harness.castCreature(player1, 0, bears.getId());
        resolveAllTriggers();
        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(bears.getTemporaryTriggeredEffects(com.github.laxika.magicalvibes.model.EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER))
                .isEmpty();
    }

    @Test
    @DisplayName("Encore creates an attacking hasty token for each opponent and sacrifices it at the next end step")
    void encoreCreatesAttackingTokenAndSacrificesIt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Subterfuge()));
        addManaForEncore();

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Subterfuge");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        harness.handlePermanentChosen(player1, token.getId());
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Subterfuge")).isEmpty();
    }

    @Test
    @DisplayName("Encore can only be activated at sorcery speed")
    void encoreRequiresSorceryTiming() {
        harness.setGraveyard(player1, List.of(new Subterfuge()));
        addManaForEncore();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private void addManaForCast() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void addManaForEncore() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
    }
}
