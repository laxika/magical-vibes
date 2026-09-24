package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BaldinCenturyHerdmaster.class, GoblinPiker.class, GrizzlyBears.class})
class BaldinCenturyHerdmasterTest extends BaseCardTest {

    @Test
    void usesToughnessForCombatDamageOnlyDuringYourTurn() {
        addCreatureReady(player1, new BaldinCenturyHerdmaster());
        Permanent ownPiker = addCreatureReady(player1, new GoblinPiker());
        Permanent opposingPiker = addCreatureReady(player2, new GoblinPiker());

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectiveCombatDamage(gd, ownPiker)).isEqualTo(1);
        assertThat(gqs.getEffectiveCombatDamage(gd, opposingPiker)).isEqualTo(1);

        harness.forceActivePlayer(player2);
        assertThat(gqs.getEffectiveCombatDamage(gd, ownPiker)).isEqualTo(2);
        assertThat(gqs.getEffectiveCombatDamage(gd, opposingPiker)).isEqualTo(2);
    }

    @Test
    void attackTriggerBoostsEachChosenCreatureByCardsInHand() {
        Permanent baldin = addCreatureReady(player1, new BaldinCenturyHerdmaster());
        Permanent ownPiker = addCreatureReady(player1, new GoblinPiker());
        Permanent opposingPiker = addCreatureReady(player2, new GoblinPiker());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(baldin.getId(), ownPiker.getId(), opposingPiker.getId());

        harness.handlePermanentChosen(player1, ownPiker.getId());
        harness.handlePermanentChosen(player1, opposingPiker.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(ownPiker.getEffectivePower()).isEqualTo(2);
        assertThat(ownPiker.getEffectiveToughness()).isEqualTo(4);
        assertThat(opposingPiker.getEffectivePower()).isEqualTo(2);
        assertThat(opposingPiker.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void attackTriggerMayChooseNoTargets() {
        addCreatureReady(player1, new BaldinCenturyHerdmaster());
        Permanent piker = addCreatureReady(player1, new GoblinPiker());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(piker.getEffectivePower()).isEqualTo(2);
        assertThat(piker.getEffectiveToughness()).isEqualTo(1);
    }
}
