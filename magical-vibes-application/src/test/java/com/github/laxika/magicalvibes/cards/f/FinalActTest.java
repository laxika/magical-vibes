package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FinalAct.class, GrizzlyBears.class, Shock.class, LlanowarElves.class})
class FinalActTest extends BaseCardTest {

    @Test
    @DisplayName("Selected creature and graveyard modes resolve together")
    void resolvesCreatureAndGraveyardModes() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card ownCard = new Shock();
        Card opponentCard = new LlanowarElves();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));

        cast(new int[]{0, 3});

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Final Act");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentCard);
    }

    @Test
    @DisplayName("Planeswalker and battle modes destroy their permanent types")
    void destroysPlaneswalkersAndBattles() {
        Card planeswalkerCard = card("Test Planeswalker", CardType.PLANESWALKER);
        planeswalkerCard.setLoyalty(4);
        Card battleCard = card("Test Battle", CardType.BATTLE);
        battleCard.setDefense(5);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, planeswalkerCard);
        Permanent battle = harness.addToBattlefieldAndReturn(player2, battleCard);

        cast(new int[]{1, 2});

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(planeswalker.getId())
                        || permanent.getId().equals(battle.getId()));
    }

    @Test
    @DisplayName("Counter mode clears every tracked counter from opponents only")
    void clearsOpponentCountersOnly() {
        gd.playerPoisonCounters.put(player1.getId(), 1);
        gd.playerEnergyCounters.put(player1.getId(), 2);
        gd.playerSparkCounters.put(player1.getId(), 3);
        gd.playerExperienceCounters.put(player1.getId(), 4);
        gd.playerPoisonCounters.put(player2.getId(), 5);
        gd.playerEnergyCounters.put(player2.getId(), 6);
        gd.playerSparkCounters.put(player2.getId(), 7);
        gd.playerExperienceCounters.put(player2.getId(), 8);

        cast(new int[]{4});

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerSparkCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerExperienceCounters.get(player1.getId())).isEqualTo(4);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.playerSparkCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.playerExperienceCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    private void cast(int[] modes) {
        harness.setHand(player1, List.of(new FinalAct()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castModalSorceryWithModes(player1, 0, 1, 5, modes, List.of(), null);
        harness.passBothPriorities();
    }

    private static Card card(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }
}
