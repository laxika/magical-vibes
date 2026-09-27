package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MisleadingSignpost.class, GrizzlyBears.class})
class MisleadingSignpostTest extends BaseCardTest {

    @Test
    void addsBlueMana() {
        Permanent signpost = harness.addToBattlefieldAndReturn(player1, new MisleadingSignpost());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(signpost), null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void reselectsAnAttackingCreatureTargetDuringDeclareAttackers() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card planeswalkerCard = new Card();
        planeswalkerCard.setName("Test Planeswalker");
        planeswalkerCard.setType(CardType.PLANESWALKER);
        planeswalkerCard.setLoyalty(3);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, planeswalkerCard);
        attacker.setAttacking(true);
        attacker.setAttackTarget(planeswalker.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        MisleadingSignpost signpost = new MisleadingSignpost();
        signpost.setName("Misleading Signpost");
        signpost.setType(CardType.ARTIFACT);
        signpost.setManaCost("{2}{U}");
        signpost.setKeywords(Set.of(Keyword.FLASH));
        harness.setHand(player1, List.of(signpost));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);

        harness.castArtifact(player1, 0);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenTargetTrigger.class);
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isEqualTo(new PermanentChoiceContext.ReselectAttackingCreatureTarget(attacker.getId()));

        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.DECLARE_BLOCKERS));
        gd.playerAutoStopSteps.put(player2.getId(), Set.of(TurnStep.DECLARE_BLOCKERS));
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(attacker.getAttackTarget()).isEqualTo(player1.getId());
    }
}
