package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DramaticFinaleTest extends BaseCardTest {

    @Test
    @DisplayName("Gives own creature tokens +1/+1 and does not boost nontoken creatures")
    void boostsOwnCreatureTokensOnly() {
        harness.addToBattlefield(player1, new DramaticFinale());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, tokenCreature("Soldier Token", 1, 1));
        harness.addToBattlefield(player2, tokenCreature("Goblin Token", 1, 1));

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent ownToken = findPermanent(player1, "Soldier Token");
        Permanent opponentToken = findPermanent(player2, "Goblin Token");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownToken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownToken)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentToken)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentToken)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates a flying Inkling when one or more own nontoken creatures die")
    void createsInklingWhenOwnNontokenCreatureDies() {
        harness.addToBattlefield(player1, new DramaticFinale());
        harness.addToBattlefield(player1, new GrizzlyBears());

        destroyCreature(player1, "Grizzly Bears");

        Permanent inkling = findPermanent(player1, "Inkling");
        assertThat(inkling.getCard().isToken()).isTrue();
        assertThat(inkling.getCard().getPower()).isEqualTo(2);
        assertThat(inkling.getCard().getToughness()).isEqualTo(1);
        assertThat(inkling.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(inkling.getCard().getSubtypes()).contains(CardSubtype.INKLING);
        assertThat(gqs.getEffectivePower(gd, inkling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, inkling)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, inkling, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Triggers only once each turn and ignores token and opposing creature deaths")
    void triggersOnlyOnceEachTurnForOwnNontokenDeaths() {
        harness.addToBattlefield(player1, new DramaticFinale());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card tokenBear = new GrizzlyBears();
        tokenBear.setToken(true);
        harness.addToBattlefield(player1, tokenBear);

        destroyCreature(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Inkling")).isEqualTo(1);

        destroyCreature(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Inkling")).isEqualTo(1);

        destroyCreature(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Inkling")).isEqualTo(1);

        destroyCreature(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Inkling")).isEqualTo(1);
    }

    private void destroyCreature(com.github.laxika.magicalvibes.model.Player player, String name) {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID creatureId = gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> name.equals(permanent.getCard().getName()))
                .map(Permanent::getId)
                .findFirst()
                .orElseThrow();
        harness.castInstant(player2, 0, creatureId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Card tokenCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(power);
        card.setToughness(toughness);
        card.setToken(true);
        return card;
    }
}
