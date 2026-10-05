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
        gd.makeCommander(player1.getId(), commander.getOriginalCard());

        GrizzlyBears spell = new GrizzlyBears();
        harness.setExile(player1, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase();

        harness.castFromExile(player1, spell.getId());
        harness.handlePermanentChosen(player1, player2.getId());

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        resolveAllTriggers();

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
        harness.addMana(player1, ManaColor.GREEN, 2);
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
        gd.makeCommander(player1.getId(), commander.getOriginalCard());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase();

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void eachCommanderCreatureDealsDamageFromTheSameExiledSpell() {
        harness.addToBattlefield(player1, new PassionateArchaeologist());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.playerCommanders.put(player1.getId(), List.of(first.getOriginalCard(), second.getOriginalCard()));
        WalkingCorpse spell = new WalkingCorpse();
        harness.setExile(player1, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.BLACK, 2);
        prepareMainPhase();

        harness.castFromExile(player1, spell.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(gd.damageDealtThisTurnBySource.get(first.getId())).isEqualTo(2);
        assertThat(gd.damageDealtThisTurnBySource.get(second.getId())).isEqualTo(2);
    }

    @Test
    void ownedCommanderControlledByOpponentTriggersForItsController() {
        harness.addToBattlefield(player1, new PassionateArchaeologist());
        WalkingCorpse commanderCard = new WalkingCorpse();
        commanderCard.setOwnerId(player1.getId());
        Permanent commander = harness.addToBattlefieldAndReturn(player2, commanderCard);
        gd.makeCommander(player1.getId(), commanderCard);
        WalkingCorpse spell = new WalkingCorpse();
        harness.setExile(player2, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player2.getId());
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromExile(player2, spell.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.damageDealtThisTurnBySource.get(commander.getId())).isEqualTo(2);
    }

    @Test
    void opponentsCommanderYouControlDoesNotGainTheAbility() {
        harness.addToBattlefield(player1, new PassionateArchaeologist());
        WalkingCorpse commanderCard = new WalkingCorpse();
        commanderCard.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, commanderCard);
        gd.makeCommander(player2.getId(), commanderCard);
        WalkingCorpse spell = new WalkingCorpse();
        harness.setExile(player1, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.BLACK, 2);
        prepareMainPhase();

        harness.castFromExile(player1, spell.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertLife(player2, 20);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
