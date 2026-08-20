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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private void castRiseOfExtus(Permanent target) {
        harness.setHand(player1, List.of(new RiseOfExtus()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castSorcery(player1, 0, List.of(target.getId()));
    }
}
