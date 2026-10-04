package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({AshlingRekindled.class, AshlingRimebound.class, Forest.class, GrizzlyBears.class,
        HillGiant.class, LightningBolt.class, Fireball.class})
class AshlingRekindledTest extends BaseCardTest {

    @Test
    @DisplayName("Ashling rummages when it enters the battlefield")
    void rummagesOnEnter() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new AshlingRekindled(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .extracting(Card::getName).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Ashling transforms into Rimebound and creates its restricted mana")
    void transformsIntoRimebound() {
        Permanent ashling = addFrontFace(player1);

        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(ashling.isTransformed()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getManaValueAtLeastFourOnlyMana(ManaColor.RED))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Rimebound's mana only casts spells with mana value at least four")
    void restrictedManaRequiresManaValueAtLeastFour() {
        addBackFace(player1);
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.setHand(player1, List.of(new LightningBolt()));
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof HillGiant);
    }

    @Test
    @DisplayName("Rimebound transforms back into Rekindled after paying red")
    void transformsBackIntoRekindled() {
        Permanent ashling = addBackFace(player1);

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(ashling.isTransformed()).isFalse();
    }

    @Test
    void rimeboundHasTwoIndependentMainPhaseTriggers() {
        addBackFace(player1);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> advanceToPrecombatMain(player1));

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    void restrictedManaCountsChosenXInSpellManaValue() {
        addFrontFace(player1);
        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, 3, List.of(player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.playerManaPools.get(player1.getId()).getManaValueAtLeastFourOnlyManaTotal())
                .isZero();
    }

    @Test
    void mayDeclineRummageOnEnter() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new AshlingRekindled(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .extracting(Card::getName).isEqualTo("Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyHandCannotDrawFromRummage() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new AshlingRekindled(), "{1}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void mayDeclineTransformationWithBlueManaAvailable() {
        Permanent ashling = addFrontFace(player1);
        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(ashling.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentMainPhaseDoesNotTriggerFrontFace() {
        addFrontFace(player1);

        advanceToPrecombatMain(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentMainPhaseDoesNotTriggerBackFace() {
        addBackFace(player1);

        advanceToPrecombatMain(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rummagesAfterTransformingBackToFrontFace() {
        Permanent ashling = addBackFace(player1);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(ashling.isTransformed()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .extracting(Card::getName).isEqualTo("Forest");
    }

    private Permanent addFrontFace(Player player) {
        return harness.addToBattlefieldAndReturn(player, new AshlingRekindled());
    }

    private Permanent addBackFace(Player player) {
        AshlingRekindled card = new AshlingRekindled();
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        return permanent;
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
