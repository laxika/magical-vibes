package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkkiEmberKeeper.class, GrizzlyBears.class, Murder.class})
class AkkiEmberKeeperTest extends BaseCardTest {

    @Test
    @DisplayName("A modified nontoken creature dying creates a colorless Spirit")
    void modifiedNontokenCreatureDyingCreatesSpirit() {
        harness.addToBattlefield(player1, new AkkiEmberKeeper());
        var modifiedBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        modifiedBears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        killWithMurder(player2, modifiedBears.getId());

        var spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(1);
        assertThat(spirits.getFirst().getCard().getColor()).isNull();
        assertThat(spirits.getFirst().getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
    }

    @Test
    @DisplayName("Akki Ember-Keeper dying while modified creates a colorless Spirit")
    void modifiedAkkiEmberKeeperDyingCreatesSpirit() {
        var keeper = harness.addToBattlefieldAndReturn(player1, new AkkiEmberKeeper());
        keeper.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        killWithMurder(player2, keeper.getId());

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("An unmodified nontoken creature dying does not create a Spirit")
    void unmodifiedNontokenCreatureDyingDoesNotCreateSpirit() {
        harness.addToBattlefield(player1, new AkkiEmberKeeper());
        var bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killWithMurder(player2, bears.getId());

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A token creature dying does not create a Spirit")
    void tokenCreatureDyingDoesNotCreateSpirit() {
        harness.addToBattlefield(player1, new AkkiEmberKeeper());
        Card token = new Card();
        token.setName("Bear Token");
        token.setType(CardType.CREATURE);
        token.setManaCost("");
        token.setToken(true);
        token.setColor(CardColor.GREEN);
        token.setPower(2);
        token.setToughness(2);
        token.setSubtypes(List.of(CardSubtype.BEAR));
        harness.addToBattlefield(player1, token);

        killWithMurder(player2, harness.getPermanentId(player1, "Bear Token"));

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's modified creature dying does not create a Spirit")
    void opponentsModifiedCreatureDyingDoesNotCreateSpirit() {
        harness.addToBattlefield(player1, new AkkiEmberKeeper());
        var modifiedBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        modifiedBears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        killWithMurder(player1, modifiedBears.getId());

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void killWithMurder(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
