package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiverHeraldGuide.class, Forest.class, GrizzlyBears.class})
class RiverHeraldGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Exploring a land puts it into its controller's hand")
    void exploringLandPutsItIntoHand() {
        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);

        castRiverHeraldGuide();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(land.getId()));
        assertThat(findRiverHeraldGuide().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Exploring a nonland puts a +1/+1 counter on River Herald Guide")
    void exploringNonlandPutsCounterOnGuide() {
        Card nonland = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(nonland);

        castRiverHeraldGuide();

        assertThat(findRiverHeraldGuide().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining the nonland graveyard choice leaves the card on top")
    void decliningNonlandGraveyardChoiceLeavesCardOnTop() {
        Card nonland = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(nonland);

        castRiverHeraldGuide();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(nonland.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(nonland.getId()));
    }

    @Test
    @DisplayName("Accepting the nonland graveyard choice puts the card into the graveyard")
    void acceptingNonlandGraveyardChoicePutsCardIntoGraveyard() {
        Card nonland = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(nonland);

        castRiverHeraldGuide();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(nonland.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(nonland.getId()));
    }

    private void castRiverHeraldGuide() {
        harness.setHand(player1, List.of(new RiverHeraldGuide()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent findRiverHeraldGuide() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getClass() == RiverHeraldGuide.class)
                .findFirst()
                .orElseThrow();
    }
}
