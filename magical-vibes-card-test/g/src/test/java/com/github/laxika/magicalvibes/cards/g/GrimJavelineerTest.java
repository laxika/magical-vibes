package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrimJavelineer.class, GrizzlyBears.class, Shock.class, Unsummon.class})
class GrimJavelineerTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever you attack, an attacking creature gets +1/+0")
    void boostsTargetAttackingCreature() {
        Permanent javelineer = addCreatureReady(player1, new GrimJavelineer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        int originalPower = gqs.getEffectivePower(gd, attacker);

        declareAttackers(player1, List.of(0, 1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(javelineer.getId(), attacker.getId());

        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(originalPower + 1);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new GrimJavelineer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        int originalPower = gqs.getEffectivePower(gd, attacker);

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(originalPower);
    }

    @Test
    @DisplayName("Surveils 1 when the targeted creature dies later that turn")
    void surveilsWhenTargetDiesLaterThisTurn() {
        addCreatureReady(player1, new GrimJavelineer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, attacker.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Does not surveil if the targeted creature survives")
    void doesNotSurveilWhenTargetSurvives() {
        addCreatureReady(player1, new GrimJavelineer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        addCreatureReady(player1, new GrimJavelineer());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Triggers even when Grim Javelineer does not attack")
    void triggersWithoutSourceAttacking() {
        addCreatureReady(player1, new GrimJavelineer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        int originalPower = gqs.getEffectivePower(gd, attacker);

        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(originalPower + 1);
    }

    @Test
    @DisplayName("Surveil may leave the card on top of the library")
    void mayKeepSurveilledCard() {
        addCreatureReady(player1, new GrimJavelineer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, attacker.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("A returned and recast creature is not tracked by the old delayed trigger")
    void doesNotSurveilForReturnedAndRecastCreature() {
        addCreatureReady(player1, new GrimJavelineer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, attacker.getId());
        resolveAllTriggers();
        harness.assertInHand(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent returned = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, returned.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }
    @Test
    @DisplayName("No delayed surveil trigger is created if the target dies in response")
    void targetDiesBeforeAttackTriggerResolves() {
        addCreatureReady(player1, new GrimJavelineer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, attacker.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("The delayed trigger still surveils after Grim Javelineer dies")
    void delayedTriggerSurvivesSourceDeath() {
        Permanent javelineer = addCreatureReady(player1, new GrimJavelineer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, javelineer.getId());
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Grim Javelineer");
        harness.castInstant(player1, 0, attacker.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

}
