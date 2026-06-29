package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KessigCagebreakersTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with creatures in graveyard creates Wolf tokens tapped and attacking")
    void attackCreatesWolfTokensPerCreatureInGraveyard() {
        Permanent cagebreakers = new Permanent(new KessigCagebreakers());
        cagebreakers.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(cagebreakers);

        // Put 3 creature cards in the graveyard
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

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
                    assertThat(token.isAttackedThisTurn()).isTrue();
                    assertThat(token.getCard().getPower()).isEqualTo(2);
                    assertThat(token.getCard().getToughness()).isEqualTo(2);
                });
    }

    @Test
    @DisplayName("No tokens created when graveyard has no creature cards")
    void noTokensWithEmptyGraveyard() {
        Permanent cagebreakers = new Permanent(new KessigCagebreakers());
        cagebreakers.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(cagebreakers);

        // Empty graveyard (default)
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

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
        Permanent cagebreakers = new Permanent(new KessigCagebreakers());
        cagebreakers.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(cagebreakers);

        // 1 creature + 1 non-creature in graveyard
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LightningBolt()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

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
        Permanent cagebreakers = new Permanent(new KessigCagebreakers());
        cagebreakers.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(cagebreakers);

        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));

        // Resolve the attack trigger
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        battlefield.stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Wolf"))
                .forEach(token -> {
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(token.getCard().getSubtypes()).contains(CardSubtype.WOLF);
                });
    }
}
