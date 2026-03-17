package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.laxika.magicalvibes.model.ManaColor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GeistOfSaintTraftTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Geist creates a 4/4 Angel token with flying tapped and attacking")
    void attackCreatesAngelToken() {
        Permanent geist = new Permanent(new GeistOfSaintTraft());
        geist.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(geist);

        // Give player2 a playable instant to prevent auto-pass
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));

        // Stack should have the token creation trigger
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        // Resolve the trigger
        harness.passBothPriorities();

        // Angel token should be on the battlefield
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> angelTokens = battlefield.stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Angel"))
                .toList();
        assertThat(angelTokens).hasSize(1);

        Permanent angel = angelTokens.getFirst();
        assertThat(angel.getCard().getPower()).isEqualTo(4);
        assertThat(angel.getCard().getToughness()).isEqualTo(4);
        assertThat(angel.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(angel.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(angel.getCard().getSubtypes()).contains(CardSubtype.ANGEL);
        assertThat(angel.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(angel.isTapped()).isTrue();
        assertThat(angel.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Angel token is scheduled for exile at end of combat")
    void angelTokenScheduledForExile() {
        Permanent geist = new Permanent(new GeistOfSaintTraft());
        geist.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(geist);

        // Give player2 a playable instant to prevent auto-pass
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        // The Angel token should be in the pending exile at end of combat set
        assertThat(gd.pendingTokenExilesAtEndOfCombat).hasSize(1);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent angel = battlefield.stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Angel"))
                .findFirst().orElseThrow();
        assertThat(gd.pendingTokenExilesAtEndOfCombat).contains(angel.getId());
    }

    @Test
    @DisplayName("Angel token is exiled when leaving end of combat step")
    void angelTokenExiledAtEndOfCombat() {
        Permanent geist = new Permanent(new GeistOfSaintTraft());
        geist.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(geist);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));
        // Let auto-pass take care of everything through end of combat
        harness.passBothPriorities();

        // By now auto-pass has advanced through the whole combat phase
        // Angel token should be gone from the battlefield
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield.stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Angel"))
                .count()).isEqualTo(0);

        // Geist should still be on the battlefield
        assertThat(battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Geist of Saint Traft"))
                .count()).isEqualTo(1);

        // Pending exiles should be cleared
        assertThat(gd.pendingTokenExilesAtEndOfCombat).isEmpty();
    }

    @Test
    @DisplayName("Geist of Saint Traft does not create tokens when not attacking")
    void noTokenWhenNotAttacking() {
        Permanent geist = new Permanent(new GeistOfSaintTraft());
        geist.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(geist);

        // Just being on the battlefield doesn't create tokens
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        long angelCount = battlefield.stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Angel"))
                .count();
        assertThat(angelCount).isEqualTo(0);
    }
}
