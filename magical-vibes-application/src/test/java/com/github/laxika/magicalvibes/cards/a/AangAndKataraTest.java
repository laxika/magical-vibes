package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AangAndKatara.class, GrizzlyBears.class, Ornithopter.class, Forest.class, MindStone.class})
class AangAndKataraTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates one Ally token for each tapped artifact or creature you control")
    void enteringCountsTappedArtifactsAndCreatures() {
        Permanent tappedBear = addCreatureReady(player1, new GrizzlyBears());
        tappedBear.tap();
        Permanent tappedOrnithopter = addCreatureReady(player1, new Ornithopter());
        tappedOrnithopter.tap();
        addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentTappedBear = addCreatureReady(player2, new GrizzlyBears());
        opponentTappedBear.tap();

        castAangAndKatara();

        assertThat(countPermanents(player1, "Ally")).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking creates tokens based on the tapped permanents at resolution")
    void attackingCountsTappedPermanents() {
        addCreatureReady(player1, new AangAndKatara());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Ally")).isEqualTo(1);
    }

    @Test
    @DisplayName("Entering with no tapped artifacts or creatures creates no tokens")
    void enteringWithZeroCountCreatesNoTokens() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new Forest()).tap();
        addCreatureReady(player2, new GrizzlyBears()).tap();

        castAangAndKatara();

        assertThat(countPermanents(player1, "Ally")).isZero();
        assertThat(countPermanents(player2, "Ally")).isZero();
    }

    @Test
    @DisplayName("Tapped noncreature artifacts count but ordinary tapped lands do not")
    void countsNoncreatureArtifactsButNotLands() {
        harness.addToBattlefieldAndReturn(player1, new MindStone()).tap();
        harness.addToBattlefield(player1, new MindStone());
        harness.addToBattlefieldAndReturn(player1, new Forest()).tap();
        harness.addToBattlefieldAndReturn(player2, new MindStone()).tap();

        castAangAndKatara();

        assertThat(countPermanents(player1, "Ally")).isEqualTo(1);
        assertThat(countPermanents(player2, "Ally")).isZero();
        Permanent token = findPermanent(player1, "Ally");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ALLY);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("The attack trigger counts permanents tapped after it triggers")
    void attackCountIncludesPermanentsTappedBeforeResolution() {
        addCreatureReady(player1, new AangAndKatara());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(countPermanents(player1, "Ally")).isZero();
            assertThat(gd.stack).isNotEmpty();
            bear.tap();
            resolveAllTriggers();
        });

        assertThat(countPermanents(player1, "Ally")).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack trigger excludes permanents untapped before it resolves")
    void attackCountExcludesPermanentsUntappedBeforeResolution() {
        addCreatureReady(player1, new AangAndKatara());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.tap();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(countPermanents(player1, "Ally")).isZero();
            assertThat(gd.stack).isNotEmpty();
            bear.untap();
            resolveAllTriggers();
        });

        assertThat(countPermanents(player1, "Ally")).isEqualTo(1);
    }

    private void castAangAndKatara() {
        harness.setHand(player1, List.of(new AangAndKatara()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
