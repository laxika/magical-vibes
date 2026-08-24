package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbandonThePost.class, Forest.class, GrizzlyBears.class})
class AbandonThePostTest extends BaseCardTest {

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Up to two target creatures can't block this turn")
    void makesTwoCreaturesUnableToBlock() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AbandonThePost()));
        addNormalMana();

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isCantBlockThisTurn()).isTrue();
        assertThat(second.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("May target only one creature")
    void makesOneCreatureUnableToBlock() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AbandonThePost()));
        addNormalMana();

        harness.castSorcery(player1, 0, List.of(bear.getId()));
        harness.passBothPriorities();

        assertThat(bear.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new AbandonThePost()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback makes target creatures unable to block and exiles the spell")
    void flashbackMakesCreaturesUnableToBlockAndExilesSpell() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new AbandonThePost()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, List.of(bear.getId()));
        harness.passBothPriorities();

        assertThat(bear.isCantBlockThisTurn()).isTrue();
        harness.assertNotInGraveyard(player1, "Abandon the Post");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Abandon the Post"));
    }

    @Test
    @DisplayName("The can't-block effect wears off at end of turn")
    void effectWearsOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AbandonThePost()));
        addNormalMana();

        harness.castSorcery(player1, 0, List.of(bear.getId()));
        harness.passBothPriorities();
        assertThat(bear.isCantBlockThisTurn()).isTrue();

        gd.expireEndOfTurnFloatingEffects();
        bear.resetModifiers();

        assertThat(bear.isCantBlockThisTurn()).isFalse();
    }
}
