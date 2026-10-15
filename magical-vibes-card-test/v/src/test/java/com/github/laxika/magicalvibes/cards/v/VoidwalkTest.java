package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.ArmoredTransport;
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

@CardUsed({Voidwalk.class, ArmoredTransport.class})
class VoidwalkTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target creature and returns it under its owner's control at the next end step")
    void exilesTargetCreatureUntilNextEndStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredTransport());
        harness.setHand(player1, List.of(new Voidwalk()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player2, "Armored Transport");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Armored Transport"));
        harness.assertInGraveyard(player1, "Voidwalk");

        advanceToEndStep();

        harness.assertOnBattlefield(player2, "Armored Transport");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Armored Transport"));
    }

    @Test
    @DisplayName("Can encode the spell on a creature you control")
    void canEncodeSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredTransport());
        Permanent encoder = addCreatureReady(player1, new ArmoredTransport());
        harness.setHand(player1, List.of(new Voidwalk()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, encoder.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Voidwalk"));
        harness.assertNotInGraveyard(player1, "Voidwalk");
    }

    @Test
    void cipherCopyExilesANewTargetWithoutManaOrAnotherEncodingChoice() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new ArmoredTransport());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player2, new ArmoredTransport());
        copyTarget.tap();
        Permanent encoder = addCreatureReady(player1, new ArmoredTransport());
        harness.setHand(player1, List.of(new Voidwalk()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(originalTarget.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, encoder.getId());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Armored Transport");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(card -> card.getName().equals("Voidwalk")).hasSize(1);
        harness.assertNotInGraveyard(player1, "Voidwalk");

        advanceToEndStep();

        assertThat(countPermanents(player2, "Armored Transport")).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Armored Transport");
    }

    @Test
    void combatDamageCopyCanBeDeclined() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredTransport());
        Permanent encoder = addCreatureReady(player1, new ArmoredTransport());
        harness.setHand(player1, List.of(new Voidwalk()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, encoder.getId());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(card -> card.getName().equals("Voidwalk")).hasSize(1);
        harness.assertOnBattlefield(player1, "Armored Transport");
        harness.assertNotInGraveyard(player1, "Voidwalk");
    }

    @Test
    void exilingYourOnlyCreatureLeavesNoCreatureToEncodeOn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArmoredTransport());
        harness.setHand(player1, List.of(new Voidwalk()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Armored Transport");
        harness.assertInGraveyard(player1, "Voidwalk");

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Armored Transport");
    }

    @Test
    void illegalTargetPreventsExileAndEncoding() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredTransport());
        addCreatureReady(player1, new ArmoredTransport());
        harness.setHand(player1, List.of(new Voidwalk()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, List.of(target.getId()));
        harness.getPermanentRemovalService().removePermanentToExile(gd, target);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Voidwalk");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        advanceToEndStep();

        harness.assertNotOnBattlefield(player2, "Armored Transport");
    }

    @Test
    void stolenCreatureReturnsUnderItsOwnersControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredTransport());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        harness.setHand(player1, List.of(new Voidwalk()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Armored Transport");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));

        advanceToEndStep();

        harness.assertOnBattlefield(player2, "Armored Transport");
        harness.assertNotOnBattlefield(player1, "Armored Transport");
        assertThat(findPermanent(player2, "Armored Transport").isTapped()).isFalse();
    }

    private void advanceToEndStep() {
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
