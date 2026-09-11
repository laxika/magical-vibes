package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Saw.class, GrizzlyBears.class})
class SawTest extends BaseCardTest {

    @Test
    @DisplayName("Equip attaches Saw and gives the creature +2/+0")
    void equipBoostsCreature() {
        Permanent saw = harness.addToBattlefieldAndReturn(player1, new Saw());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent creature = findPermanent(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(saw.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Attack trigger offers only an eligible permanent and draws after sacrificing it")
    void attackTriggerSacrificesOtherPermanentAndDraws() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent currentHost = addCreatureReady(player1, new GrizzlyBears());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent saw = harness.addToBattlefieldAndReturn(player1, new Saw());
        saw.setAttachedTo(attacker.getId());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        saw.setAttachedTo(currentHost.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(sacrifice.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the attack trigger does not sacrifice or draw")
    void decliningAttackTriggerDoesNothing() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent saw = harness.addToBattlefieldAndReturn(player1, new Saw());
        saw.setAttachedTo(attacker.getId());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .doesNotContain("Grizzly Bears");
    }
}
