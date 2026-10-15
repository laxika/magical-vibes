package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cryptbreaker.class, GrizzlyBears.class, Mountain.class, ScatheZombies.class})
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

    @Test
    void summoningSickZombiesIncludingCryptbreakerCanPayDrawCost() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Cryptbreaker());
        Permanent zombie1 = harness.addToBattlefieldAndReturn(player1, new Cryptbreaker());
        Permanent zombie2 = harness.addToBattlefieldAndReturn(player1, new Cryptbreaker());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(List.of(source, zombie1, zombie2)).allMatch(Permanent::isTapped);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Mountain");
        harness.assertLife(player1, 19);
    }

    @Test
    void tappedCryptbreakerCanActivateDrawAbilityUsingOtherZombies() {
        Permanent source = addCreatureReady(player1, new Cryptbreaker());
        source.tap();
        Permanent zombie1 = addCreatureReady(player1, new Cryptbreaker());
        Permanent zombie2 = addCreatureReady(player1, new Cryptbreaker());
        Permanent zombie3 = addCreatureReady(player1, new Cryptbreaker());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(List.of(source, zombie1, zombie2, zombie3)).allMatch(Permanent::isTapped);
        harness.assertInHand(player1, "Mountain");
        harness.assertLife(player1, 19);
    }

    @Test
    void tappedZombiesDoNotCountTowardDrawCost() {
        addCreatureReady(player1, new Cryptbreaker());
        addCreatureReady(player1, new Cryptbreaker());
        addCreatureReady(player1, new Cryptbreaker()).tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsZombiesDoNotCountTowardDrawCost() {
        addCreatureReady(player1, new Cryptbreaker());
        addCreatureReady(player1, new Cryptbreaker());
        addCreatureReady(player2, new Cryptbreaker());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tokenAbilityRequiresCardToDiscard() {
        addCreatureReady(player1, new Cryptbreaker());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void summoningSickCryptbreakerCannotActivateTokenAbility() {
        harness.addToBattlefield(player1, new Cryptbreaker());
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tokenAbilityPaysCostsBeforeCreatingToken() {
        Permanent source = addCreatureReady(player1, new Cryptbreaker());
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(source.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        Permanent token = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isSummoningSick()).isTrue();
    }

    @Test
    void tokenAbilityRequiresBlackMana() {
        addCreatureReady(player1, new Cryptbreaker());
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tappedCryptbreakerCannotActivateTokenAbility() {
        addCreatureReady(player1, new Cryptbreaker()).tap();
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
