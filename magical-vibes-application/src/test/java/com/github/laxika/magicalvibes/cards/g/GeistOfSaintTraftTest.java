package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SpideryGrasp;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GeistOfSaintTraft.class, SpideryGrasp.class})
class GeistOfSaintTraftTest extends BaseCardTest {

    @Test
    void opponentCannotTargetGeist() {
        Permanent geist = addCreatureReady(player1, new GeistOfSaintTraft());
        harness.setHand(player2, List.of(new SpideryGrasp()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, geist.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    void controllerCanTargetGeist() {
        Permanent geist = addCreatureReady(player1, new GeistOfSaintTraft());
        geist.tap();
        harness.setHand(player1, List.of(new SpideryGrasp()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, geist.getId());

        assertThat(geist.isTapped()).isFalse();
    }

    @Test
    void exileTriggerRetainsGeistAsItsSource() {
        addCreatureReady(player1, new GeistOfSaintTraft());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
        });

        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(countPermanents(player1, "Angel")).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(GeistOfSaintTraft.class);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        harness.withAutoStop(TurnStep.END_OF_COMBAT, harness::passBothPriorities);
        assertThat(countPermanents(player1, "Angel")).isZero();
    }

    @Test
    void unblockedGeistAndAngelBothDealCombatDamage() {
        addCreatureReady(player1, new GeistOfSaintTraft());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        harness.assertOnBattlefield(player1, "Geist of Saint Traft");
        harness.assertNotOnBattlefield(player1, "Angel");
    }

    @Test
    @DisplayName("Attacking with Geist creates a 4/4 Angel token with flying tapped and attacking")
    void attackCreatesAngelToken() {
        addCreatureReady(player1, new GeistOfSaintTraft());

        // Give player2 a playable instant to prevent auto-pass
        harness.setHand(player2, List.of(new SpideryGrasp()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        declareAttackers(List.of(0));

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
        assertThat(angel.isAttacking()).isTrue();
        assertThat(angel.isAttackedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Angel token is scheduled for exile at end of combat")
    void angelTokenScheduledForExile() {
        addCreatureReady(player1, new GeistOfSaintTraft());

        // Give player2 a playable instant to prevent auto-pass
        harness.setHand(player2, List.of(new SpideryGrasp()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        // The Angel token should be in the pending exile at end of combat set
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).hasSize(1);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent angel = battlefield.stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Angel"))
                .findFirst().orElseThrow();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).contains(new DelayedPermanentAction(angel.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT));
    }

    @Test
    @DisplayName("Angel token is exiled during the end of combat step")
    void angelTokenExiledAtEndOfCombat() {
        addCreatureReady(player1, new GeistOfSaintTraft());

        declareAttackers(List.of(0));
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
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).isEmpty();
    }

    @Test
    @DisplayName("Geist of Saint Traft does not create tokens when not attacking")
    void noTokenWhenNotAttacking() {
        addCreatureReady(player1, new GeistOfSaintTraft());

        // Just being on the battlefield doesn't create tokens
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        long angelCount = battlefield.stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Angel"))
                .count();
        assertThat(angelCount).isEqualTo(0);
    }
}
