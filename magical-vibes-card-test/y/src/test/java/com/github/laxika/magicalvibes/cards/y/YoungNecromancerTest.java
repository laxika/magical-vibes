package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.b.BoneShards;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YoungNecromancer.class, GrizzlyBears.class, Island.class, OrnithopterOfParadise.class, BoneShards.class})
class YoungNecromancerTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability exiles two cards and returns a targeted creature")
    void exilesTwoCardsAndReanimatesCreature() {
        Card target = new GrizzlyBears();
        Card first = new Island();
        Card second = new Island();
        harness.setGraveyard(player1, List.of(target, first, second));

        castYoungNecromancer();
        harness.handleMayAbilityChosen(player1, true);
        exileFromGraveyard(first, second);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Declining the ETB ability does not exile or return cards")
    void decliningDoesNothing() {
        Card target = new GrizzlyBears();
        Card first = new Island();
        Card second = new Island();
        harness.setGraveyard(player1, List.of(target, first, second));

        castYoungNecromancer();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target, first, second);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
    }

    @Test
    @DisplayName("With fewer than two cards in the graveyard no exile is offered")
    void fewerThanTwoCardsDoNothing() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        castYoungNecromancer();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
    }

    @Test
    @DisplayName("Exactly two cards can be exiled even when no creature remains to return")
    void exactlyTwoCardsAreExiledWithoutRemainingTarget() {
        Card first = new OrnithopterOfParadise();
        Card second = new OrnithopterOfParadise();
        harness.setGraveyard(player1, List.of(first, second));

        castYoungNecromancer();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Ornithopter of Paradise");
    }

    @Test
    @DisplayName("The creature target is chosen after exile and returns after a separate priority round")
    void choosesRemainingOwnCreatureAfterExileAndUsesSeparateTrigger() {
        Card target = new OrnithopterOfParadise();
        Card first = new OrnithopterOfParadise();
        Card second = new OrnithopterOfParadise();
        Card opposingCreature = new OrnithopterOfParadise();
        Card noncreature = new BoneShards();
        harness.setGraveyard(player1, List.of(target, first, second, noncreature));
        harness.setGraveyard(player2, List.of(opposingCreature));

        castYoungNecromancer();
        harness.handleMayAbilityChosen(player1, true);
        exileFromGraveyard(first, second);

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(target.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target, noncreature);
        harness.assertNotOnBattlefield(player1, "Ornithopter of Paradise");

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCreature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        harness.assertNotOnBattlefield(player2, "Ornithopter of Paradise");
    }

    @Test
    @DisplayName("A creature leaving the graveyard before the reflexive trigger resolves is not returned")
    void targetLeavingGraveyardDoesNotUndoExileOrReturnAnotherCreature() {
        Card target = new OrnithopterOfParadise();
        Card otherCreature = new OrnithopterOfParadise();
        Card first = new OrnithopterOfParadise();
        Card second = new OrnithopterOfParadise();
        harness.setGraveyard(player1, List.of(target, otherCreature, first, second));

        castYoungNecromancer();
        harness.handleMayAbilityChosen(player1, true);
        exileFromGraveyard(first, second);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(otherCreature));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second, target);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherCreature);
        harness.assertNotOnBattlefield(player1, "Ornithopter of Paradise");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With an empty graveyard no exile is offered")
    void emptyGraveyardDoesNothing() {
        harness.setGraveyard(player1, List.of());

        castYoungNecromancer();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With more than two cards, the player chooses which two to exile, one at a time")
    void choosesWhichTwoCardsToExile() {
        Card target = new GrizzlyBears();
        Card first = new Island();
        Card second = new Island();
        harness.setGraveyard(player1, List.of(target, first, second));

        castYoungNecromancer();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0, 1, 2);
        harness.handleGraveyardCardChosen(player1, 1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first);
    }

    private void exileFromGraveyard(Card... cards) {
        for (Card card : cards) {
            harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(card));
        }
    }

    private void castYoungNecromancer() {
        harness.castFromHand(player1, new YoungNecromancer(), "{4}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
