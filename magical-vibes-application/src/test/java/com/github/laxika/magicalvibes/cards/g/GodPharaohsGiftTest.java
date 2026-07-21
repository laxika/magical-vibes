package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GodPharaohsGiftTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to BEGINNING_OF_COMBAT, triggers fire
    }

    private Permanent findToken(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("Triggers at beginning of combat on controller's turn and offers graveyard choice")
    void triggersOnControllersCombat() {
        harness.addToBattlefield(player1, new GodPharaohsGift());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }

    @Test
    @DisplayName("Does not trigger at beginning of combat on opponent's turn")
    void doesNotTriggerOnOpponentsCombat() {
        harness.addToBattlefield(player1, new GodPharaohsGift());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        advanceToCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }

    @Test
    @DisplayName("Exiling a creature creates a 4/4 black Zombie token copy with haste until end of turn")
    void acceptingCreatesFourFourBlackZombieWithHaste() {
        harness.addToBattlefield(player1, new GodPharaohsGift());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));
        UUID bearsId = bears.getId();

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bearsId));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(bearsId));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(bearsId));

        Permanent token = findToken(player1, "Grizzly Bears");
        assertThat(token.getCard().getPower()).isEqualTo(4);
        assertThat(token.getCard().getToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
        assertThat(token.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(token.getCard().getKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    @DisplayName("Declining the choice leaves the graveyard unchanged and creates no token")
    void decliningDoesNothing() {
        harness.addToBattlefield(player1, new GodPharaohsGift());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }

    @Test
    @DisplayName("Non-creature cards in the graveyard are not offered")
    void onlyCreatureCardsAreOffered() {
        harness.addToBattlefield(player1, new GodPharaohsGift());
        GrizzlyBears bears = new GrizzlyBears();
        Plains plains = new Plains();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears, plains)));

        advanceToCombat(player1);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(bears.getId());
    }
}
