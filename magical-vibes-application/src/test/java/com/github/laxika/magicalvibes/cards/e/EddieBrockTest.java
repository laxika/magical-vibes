package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AlexiosDeimosOfKosmos;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EddieBrock.class, VenomLethalProtector.class, LlanowarElves.class,
        GrizzlyBears.class, MindStone.class, Opt.class, AlexiosDeimosOfKosmos.class})
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

        declareAttackers(List.of(0));
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

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInHand(player1, "Mind Stone");
    }

    @Test
    @DisplayName("Sacrificing is allowed without putting a permanent from hand")
    void declinesPuttingPermanentAfterDrawing() {
        addBackReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MindStone()));
        harness.setLibrary(player1, List.of(new Opt(), new Opt()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Mind Stone");
        harness.assertNotOnBattlefield(player1, "Mind Stone");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Venom cannot sacrifice itself or an opposing creature")
    void noOtherControlledCreatureMeansNoDraw() {
        addBackReady(player1);
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Opt(), new Opt()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Venom, Lethal Protector");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A creature that cannot be sacrificed is excluded from Venom's choices")
    void excludesCreaturesThatCannotBeSacrificed() {
        addBackReady(player1);
        Permanent alexios = addCreatureReady(player1, new AlexiosDeimosOfKosmos());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MindStone()));
        harness.setLibrary(player1, List.of(new Opt(), new Opt()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        if (choice != null) {
            harness.handlePermanentChosen(player1, bears.getId());
        }
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Alexios, Deimos of Kosmos");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        if (choice != null) {
            assertThat(choice.validPermanentIds()).doesNotContain(alexios.getId());
        }
    }

    @Test
    @DisplayName("Transformation cannot be activated during combat")
    void rejectsTransformationDuringCombat() {
        Permanent eddie = addFrontReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(eddie.isTransformed()).isFalse();
    }

    private void castEddieBrock() {
        prepareMainPhase();
        harness.castFromHand(player1, new EddieBrock(), "{2}{B}");
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

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
