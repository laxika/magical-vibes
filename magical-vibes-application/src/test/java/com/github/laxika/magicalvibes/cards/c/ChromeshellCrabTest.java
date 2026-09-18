package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvenEnvoy;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChromeshellCrab.class, FugitiveWizard.class, AvenEnvoy.class})
class ChromeshellCrabTest extends BaseCardTest {

    @Test
    void turningFaceUpMayExchangeControlOfOwnAndOpponentsCreature() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new AvenEnvoy());
        harness.setHand(player1, List.of(new ChromeshellCrab()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent crab = findPermanent(player1, "Chromeshell Crab");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crab));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(own.getId())
                .doesNotContain(opponent.getId());
        harness.handlePermanentChosen(player1, own.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(opponent.getId());
        harness.handlePermanentChosen(player1, opponent.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player2, "Fugitive Wizard");
        harness.assertOnBattlefield(player1, "Aven Envoy");
    }

    @Test
    void decliningExchangeLeavesBothCreaturesUnderTheirOriginalControllers() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new AvenEnvoy());
        harness.setHand(player1, List.of(new ChromeshellCrab()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent crab = findPermanent(player1, "Chromeshell Crab");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crab));
        harness.handlePermanentChosen(player1, own.getId());
        harness.handlePermanentChosen(player1, opponent.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Aven Envoy");
    }

    @Test
    void turningFaceUpWithoutAnOpponentCreatureDoesNotPutExchangeAbilityOnStack() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        harness.setHand(player1, List.of(new ChromeshellCrab()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent crab = findPermanent(player1, "Chromeshell Crab");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crab));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(own.getId());
        harness.handlePermanentChosen(player1, own.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(crab.isFaceDown()).isFalse();
        harness.assertOnBattlefield(player1, "Fugitive Wizard");
        harness.assertOnBattlefield(player1, "Chromeshell Crab");
    }

    @Test
    void exchangeDoesNothingIfAChosenCreatureLeavesBeforeResolution() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new AvenEnvoy());
        harness.setHand(player1, List.of(new ChromeshellCrab()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent crab = findPermanent(player1, "Chromeshell Crab");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crab));
        harness.handlePermanentChosen(player1, own.getId());
        harness.handlePermanentChosen(player1, opponent.getId());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, opponent));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Fugitive Wizard");
        harness.assertNotOnBattlefield(player1, "Aven Envoy");
        harness.assertNotOnBattlefield(player2, "Aven Envoy");
        harness.assertInGraveyard(player2, "Aven Envoy");
    }
}
