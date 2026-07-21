package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DocentOfPerfectionTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant creates a 1/1 blue Human Wizard token")
    void castingInstantCreatesWizardToken() {
        harness.addToBattlefield(player1, new DocentOfPerfection());
        int creaturesBefore = gd.playerBattlefields.get(player1.getId()).size();

        castShockAndResolveTrigger();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getName()).isEqualTo("Human Wizard");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.HUMAN, CardSubtype.WIZARD);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(creaturesBefore + 1);
    }

    @Test
    @DisplayName("Casting a creature does not create a token")
    void castingCreatureDoesNotCreateToken() {
        harness.addToBattlefield(player1, new DocentOfPerfection());
        int creaturesBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .count()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(creaturesBefore + 1);
    }

    @Test
    @DisplayName("Does not transform with fewer than three Wizards after creating the token")
    void doesNotTransformWithFewerThanThreeWizards() {
        Permanent docent = harness.addToBattlefieldAndReturn(player1, new DocentOfPerfection());
        harness.addToBattlefield(player1, new FugitiveWizard());

        castShockAndResolveTrigger();

        assertThat(docent.isTransformed()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Transforms when the created token brings Wizard count to three")
    void transformsWhenThirdWizardIsCreated() {
        Permanent docent = harness.addToBattlefieldAndReturn(player1, new DocentOfPerfection());
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player1, new FugitiveWizard());

        castShockAndResolveTrigger();

        assertThat(docent.isTransformed()).isTrue();
        assertThat(docent.getCard().getName()).isEqualTo("Final Iteration");
    }

    @Test
    @DisplayName("Does not transform merely from already controlling three Wizards")
    void doesNotTransformWithoutCastTrigger() {
        Permanent docent = harness.addToBattlefieldAndReturn(player1, new DocentOfPerfection());
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player1, new FugitiveWizard());

        assertThat(docent.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Transforming does not also fire Final Iteration's cast trigger for the same spell")
    void transformDoesNotRetriggerForSameSpell() {
        Permanent docent = harness.addToBattlefieldAndReturn(player1, new DocentOfPerfection());
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player1, new FugitiveWizard());
        int before = gd.playerBattlefields.get(player1.getId()).size();

        castShockAndResolveTrigger();

        assertThat(docent.isTransformed()).isTrue();
        // Only the Docent token — Final Iteration must not also create one for the same cast
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(before + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Final Iteration gives Wizards you control +2/+1 and flying")
    void finalIterationBoostsWizards() {
        DocentOfPerfection card = new DocentOfPerfection();
        Permanent finalIteration = new Permanent(card);
        finalIteration.setCard(card.getBackFaceCard());
        finalIteration.setTransformed(true);
        gd.playerBattlefields.get(player1.getId()).add(finalIteration);

        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentWizard = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());

        // Fugitive Wizard is 1/1; with +2/+1 = 3/2 and flying
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.FLYING)).isTrue();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();

        assertThat(gqs.getEffectivePower(gd, opponentWizard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentWizard)).isEqualTo(1);
    }

    @Test
    @DisplayName("Final Iteration creates a Wizard token when you cast an instant")
    void finalIterationCreatesTokenOnCast() {
        DocentOfPerfection card = new DocentOfPerfection();
        Permanent finalIteration = new Permanent(card);
        finalIteration.setCard(card.getBackFaceCard());
        finalIteration.setTransformed(true);
        gd.playerBattlefields.get(player1.getId()).add(finalIteration);

        castShockAndResolveTrigger();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getSubtypes())
                .contains(CardSubtype.HUMAN, CardSubtype.WIZARD);
        // Token is a Wizard, so it also gets the anthem: 1/1 + 2/1 = 3/2
        assertThat(gqs.getEffectivePower(gd, tokens.getFirst())).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tokens.getFirst())).isEqualTo(2);
    }

    private void castShockAndResolveTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve cast trigger
    }
}
