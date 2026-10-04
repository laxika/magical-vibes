package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AdelineResplendentCathar;
import com.github.laxika.magicalvibes.cards.c.CandlegroveWitch;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HallowedRespite.class, CandlegroveWitch.class, AdelineResplendentCathar.class})
class HallowedRespiteTest extends BaseCardTest {

    @Test
    @DisplayName("Returns your creature with a +1/+1 counter")
    void returnsOwnCreatureWithCounter() {
        harness.addToBattlefield(player1, new CandlegroveWitch());
        castFromHand(player1, harness.getPermanentId(player1, "Candlegrove Witch"));

        Permanent returned = findPermanent(player1, "Candlegrove Witch");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returned.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Returns an opponent's creature tapped")
    void returnsOpponentsCreatureTapped() {
        harness.addToBattlefield(player2, new CandlegroveWitch());
        UUID bearsId = harness.getPermanentId(player2, "Candlegrove Witch");
        castFromHand(player1, bearsId);

        Permanent returned = findPermanent(player2, "Candlegrove Witch");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a legendary creature")
    void cannotTargetLegendaryCreature() {
        harness.addToBattlefield(player1, new AdelineResplendentCathar());
        harness.setHand(player1, List.of(new HallowedRespite()));
        addSpellMana(player1);

        UUID adelineId = harness.getPermanentId(player1, "Adeline, Resplendent Cathar");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(adelineId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback returns your creature with a +1/+1 counter and exiles the spell")
    void flashbackReturnsWithCounterAndExilesSpell() {
        harness.addToBattlefield(player1, new CandlegroveWitch());
        harness.setGraveyard(player1, List.of(new HallowedRespite()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearsId = harness.getPermanentId(player1, "Candlegrove Witch");
        harness.castAndResolveFlashback(player1, 0, bearsId);

        Permanent returned = findPermanent(player1, "Candlegrove Witch");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Hallowed Respite");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Hallowed Respite"));
    }

    @Test
    @DisplayName("A creature you control but do not own returns to its owner tapped")
    void borrowedCreatureReturnsToOpponentTapped() {
        Permanent borrowed = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        gd.stolenCreatures.put(borrowed.getId(), player2.getId());

        castFromHand(player1, borrowed.getId());

        harness.assertNotOnBattlefield(player1, "Candlegrove Witch");
        Permanent returned = findPermanent(player2, "Candlegrove Witch");
        assertThat(returned.getId()).isNotEqualTo(borrowed.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Your creature controlled by an opponent returns to you with a counter")
    void ownedCreatureReturnsFromOpponentWithCounter() {
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, new CandlegroveWitch());
        gd.stolenCreatures.put(stolen.getId(), player1.getId());

        castFromHand(player1, stolen.getId());

        harness.assertNotOnBattlefield(player2, "Candlegrove Witch");
        Permanent returned = findPermanent(player1, "Candlegrove Witch");
        assertThat(returned.getId()).isNotEqualTo(stolen.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Returning a creature clears old counters and tapped state before adding one counter")
    void returnClearsPreviousPermanentState() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        original.tap();

        castFromHand(player1, original.getId());

        Permanent returned = findPermanent(player1, "Candlegrove Witch");
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returned.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Hallowed Respite");
    }

    private void castFromHand(Player player, UUID targetId) {
        harness.setHand(player, List.of(new HallowedRespite()));
        addSpellMana(player);
        harness.castAndResolveSorcery(player, 0, List.of(targetId));
    }

    private void addSpellMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
    }
}
