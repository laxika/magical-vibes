package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VarinaLichQueen.class, Gravecrawler.class, GrizzlyBears.class, Forest.class, Island.class,
        Mountain.class})
class VarinaLichQueenTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking Zombies draws and discards that many cards, then gains that much life")
    void attacksWithZombiesDrawsDiscardsAndGainsLife() {
        addCreatureReady(player1, new VarinaLichQueen());
        addCreatureReady(player1, new Gravecrawler());
        addCreatureReady(player1, new Gravecrawler());
        addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new Forest(), new Island()));

        declareAttackers(List.of(1, 2, 3));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)
                .remainingCount()).isEqualTo(2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Attacking without a Zombie does not trigger Varina")
    void nonZombieAttackDoesNotTrigger() {
        addCreatureReady(player1, new VarinaLichQueen());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(1));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Exiling two graveyard cards creates a tapped black Zombie token")
    void exilesTwoGraveyardCardsForZombieToken() {
        harness.addToBattlefield(player1, new VarinaLichQueen());
        harness.setGraveyard(player1, List.of(new Forest(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        Permanent zombie = findPermanents(player1, "Zombie").getFirst();
        assertThat(zombie.isTapped()).isTrue();
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
        assertThat(zombie.getEffectivePower()).isEqualTo(2);
        assertThat(zombie.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Varina cannot exile fewer than two cards from the graveyard")
    void requiresTwoGraveyardCards() {
        harness.addToBattlefield(player1, new VarinaLichQueen());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }
}
