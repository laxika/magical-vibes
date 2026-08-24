package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.v.VenomLethalProtector;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EddieBrock.class, VenomLethalProtector.class, LlanowarElves.class,
        GrizzlyBears.class, MindStone.class, Opt.class})
class EddieBrockTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a targeted creature with mana value one or less on entry")
    void returnsSmallCreatureFromGraveyardOnEntry() {
        LlanowarElves elves = new LlanowarElves();
        GrizzlyBears tooExpensive = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(elves, tooExpensive));
        castEddieBrock();

        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(elves.getId());
        harness.handleMultipleCardsChosen(player1, List.of(elves.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Transforms at sorcery speed")
    void transformsAtSorcerySpeed() {
        Permanent eddie = addFrontReady(player1);
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(eddie.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("May sacrifice another creature, draw its mana value, and put a matching permanent onto the battlefield")
    void sacrificesDrawsAndPutsPermanent() {
        addBackReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MindStone()));
        harness.setLibrary(player1, List.of(new Opt(), new Opt()));

        declareAttackers();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Mind Stone");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Opt", "Opt");
    }

    @Test
    @DisplayName("Declining the attack sacrifice leaves the battlefield and library unchanged")
    void declinesAttackSacrifice() {
        addBackReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MindStone()));
        harness.setLibrary(player1, List.of(new Opt(), new Opt()));

        declareAttackers();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInHand(player1, "Mind Stone");
    }

    private void castEddieBrock() {
        prepareMainPhase();
        harness.setHand(player1, List.of(new EddieBrock()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addFrontReady(Player player) {
        return addCreatureReady(player, new EddieBrock());
    }

    private Permanent addBackReady(Player player) {
        EddieBrock card = new EddieBrock();
        Permanent permanent = addCreatureReady(player, card);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        return permanent;
    }

    private void declareAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
