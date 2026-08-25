package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AdelineResplendentCathar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({HallowedRespite.class, GrizzlyBears.class, AdelineResplendentCathar.class})
class HallowedRespiteTest extends BaseCardTest {

    @Test
    @DisplayName("Returns your creature with a +1/+1 counter")
    void returnsOwnCreatureWithCounter() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castFromHand(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returned.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Returns an opponent's creature tapped")
    void returnsOpponentsCreatureTapped() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castFromHand(player1, bearsId);

        Permanent returned = findPermanent(player2, "Grizzly Bears");
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
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new HallowedRespite()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castFlashback(player1, 0, List.of(bearsId));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Hallowed Respite");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Hallowed Respite"));
    }

    private void castFromHand(Player player, UUID targetId) {
        harness.setHand(player, List.of(new HallowedRespite()));
        addSpellMana(player);
        harness.castSorcery(player, 0, List.of(targetId));
        harness.passBothPriorities();
    }

    private void addSpellMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
    }
}
