package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.h.HyenaPack;
import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.f.Feed;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MouthFeed.class, Feed.class, Forest.class, DuneBeetle.class, HyenaPack.class, Colossapede.class})
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
        harness.addToBattlefield(player1, new HyenaPack()); // 3/4
        harness.addToBattlefield(player1, new Colossapede()); // 5/5
        harness.addToBattlefield(player1, new DuneBeetle()); // 1/4 — ignored
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

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
        harness.addToBattlefield(player1, new DuneBeetle());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.setGraveyard(player1, List.of(new MouthFeed()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mouth"));
    }

    @Test
    @DisplayName("Feed counts current power at resolution and ignores opponents' creatures")
    void feedCountsCurrentPowerAtResolution() {
        Permanent beetle = harness.addToBattlefieldAndReturn(player1, new DuneBeetle());
        Permanent hyena = harness.addToBattlefieldAndReturn(player1, new HyenaPack());
        harness.addToBattlefield(player2, new Colossapede());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setGraveyard(player1, List.of(new MouthFeed()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castFlashback(player1, 0);
        beetle.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 2);
        hyena.getCounters().put(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Feed counts the Hippo created by Mouth from the same card")
    void mouthThenFeedDrawsForItsHippo() {
        MouthFeed spell = new MouthFeed();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        harness.assertOnBattlefield(player1, "Hippo");
    }

    @Test
    @DisplayName("Feed requires sorcery timing")
    void feedRequiresSorceryTiming() {
        harness.setGraveyard(player1, List.of(new MouthFeed()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
        harness.assertInGraveyard(player1, "Mouth");
    }

    @Test
    @DisplayName("Feed cannot be cast for Mouth's cheaper mana cost")
    void feedRequiresFourMana() {
        harness.setGraveyard(player1, List.of(new MouthFeed()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Mouth");
        assertThat(gd.stack).isEmpty();
    }
}
