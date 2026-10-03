package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.j.JaceUnravelerOfSecrets;
import com.github.laxika.magicalvibes.cards.q.QuilledWolf;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.cards.t.TormentingVoice;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvacynsJudgment.class, QuilledWolf.class, RavensCrime.class,
        TormentingVoice.class, JaceUnravelerOfSecrets.class})
class AvacynsJudgmentTest extends BaseCardTest {

    @Test
    void dealsTwoDamageDividedAmongCreatureAndPlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new QuilledWolf());
        int lifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new AvacynsJudgment()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, Map.of(creature.getId(), 1, player2.getId(), 1));
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void madnessDealsXDamageToChosenTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new QuilledWolf());
        AvacynsJudgment judgment = new AvacynsJudgment();
        harness.setHand(player1, List.of(judgment));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.AlternateCastXValueChoice.class);
        harness.handleXValueChosen(player1, 3);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Quilled Wolf");
        harness.assertInGraveyard(player2, "Quilled Wolf");
    }

    @Test
    void normalCastCanDamagePlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceUnravelerOfSecrets());
        harness.setHand(player1, List.of(new AvacynsJudgment()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, Map.of(planeswalker.getId(), 2));
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void rejectsZeroDamageAssignedToATarget() {
        harness.setHand(player1, List.of(new AvacynsJudgment()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                Map.of(player1.getId(), 0, player2.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void madnessWithZeroXIsCastWithoutTargets() {
        prepareMadnessChoice(0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Avacyn's Judgment");
    }

    @Test
    void madnessCanTargetPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceUnravelerOfSecrets());
        prepareMadnessChoice(3);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 3);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).contains(planeswalker.getId());
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void madnessDealsChosenXDamageToPlayer() {
        prepareMadnessChoice(3);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 3);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Avacyn's Judgment");
    }

    @Test
    void decliningMadnessPutsJudgmentIntoGraveyard() {
        prepareMadnessChoice(3);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Avacyn's Judgment");
        harness.assertLife(player2, 20);
    }

    private void prepareMadnessChoice(int x) {
        harness.setHand(player1, List.of(new TormentingVoice(), new AvacynsJudgment()));
        harness.setLibrary(player1, List.of(new QuilledWolf(), new QuilledWolf()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, x + 1);
        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();
    }
}
