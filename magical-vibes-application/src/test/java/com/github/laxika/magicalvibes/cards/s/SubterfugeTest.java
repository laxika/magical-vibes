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
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Subterfuge()));
        addManaForCast();

        harness.castCreature(player1, 0, bears.getId());
        resolveAllTriggers();
        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.FLYING)).isFalse();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        bears.setAttacking(true);
        bears.setAttackTarget(player2.getId());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        resolveAllTriggers();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Encore creates an untapped hasty token and sacrifices it at the next end step")
    void encoreCreatesUntappedTokenAndSacrificesIt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Subterfuge()));
        addManaForEncore();

        harness.activateGraveyardAbility(player1, 0);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(1);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Subterfuge");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();
        harness.handlePermanentChosen(player1, token.getId());
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Subterfuge")).isEmpty();
    }

    @Test
    @DisplayName("An opposing creature receives the ability and its controller draws the cards")
    void opposingCreatureControllerDraws() {
        Permanent target = addCreatureReady(player2, new Subterfuge());
        harness.setLibrary(player2, List.of(new Subterfuge(), new Subterfuge(), new Subterfuge(), new Subterfuge()));
        harness.setHand(player1, List.of(new Subterfuge()));
        addManaForCast();

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        int controllerHandBefore = gd.playerHands.get(player2.getId()).size();
        int casterHandBefore = gd.playerHands.get(player1.getId()).size();
        target.setAttacking(true);
        target.setAttackTarget(player1.getId());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(controllerHandBefore + 3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(casterHandBefore);
    }

    @Test
    @DisplayName("Encore activated after combat creates a token that is not attacking")
    void encoreAfterCombatDoesNotCreateAttacker() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Subterfuge()));
        addManaForEncore();

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Subterfuge");
        assertThat(token.isAttacking()).isFalse();
        assertThat(token.isTapped()).isFalse();
        harness.handlePermanentChosen(player1, token.getId());
        resolveAllTriggers();
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
