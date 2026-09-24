package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PassionateArchaeologist.class, GrizzlyBears.class, WalkingCorpse.class})
class PassionateArchaeologistTest extends BaseCardTest {

    @Test
    void commanderDealsDamageEqualToManaValueWhenSpellIsCastFromExile() {
        harness.addToBattlefield(player1, new PassionateArchaeologist());
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        commander.setCommander(true);

        GrizzlyBears spell = new GrizzlyBears();
        harness.setExile(player1, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase();

        harness.castFromExile(player1, spell.getId());
        harness.handlePermanentChosen(player1, player2.getId());

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void doesNotTriggerForHandCastOrNonCommanderCreature() {
        harness.addToBattlefield(player1, new PassionateArchaeologist());
        harness.addToBattlefield(player1, new WalkingCorpse());

        GrizzlyBears exiledSpell = new GrizzlyBears();
        harness.setExile(player1, List.of(exiledSpell));
        gd.exilePlayPermissions.put(exiledSpell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase();

        harness.castFromExile(player1, exiledSpell.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();

        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void doesNotTriggerWhenCommanderCastsSpellFromHand() {
        harness.addToBattlefield(player1, new PassionateArchaeologist());
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        commander.setCommander(true);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase();

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
