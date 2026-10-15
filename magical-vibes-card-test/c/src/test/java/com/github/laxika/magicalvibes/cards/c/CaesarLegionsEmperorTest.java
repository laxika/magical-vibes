package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaesarLegionsEmperor.class, GrizzlyBears.class})
class CaesarLegionsEmperorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature allows two modes and counts tokens created earlier")
    void sacrificeAllowsTwoModes() {
        gd.playerAutoStopSteps.put(player1.getId(), java.util.EnumSet.of(
                com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS));
        gd.playerAutoStopSteps.put(player2.getId(), java.util.EnumSet.of(
                com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS));
        addCreatureReady(player1, new CaesarLegionsEmperor());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        int opponentLifeBefore = gd.getLife(player2.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1,
                "Create two 1/1 red and white Soldier creature tokens with haste that are tapped and attacking");
        harness.handleListChoice(player1,
                "Caesar deals damage equal to the number of creature tokens you control to target opponent");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        List<Permanent> soldiers = findPermanents(player1, "Soldier");
        assertThat(soldiers).hasSize(2);
        assertThat(soldiers).allSatisfy(soldier -> {
            assertThat(soldier.isTapped()).isTrue();
            assertThat(soldier.isAttacking()).isTrue();
            assertThat(gqs.hasKeyword(gd, soldier, Keyword.HASTE)).isTrue();
            assertThat(soldier.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
        });
        resolveCombat();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 8);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The draw mode draws a card and loses 1 life")
    void drawModeDrawsAndLosesLife() {
        addCreatureReady(player1, new CaesarLegionsEmperor());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Draw a card and lose 1 life");
        harness.handleListChoice(player1,
                "Caesar deals damage equal to the number of creature tokens you control to target opponent");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the sacrifice does nothing")
    void decliningSacrificeDoesNothing() {
        addCreatureReady(player1, new CaesarLegionsEmperor());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Reflexive modes and target are chosen before players receive priority")
    void reflexiveChoicesPrecedePriority() {
        addCreatureReady(player1, new CaesarLegionsEmperor());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1,
                "Create two 1/1 red and white Soldier creature tokens with haste that are tapped and attacking");
        harness.handleListChoice(player1,
                "Caesar deals damage equal to the number of creature tokens you control to target opponent");
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Soldier")).hasSize(2);
    }

    @Test
    @DisplayName("Token and draw modes work together when selected in reverse order")
    void tokenAndDrawModes() {
        addCreatureReady(player1, new CaesarLegionsEmperor());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.handleListChoice(player1, "Draw a card and lose 1 life");
        harness.handleListChoice(player1,
                "Create two 1/1 red and white Soldier creature tokens with haste that are tapped and attacking");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).hasSize(2);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player1, lifeBefore - 1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Caesar triggers when only another creature attacks and that attacker can be sacrificed")
    void caesarDoesNotNeedToAttack() {
        addCreatureReady(player1, new CaesarLegionsEmperor());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.handleListChoice(player1, "Draw a card and lose 1 life");
        harness.handleListChoice(player1,
                "Create two 1/1 red and white Soldier creature tokens with haste that are tapped and attacking");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Soldier")).hasSize(2);
        assertThat(findPermanent(player1, "Caesar, Legion's Emperor").isTapped()).isFalse();
    }
}
