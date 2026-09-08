package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LegionsToAshesTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the target and same-name tokens controlled by its controller")
    void exilesTargetAndMatchingTokensOnly() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, token("Grizzly Bears"));
        harness.addToBattlefield(player2, token("Grizzly Bears"));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, token("Saproling"));
        harness.addToBattlefield(player1, token("Grizzly Bears"));
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        cast(targetId);

        assertThat(permanentsNamed(player2, "Grizzly Bears"))
                .hasSize(1)
                .allMatch(permanent -> !permanent.getCard().isToken());
        assertThat(permanentsNamed(player2, "Saproling")).hasSize(1);
        assertThat(permanentsNamed(player1, "Grizzly Bears")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        UUID landId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> cast(landId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent an opponent controls");
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by the caster")
    void cannotTargetOwnPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID ownPermanentId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> cast(ownPermanentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent an opponent controls");
    }

    private void cast(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new LegionsToAshes()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private List<Permanent> permanentsNamed(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> name.equals(permanent.getCard().getName()))
                .toList();
    }

    private Card token(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
