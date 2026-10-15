package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.EnvironmentalSciences;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiseOfExtus.class, EnvironmentalSciences.class, Forest.class, GrizzlyBears.class, HolyDay.class})
class RiseOfExtusTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature and an instant or sorcery, then learns")
    void exilesCreatureAndInstantOrSorceryThenLearns() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card instant = new HolyDay();
        Card nonSpell = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(instant, nonSpell)));
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));

        castRiseOfExtus(target);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(instant.getId());
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .contains(target.getCard(), instant);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(nonSpell);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).contains(lesson);
    }

    @Test
    @DisplayName("Still exiles the creature and learns when no instant or sorcery is available")
    void resolvesOptionalGraveyardExileWithNoMatchingCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card nonSpell = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(nonSpell));
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));

        castRiseOfExtus(target);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(nonSpell);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new RiseOfExtus()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May decline graveyard exile even when a legal target exists")
    void declinesOptionalGraveyardTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card instant = new HolyDay();
        harness.setGraveyard(player2, List.of(instant));
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));

        castRiseOfExtus(target);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(instant);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lesson);
    }

    @Test
    @DisplayName("Exiles a sorcery from your own graveyard and learns by discarding and drawing")
    void exilesOwnSorceryAndDiscardsToDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card sorcery = new EnvironmentalSciences();
        Card discarded = new GrizzlyBears();
        Card drawn = new Forest();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new RiseOfExtus(), discarded));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, List.of(target.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(sorcery);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Still learns and exiles the graveyard target when the creature target leaves")
    void resolvesWithOnlyGraveyardTargetStillLegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card instant = new HolyDay();
        harness.setGraveyard(player2, List.of(instant));
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));

        castRiseOfExtus(target);
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setHand(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(instant);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(target.getCard());
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lesson);
    }

    @Test
    @DisplayName("Still exiles the creature and learns when the graveyard target leaves")
    void resolvesWithOnlyCreatureTargetStillLegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card instant = new HolyDay();
        harness.setGraveyard(player2, List.of(instant));
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));

        castRiseOfExtus(target);
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(instant));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target.getCard());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(instant);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lesson);
    }

    @Test
    @DisplayName("Does not learn when its only chosen target is illegal")
    void doesNotLearnWhenAllTargetsAreIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card lesson = new EnvironmentalSciences();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(lesson)));

        castRiseOfExtus(target);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setHand(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(lesson);
        harness.assertInGraveyard(player1, "Rise of Extus");
    }

    private void castRiseOfExtus(Permanent target) {
        harness.setHand(player1, List.of(new RiseOfExtus()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castSorcery(player1, 0, List.of(target.getId()));
    }
}
