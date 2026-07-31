package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MouthFeedTest extends BaseCardTest {

    @Test
    @DisplayName("Mouth creates a 3/3 green Hippo token")
    void mouthCreatesHippoToken() {
        harness.setHand(player1, List.of(new MouthFeed()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent hippo = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Hippo"))
                .findFirst()
                .orElseThrow();
        assertThat(hippo.getEffectivePower()).isEqualTo(3);
        assertThat(hippo.getEffectiveToughness()).isEqualTo(3);
        assertThat(hippo.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(hippo.getCard().getSubtypes()).contains(CardSubtype.HIPPO);
        harness.assertInGraveyard(player1, "Mouth");
    }

    @Test
    @DisplayName("Feed draws one card per controlled creature with power 3+, then exiles")
    void feedDrawsPerPowerfulCreatureThenExiles() {
        harness.addToBattlefield(player1, new HillGiant()); // 3/3
        harness.addToBattlefield(player1, new SerraAngel()); // 4/4
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2 — ignored
        harness.setHand(player1, List.of());
        setDeck(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.setGraveyard(player1, List.of(new MouthFeed()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Mouth") || c.getName().equals("Feed"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mouth"));
    }

    @Test
    @DisplayName("Feed draws nothing when no creature has power 3 or greater")
    void feedDrawsNothingWithoutPowerfulCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        setDeck(player1, List.of(new Forest(), new Forest()));

        harness.setGraveyard(player1, List.of(new MouthFeed()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mouth"));
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
