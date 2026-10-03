package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SteadfastPaladin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Blur.class, GrizzlyBears.class, SteadfastPaladin.class})
class BlurTest extends BaseCardTest {

    @Test
    void flickersCreatureAndDrawsCard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Blur()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveInstant(player1, 0, bearsId);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(bearsId);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void returnedCreatureHasSummoningSickness() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Blur()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearsId);

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Blur()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentBearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsStolenCreatureToOwnerAndDrawsForCaster() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        gd.stolenCreatures.put(paladin.getId(), player2.getId());
        harness.setHand(player1, List.of(new Blur()));
        SteadfastPaladin draw = new SteadfastPaladin();
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        harness.castAndResolveInstant(player1, 0, paladin.getId());

        harness.assertNotOnBattlefield(player1, "Steadfast Paladin");
        harness.assertOnBattlefield(player2, "Steadfast Paladin");
        assertThat(harness.getPermanentId(player2, "Steadfast Paladin")).isNotEqualTo(paladin.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
    }

    @Test
    void doesNotDrawWhenTargetChangesControllerBeforeResolution() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        harness.setHand(player1, List.of(new Blur()));
        SteadfastPaladin draw = new SteadfastPaladin();
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, paladin.getId());
        gd.playerBattlefields.get(player1.getId()).remove(paladin);
        gd.playerBattlefields.get(player2.getId()).add(paladin);
        gd.stolenCreatures.put(paladin.getId(), player1.getId());
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player2, "Steadfast Paladin")).isEqualTo(paladin.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        harness.assertInGraveyard(player1, "Blur");
    }

    @Test
    void doesNotDrawWhenTargetLeavesBattlefieldBeforeResolution() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        harness.setHand(player1, List.of(new Blur()));
        SteadfastPaladin draw = new SteadfastPaladin();
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, paladin.getId());
        gd.playerBattlefields.get(player1.getId()).remove(paladin);
        harness.setGraveyard(player1, List.of(paladin.getCard()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Steadfast Paladin");
        harness.assertInGraveyard(player1, "Steadfast Paladin");
        harness.assertInGraveyard(player1, "Blur");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
    }

    @Test
    void tokenDoesNotReturnButCasterStillDraws() {
        SteadfastPaladin token = new SteadfastPaladin();
        token.setToken(true);
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, token);
        harness.setHand(player1, List.of(new Blur()));
        SteadfastPaladin draw = new SteadfastPaladin();
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, paladin.getId());

        harness.assertNotOnBattlefield(player1, "Steadfast Paladin");
        harness.assertNotOnBattlefield(player2, "Steadfast Paladin");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }

    @Test
    void returnedCreatureIsUntappedWithoutCountersOrMarkedDamage() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        paladin.tap();
        paladin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        paladin.setMarkedDamage(1);
        harness.setHand(player1, List.of(new Blur()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, paladin.getId());

        Permanent returned = findPermanent(player1, "Steadfast Paladin");
        assertThat(returned.getId()).isNotEqualTo(paladin.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(returned.getMarkedDamage()).isZero();
    }
}
