package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DesperateRitual;
import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.h.HarshDeceiver;
import com.github.laxika.magicalvibes.cards.h.HondenOfSeeingWinds;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TellerOfTales.class, DesperateRitual.class, DevotedRetainer.class,
        HarshDeceiver.class, HondenOfSeeingWinds.class})
class TellerOfTalesTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Arcane spell lets the controller tap an untapped creature")
    void arcaneSpellTapsCreature() {
        addCreatureReady(player1, new TellerOfTales());
        Permanent creature = addCreatureReady(player2, new DevotedRetainer());

        harness.setHand(player1, List.of(new DesperateRitual()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, (UUID) null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a Spirit spell lets the controller untap a tapped creature")
    void spiritSpellUntapsCreature() {
        addCreatureReady(player1, new TellerOfTales());
        Permanent creature = addCreatureReady(player1, new DevotedRetainer());
        creature.tap();

        harness.setHand(player1, List.of(new HarshDeceiver()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the trigger taps nothing")
    void decliningDoesNothing() {
        addCreatureReady(player1, new TellerOfTales());
        Permanent creature = addCreatureReady(player2, new DevotedRetainer());

        harness.setHand(player1, List.of(new DesperateRitual()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, (UUID) null);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a non-Spirit non-Arcane spell does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        addCreatureReady(player1, new TellerOfTales());
        harness.setHand(player1, List.of(new DevotedRetainer()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The trigger can target creatures but not other permanents")
    void onlyCreaturesCanBeTargeted() {
        addCreatureReady(player1, new TellerOfTales());
        Permanent creature = addCreatureReady(player2, new DevotedRetainer());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new HondenOfSeeingWinds());

        harness.setHand(player1, List.of(new DesperateRitual()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, (UUID) null);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(creature.getId()).doesNotContain(noncreature.getId());
    }

    @Test
    @DisplayName("An opponent's Spirit or Arcane spell does not trigger")
    void opponentCastDoesNotTrigger() {
        addCreatureReady(player1, new TellerOfTales());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DesperateRitual()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, (UUID) null);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
