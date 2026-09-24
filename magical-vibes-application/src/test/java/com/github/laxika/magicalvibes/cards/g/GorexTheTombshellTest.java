package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GorexTheTombshell.class, GrizzlyBears.class, Shock.class})
class GorexTheTombshellTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles any number of creature cards and tracks them with Gorex")
    void exilesCreatureCardsAndTracksThem() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(first, second, shock));
        harness.setHand(player1, List.of(new GorexTheTombshell()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shock);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();

        harness.passBothPriorities();

        Permanent gorex = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.getCardsExiledByPermanent(gorex.getId()))
                .containsExactlyInAnyOrder(first, second);
    }

    @Test
    @DisplayName("Rejects a noncreature card selected for Gorex's additional cost")
    void rejectsNoncreatureAdditionalCostCard() {
        GrizzlyBears creature = new GrizzlyBears();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(creature, shock));
        harness.setHand(player1, List.of(new GorexTheTombshell()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("additional cost");

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature, shock);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(6);
    }

    @Test
    @DisplayName("Returns a random card exiled with Gorex when it attacks")
    void returnsExiledCardWhenItAttacks() {
        Card exiledCard = new GrizzlyBears();
        Permanent gorex = castGorex(exiledCard);
        gorex.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(exiledCard);
    }

    @Test
    @DisplayName("Returns a random card exiled with Gorex when it dies")
    void returnsExiledCardWhenItDies() {
        Card exiledCard = new GrizzlyBears();
        Permanent gorex = castGorex(exiledCard);
        Permanent dyingGorex = gorex;
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, dyingGorex));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(exiledCard);
    }

    private Permanent castGorex(Card exiledCard) {
        harness.setGraveyard(player1, List.of(exiledCard));
        harness.setHand(player1, List.of(new GorexTheTombshell()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0));
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Gorex, the Tombshell"))
                .findFirst()
                .orElseThrow();
    }
}
