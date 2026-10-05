package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AvacynsPilgrim;
import com.github.laxika.magicalvibes.cards.b.BrimstoneVolley;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KessigCagebreakers.class, AvacynsPilgrim.class, BrimstoneVolley.class,
        InvasionOfZendikar.class, AwakenedSkyclave.class})
class KessigCagebreakersTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with creatures in graveyard creates Wolf tokens tapped and attacking")
    void attackCreatesWolfTokensPerCreatureInGraveyard() {
        Permanent cagebreakers = harness.addToBattlefieldAndReturn(player1, new KessigCagebreakers());
        cagebreakers.setSummoningSick(false);

        // Put 3 creature cards in the graveyard
        harness.setGraveyard(player1, List.of(new AvacynsPilgrim(), new AvacynsPilgrim(), new AvacynsPilgrim()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        // Resolve the attack trigger
        harness.passBothPriorities();

        // 3 Wolf tokens should be on the battlefield
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        long wolfCount = battlefield.stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Wolf"))
                .count();
        assertThat(wolfCount).isEqualTo(3);

        // Tokens should be tapped and attacking
        battlefield.stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Wolf"))
                .forEach(token -> {
                    assertThat(token.isTapped()).isTrue();
                    assertThat(token.isAttacking()).isTrue();
                    assertThat(token.isAttackedThisTurn()).isFalse();
                    assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
                    assertThat(token.getCard().getPower()).isEqualTo(2);
                    assertThat(token.getCard().getToughness()).isEqualTo(2);
                });
    }

    @Test
    @DisplayName("No tokens created when graveyard has no creature cards")
    void noTokensWithEmptyGraveyard() {
        Permanent cagebreakers = harness.addToBattlefieldAndReturn(player1, new KessigCagebreakers());
        cagebreakers.setSummoningSick(false);

        // Empty graveyard (default)
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        // Resolve the attack trigger
        harness.passBothPriorities();

        // No tokens should be created
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        long wolfCount = battlefield.stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Wolf"))
                .count();
        assertThat(wolfCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Non-creature cards in graveyard are not counted")
    void nonCreatureCardsNotCounted() {
        Permanent cagebreakers = harness.addToBattlefieldAndReturn(player1, new KessigCagebreakers());
        cagebreakers.setSummoningSick(false);

        // 1 creature + 1 non-creature in graveyard
        harness.setGraveyard(player1, List.of(new AvacynsPilgrim(), new BrimstoneVolley()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        // Resolve the attack trigger
        harness.passBothPriorities();

        // Only 1 Wolf token (only the creature card counts)
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        long wolfCount = battlefield.stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Wolf"))
                .count();
        assertThat(wolfCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Wolf tokens are green with Wolf subtype")
    void wolfTokenCharacteristics() {
        Permanent cagebreakers = harness.addToBattlefieldAndReturn(player1, new KessigCagebreakers());
        cagebreakers.setSummoningSick(false);

        harness.setGraveyard(player1, List.of(new AvacynsPilgrim()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        // Resolve the attack trigger
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).filteredOn(p -> p.getCard().isToken()).hasSize(1);
        battlefield.stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Wolf"))
                .forEach(token -> {
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(token.getCard().getSubtypes()).contains(CardSubtype.WOLF);
                });
    }

    @Test
    void countsCurrentGraveyardAtResolutionAndIgnoresOpponentsGraveyard() {
        Permanent cagebreakers = harness.addToBattlefieldAndReturn(player1, new KessigCagebreakers());
        cagebreakers.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new AvacynsPilgrim()));
        harness.setGraveyard(player2, List.of(new AvacynsPilgrim(), new AvacynsPilgrim()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        harness.setGraveyard(player1, List.of(new AvacynsPilgrim(), new AvacynsPilgrim(),
                new BrimstoneVolley()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(2);
    }

    @Test
    void createsNoTokensWhenCreaturesLeaveGraveyardBeforeResolution() {
        Permanent cagebreakers = harness.addToBattlefieldAndReturn(player1, new KessigCagebreakers());
        cagebreakers.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new AvacynsPilgrim()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        harness.setGraveyard(player1, List.of(new BrimstoneVolley()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }

    @Test
    void triggerResolvesAfterSourceLeavesBattlefieldAndCountsSourceInGraveyard() {
        Permanent cagebreakers = harness.addToBattlefieldAndReturn(player1, new KessigCagebreakers());
        cagebreakers.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        gd.playerBattlefields.get(player1.getId()).remove(cagebreakers);
        harness.setGraveyard(player1, List.of(cagebreakers.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(1)
                .allSatisfy(token -> {
                    assertThat(token.isTapped()).isTrue();
                    assertThat(token.isAttacking()).isTrue();
                    assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
                });
    }

    @Test
    void mayChooseToHaveWolfAttackBattleProtectedByOpponent() {
        Permanent cagebreakers = harness.addToBattlefieldAndReturn(player1, new KessigCagebreakers());
        cagebreakers.setSummoningSick(false);
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player2.getId());
        harness.setGraveyard(player1, List.of(new AvacynsPilgrim()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(battle.getId());
        harness.handlePermanentChosen(player1, battle.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(1)
                .allSatisfy(token -> {
                    assertThat(token.isTapped()).isTrue();
                    assertThat(token.isAttacking()).isTrue();
                    assertThat(token.getAttackTarget()).isEqualTo(battle.getId());
                });
    }
}
