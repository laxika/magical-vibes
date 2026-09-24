package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RimewallProtector.class, FugitiveWizard.class, HillGiant.class,
        GrizzlyBears.class, Unsummon.class})
class RimewallProtectorTest extends BaseCardTest {

    @Test
    void givesPerpetualWardToOtherGiantsAndWizardsOnBattlefieldAndInHand() {
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        FugitiveWizard handWizard = new FugitiveWizard();
        GrizzlyBears handBears = new GrizzlyBears();

        harness.setHand(player1, List.of(new RimewallProtector(), handWizard, handBears));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, wizard, Keyword.WARD)).isTrue();
        assertThat(gqs.hasKeyword(gd, giant, Keyword.WARD)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.WARD)).isFalse();

        harness.setHand(player1, List.of(handWizard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Fugitive Wizard"), Keyword.WARD)).isTrue();

        harness.setHand(player1, List.of(handBears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.WARD)).isFalse();
    }

    @Test
    void wardPersistsWhenAMatchingPermanentLeavesAndReturns() {
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        harness.enterBattlefieldAndReturn(player1, new RimewallProtector());
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.WARD)).isTrue();

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, wizard.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent returnedWizard = findPermanent(player1, "Fugitive Wizard");
        assertThat(gqs.hasKeyword(gd, returnedWizard, Keyword.WARD)).isTrue();
    }

    @Test
    void grantedWardCountersAnOpponentsUnpaidSpell() {
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        harness.enterBattlefieldAndReturn(player1, new RimewallProtector());
        resolveAllTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, wizard.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Unsummon");
        harness.assertOnBattlefield(player1, "Fugitive Wizard");
    }
}
