package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EatToExtinction.class, GarrukWildspeaker.class, GrizzlyBears.class, Plains.class})
class EatToExtinctionTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target creature and then surveils 1")
    void exilesCreatureAndSurveils() {
        Permanent target = addCreature(player2);
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        castEatToExtinction(target);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card == target.getCard());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Can leave the card on top when surveilling 1")
    void canDeclineSurveil() {
        Permanent target = addCreature(player2);
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        castEatToExtinction(target);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Can exile a target planeswalker")
    void exilesPlaneswalker() {
        Permanent target = addPlaneswalker(player2);
        castEatToExtinction(target);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card == target.getCard());
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Rejects a land target")
    void rejectsLandTarget() {
        harness.addToBattlefield(player2, new Plains());
        Permanent land = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, java.util.List.of(new EatToExtinction()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent addPlaneswalker(Player player) {
        Permanent planeswalker = new Permanent(new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        planeswalker.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }

    private void castEatToExtinction(Permanent target) {
        harness.setHand(player1, java.util.List.of(new EatToExtinction()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
