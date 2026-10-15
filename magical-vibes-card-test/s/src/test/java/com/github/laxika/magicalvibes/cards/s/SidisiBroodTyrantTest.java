package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SidisiBroodTyrant.class, AlpineGrizzly.class, Forest.class, ScoutTheBorders.class})
class SidisiBroodTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("When Sidisi enters, it mills three and creates one Zombie if a creature was milled")
    void entersMillsAndCreatesOneZombieForCreatureCards() {
        harness.setLibrary(player1, List.of(new AlpineGrizzly(), new AlpineGrizzly(), new Forest()));
        castSidisi();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(zombieTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Milling multiple creature cards in one event creates only one Zombie")
    void createsOnlyOneZombiePerMillEvent() {
        harness.setLibrary(player1, List.of(new AlpineGrizzly(), new AlpineGrizzly(), new AlpineGrizzly()));
        castSidisi();

        assertThat(zombieTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Milling no creature cards does not create a Zombie")
    void doesNotCreateZombieForNoncreatureCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        castSidisi();

        assertThat(zombieTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("Attacking Sidisi mills three and triggers the Zombie ability")
    void attackMillsAndCreatesZombie() {
        addCreatureReady(player1, new SidisiBroodTyrant());
        harness.setLibrary(player1, List.of(new AlpineGrizzly(), new Forest(), new Forest()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(zombieTokens(player1)).hasSize(1);
    }

    @Test
    void millsOnlyAvailableCardsFromShortLibrary() {
        AlpineGrizzly creature = new AlpineGrizzly();
        harness.setLibrary(player1, List.of(creature));

        castSidisi();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(zombieTokens(player1)).hasSize(1);
    }

    @Test
    void emptyLibraryCreatesNoZombie() {
        harness.setLibrary(player1, List.of());

        castSidisi();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(zombieTokens(player1)).isEmpty();
    }

    @Test
    void separateEnterAndAttackEventsEachCreateZombie() {
        harness.setLibrary(player1, List.of(new AlpineGrizzly(), new Forest(), new Forest(),
                new AlpineGrizzly(), new Forest(), new Forest()));
        castSidisi();
        findPermanent(player1, "Sidisi, Brood Tyrant").setSummoningSick(false);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThat(zombieTokens(player1)).hasSize(2);
    }

    @Test
    void opponentMillingOnlyTriggersTheirSidisi() {
        addCreatureReady(player1, new SidisiBroodTyrant());
        addCreatureReady(player2, new SidisiBroodTyrant());
        harness.setLibrary(player2, List.of(new AlpineGrizzly(), new Forest(), new Forest()));

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(zombieTokens(player1)).isEmpty();
        assertThat(zombieTokens(player2)).hasSize(1);
    }

    @Test
    void nonMillLibraryToGraveyardEventCreatesOnlyOneZombie() {
        harness.addToBattlefield(player1, new SidisiBroodTyrant());
        Forest selected = new Forest();
        AlpineGrizzly first = new AlpineGrizzly();
        AlpineGrizzly second = new AlpineGrizzly();
        harness.setLibrary(player1, List.of(selected, first, second, new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ScoutTheBorders(), "{2}{G}");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(selected);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second).hasSize(5);
        assertThat(zombieTokens(player1)).hasSize(1);
    }

    private void castSidisi() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SidisiBroodTyrant(), "{1}{B}{G}{U}");
        harness.passBothPriorities();
        resolveAllTriggers();
    }

    private List<Permanent> zombieTokens(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Zombie"))
                .toList();
    }
}
