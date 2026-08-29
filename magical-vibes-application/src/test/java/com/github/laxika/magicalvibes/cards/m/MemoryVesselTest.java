package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MemoryVessel.class, Forest.class, GrizzlyBears.class})
class MemoryVesselTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles seven cards for each player, permits exile play, and blocks hands")
    void exilesCardsAndBlocksHands() {
        Card player1Land = new Forest();
        Card player1Spell = new GrizzlyBears();
        Card player2Land = new Forest();
        Card player2Spell = new GrizzlyBears();
        harness.setLibrary(player1, libraryWith(player1Land, player1Spell));
        harness.setLibrary(player2, libraryWith(player2Land, player2Spell));

        harness.addToBattlefield(player1, new MemoryVessel());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.findExiledCard(player1Land.getId())).isNotNull();
        assertThat(gd.findExiledCard(player1Spell.getId())).isNotNull();
        assertThat(gd.findExiledCard(player2Land.getId())).isNotNull();
        assertThat(gd.findExiledCard(player2Spell.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions)
                .containsEntry(player1Spell.getId(), player1.getId())
                .containsEntry(player2Spell.getId(), player2.getId());
        assertThat(gd.playersCantPlayCardsFromHandUntilControllerNextTurn.get(player1.getId()))
                .containsExactlyInAnyOrder(player1.getId(), player2.getId());

        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).isEmpty();
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castCreature(player1, 1))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player2.getId())).isEmpty();

        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castFromExile(player2, player2Spell.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == player2Spell);
    }

    @Test
    @DisplayName("Exile play and the hand restriction expire on the activating player's next turn")
    void permissionsExpireOnActivatingPlayersNextTurn() {
        Card player1Top = new GrizzlyBears();
        Card player2Top = new GrizzlyBears();
        harness.setLibrary(player1, libraryWith(player1Top, new GrizzlyBears()));
        harness.setLibrary(player2, libraryWith(player2Top, new GrizzlyBears()));

        harness.addToBattlefield(player1, new MemoryVessel());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passUntil(player1, TurnStep.UPKEEP);

        assertThat(gd.exilePlayPermissions)
                .doesNotContainKey(player1Top.getId())
                .doesNotContainKey(player2Top.getId());
        assertThat(gd.playersCantPlayCardsFromHandUntilControllerNextTurn)
                .doesNotContainKey(player1.getId());

        harness.setHand(player1, List.of(new Forest()));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("Can only be activated as a sorcery")
    void activationIsSorcerySpeed() {
        harness.addToBattlefield(player1, new MemoryVessel());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<Card> libraryWith(Card first, Card second) {
        List<Card> library = new ArrayList<>(List.of(first, second));
        for (int i = 0; i < 8; i++) {
            library.add(new GrizzlyBears());
        }
        return library;
    }
}
