package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.ForbiddenCrypt;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreadSummons.class, Forest.class, ForbiddenCrypt.class, GrizzlyBears.class})
class DreadSummonsTest extends BaseCardTest {

    @Test
    @DisplayName("Each player mills X and creates a tapped Zombie for each milled creature")
    void millsEachPlayerAndCreatesTappedZombieForEachMilledCreature() {
        Card ownCreature = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ownCreature, new Forest()));
        harness.setLibrary(player2, List.of(opponentCreature, new Forest()));

        castDreadSummons(2);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentCreature);

        List<Permanent> zombies = zombies(player1);
        assertThat(zombies).hasSize(2);
        assertThat(zombies).allSatisfy(zombie -> {
            assertThat(zombie.isTapped()).isTrue();
            assertThat(zombie.getEffectivePower()).isEqualTo(2);
            assertThat(zombie.getEffectiveToughness()).isEqualTo(2);
        });
        assertThat(zombies(player2)).isEmpty();
    }

    @Test
    @DisplayName("Only creature cards that actually enter graveyards count")
    void excludesNoncreaturesAndCardsReplacedFromEnteringGraveyards() {
        harness.addToBattlefield(player1, new ForbiddenCrypt());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest()));

        castDreadSummons(2);

        assertThat(zombies(player1)).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("X zero mills no cards and creates no Zombies")
    void zeroDoesNothing() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        castDreadSummons(0);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(zombies(player1)).isEmpty();
    }

    private void castDreadSummons(int xValue) {
        harness.setHand(player1, List.of(new DreadSummons()));
        harness.addMana(player1, ManaColor.BLACK, xValue + 2);
        harness.castAndResolveSorcery(player1, 0, xValue);
    }

    private List<Permanent> zombies(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Zombie"))
                .toList();
    }
}
