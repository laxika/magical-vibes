package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.k.KessDissidentMage;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VisionsOfDread.class, GrizzlyBears.class, HillGiant.class, EdgarMarkov.class, KessDissidentMage.class})
class VisionsOfDreadTest extends BaseCardTest {

    @Test
    @DisplayName("The targeted opponent chooses a creature to return under the caster's control")
    void targetedOpponentChoosesCreature() {
        GrizzlyBears firstCreature = new GrizzlyBears();
        HillGiant secondCreature = new HillGiant();
        harness.setGraveyard(player2, List.of(firstCreature, secondCreature));

        castNormally(player2.getId());

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.cardPool()).containsExactly(firstCreature, secondCreature);

        harness.handleGraveyardCardChosen(player2, 1);

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertNotInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The spell cannot target its controller")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new VisionsOfDread()));
        addNormalCastMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Flashback is reduced by the greatest owned commander")
    void flashbackUsesCommanderManaValue() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.get(player1.getId()).add(commander);
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setGraveyard(player1, List.of(new VisionsOfDread()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFlashback(player1, 0, player2.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Visions of Dread"));
    }

    @Test
    void emptyGraveyardDoesNothing() {
        harness.setGraveyard(player2, List.of());

        castNormally(player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Visions of Dread");
    }

    @Test
    void onlyCreatureCardsCanBeChosen() {
        VisionsOfDread noncreature = new VisionsOfDread();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(noncreature, creature));

        castNormally(player2.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Visions of Dread");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void flashbackWithoutCommanderRequiresFullCost() {
        harness.setGraveyard(player1, List.of(new VisionsOfDread()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castAndResolveFlashback(player1, 0, player2.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Visions of Dread"));
        harness.assertNotInGraveyard(player1, "Visions of Dread");
    }

    @Test
    void ownedCommanderUnderOpponentControlStillReducesFlashback() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player2, commander);
        harness.setGraveyard(player1, List.of(new VisionsOfDread()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFlashback(player1, 0, player2.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Visions of Dread"));
    }

    @Test
    void commanderInGraveyardDoesNotReduceFlashback() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        harness.setGraveyard(player1, List.of(new VisionsOfDread(), commander));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void castingThroughKessDoesNotReceiveFlashbackReduction() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.get(player1.getId()).add(commander);
        harness.addToBattlefield(player1, new KessDissidentMage());
        harness.setGraveyard(player1, List.of(new VisionsOfDread()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
    private void castNormally(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new VisionsOfDread()));
        addNormalCastMana();
        harness.castAndResolveSorcery(player1, 0, targetPlayerId);
    }

    private void addNormalCastMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
