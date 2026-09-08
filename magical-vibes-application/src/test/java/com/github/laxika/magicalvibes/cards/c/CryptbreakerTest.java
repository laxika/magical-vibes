package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CryptbreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card creates a 2/2 black Zombie and taps Cryptbreaker")
    void discardCardCreatesZombie() {
        Permanent cryptbreaker = addCreatureReady(player1, new Cryptbreaker());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(cryptbreaker.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("Tapping three Zombies draws a card and loses 1 life")
    void tappingThreeZombiesDrawsAndLosesLife() {
        Permanent cryptbreaker = addCreatureReady(player1, new Cryptbreaker());
        Permanent zombie1 = addCreatureReady(player1, new ScatheZombies());
        Permanent zombie2 = addCreatureReady(player1, new ScatheZombies());
        Permanent zombie3 = addCreatureReady(player1, new ScatheZombies());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, zombie1.getId());
        harness.handlePermanentChosen(player1, zombie2.getId());
        harness.handlePermanentChosen(player1, zombie3.getId());
        harness.passBothPriorities();

        assertThat(cryptbreaker.isTapped()).isFalse();
        assertThat(zombie1.isTapped()).isTrue();
        assertThat(zombie2.isTapped()).isTrue();
        assertThat(zombie3.isTapped()).isTrue();
        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(Mountain.class);
    }

    @Test
    @DisplayName("The Zombie-drawing ability requires three Zombies")
    void requiresThreeZombies() {
        addCreatureReady(player1, new Cryptbreaker());
        addCreatureReady(player1, new ScatheZombies());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
