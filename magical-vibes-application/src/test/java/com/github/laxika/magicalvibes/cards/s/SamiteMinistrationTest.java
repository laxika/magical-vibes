package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AncientKavu;
import com.github.laxika.magicalvibes.cards.d.Duskwalker;
import com.github.laxika.magicalvibes.cards.l.LlanowarElite;
import com.github.laxika.magicalvibes.cards.z.Zap;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SamiteMinistration.class, AncientKavu.class, Duskwalker.class, LlanowarElite.class, Zap.class})
class SamiteMinistrationTest extends BaseCardTest {

    @Test
    void resolvingPromptsForSourceChoice() {
        castSamiteMinistration();
        addCreatureReady(player2, new AncientKavu());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    void preventsRedCombatDamageAndGainsThatMuchLife() {
        harness.setLife(player1, 20);
        Permanent source = addCreatureReady(player2, new AncientKavu());
        castSamiteMinistration();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        source.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 23);
    }

    @Test
    void preventsBlackCombatDamageAndGainsThatMuchLife() {
        harness.setLife(player1, 20);
        Permanent source = addCreatureReady(player2, new Duskwalker());
        castSamiteMinistration();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        source.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 21);
    }

    @Test
    void preventsOtherColorCombatDamageWithoutGainingLife() {
        harness.setLife(player1, 20);
        Permanent source = addCreatureReady(player2, new LlanowarElite());
        castSamiteMinistration();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        source.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    void preventsDamageOnlyFromTheChosenSource() {
        harness.setLife(player1, 20);
        Permanent chosenSource = addCreatureReady(player2, new AncientKavu());
        Permanent otherSource = addCreatureReady(player2, new AncientKavu());
        castSamiteMinistration();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosenSource.getId());

        chosenSource.setAttacking(true);
        otherSource.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    void preventsDamageFromAChosenRedSpellAndGainsThatMuchLife() {
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Zap zap = new Zap();
        harness.setHand(player2, List.of(zap));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, player1.getId());
        castSamiteMinistration();

        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(zap.getId());

        harness.handlePermanentChosen(player1, zap.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    void doesNotPreventChosenSourceDamageToAnotherPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent source = addCreatureReady(player1, new AncientKavu());
        castSamiteMinistration();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        source.setAttacking(true);
        resolveCombat(player1);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    @Test
    void preventionExpiresAtEndOfTurn() {
        harness.setLife(player1, 20);
        Permanent source = addCreatureReady(player2, new AncientKavu());
        castSamiteMinistration();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        source.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 17);
    }

    private void castSamiteMinistration() {
        harness.castFromHand(player1, new SamiteMinistration(), "{1}{W}");
    }
}
