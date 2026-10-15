package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BurstLightning;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.AddManaAtNextMainPhase;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManaSculpt.class, BurstLightning.class, FugitiveWizard.class})
class ManaSculptTest extends BaseCardTest {

    @Test
    void countersKickedSpellAndAddsManaEqualToActualPaymentAtNextMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new FugitiveWizard());

        BurstLightning burstLightning = new BurstLightning();
        harness.setHand(player1, List.of(burstLightning));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setHand(player2, List.of(new ManaSculpt()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castKickedInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, burstLightning.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Burst Lightning");
        AddManaAtNextMainPhase reward = gd.getDelayedActions(AddManaAtNextMainPhase.class).getFirst();
        assertThat(reward.amount()).isEqualTo(5);
        assertThat(reward.color()).isEqualTo(ManaColor.COLORLESS);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(5);
    }

    @Test
    void countersSpellButDoesNotScheduleManaWithoutAWizard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        BurstLightning burstLightning = new BurstLightning();
        harness.setHand(player1, List.of(burstLightning));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new ManaSculpt()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, burstLightning.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Burst Lightning");
        assertThat(gd.getDelayedActions(AddManaAtNextMainPhase.class)).isEmpty();
    }

    @Test
    void opponentsWizardDoesNotEnableManaReward() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new FugitiveWizard());
        BurstLightning spell = new BurstLightning();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new ManaSculpt()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Burst Lightning");
        harness.assertLife(player2, 20);
        assertThat(gd.getDelayedActions(AddManaAtNextMainPhase.class)).isEmpty();
    }

    @Test
    void wizardMustStillBeControlledWhenManaSculptResolves() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        var wizard = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        BurstLightning spell = new BurstLightning();
        harness.setHand(player1, List.of(spell, new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new ManaSculpt()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, wizard.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Fugitive Wizard");
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        harness.assertLife(player2, 20);
        assertThat(gd.getDelayedActions(AddManaAtNextMainPhase.class)).isEmpty();
    }

    @Test
    void rewardRemainsAfterWizardDiesAndWaitsForControllersMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        var wizard = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        BurstLightning spell = new BurstLightning();
        harness.setHand(player1, List.of(spell, new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new ManaSculpt()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, wizard.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Fugitive Wizard");

        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.getDelayedActions(AddManaAtNextMainPhase.class)).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.getDelayedActions(AddManaAtNextMainPhase.class)).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
