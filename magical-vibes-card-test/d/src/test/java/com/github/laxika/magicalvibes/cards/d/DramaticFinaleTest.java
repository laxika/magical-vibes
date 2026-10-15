package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DramaticFinale.class, GrizzlyBears.class, Shock.class, WrathOfGod.class})
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

        destroyCreature(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        assertThat(countPermanents(player1, "Inkling")).isEqualTo(1);
    }

    @Test
    void tokenAndOpponentDeathsDoNotConsumeTheTrigger() {
        harness.addToBattlefield(player1, new DramaticFinale());
        harness.addToBattlefield(player1, tokenCreature("Soldier Token", 1, 1));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        destroyCreature(player1, "Soldier Token");
        harness.assertNotOnBattlefield(player1, "Soldier Token");
        assertThat(countPermanents(player1, "Inkling")).isZero();
        destroyCreature(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Inkling")).isZero();
        destroyCreature(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Inkling")).isEqualTo(1);
    }

    @Test
    void simultaneousDeathsCreateOnlyOneInkling() {
        harness.addToBattlefield(player1, new DramaticFinale());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        assertThat(countPermanents(player2, "Grizzly Bears")).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Inkling")).isEqualTo(1);
    }

    @Test
    void eachCopyTriggersIndependentlyAndBoostsTheTokens() {
        harness.addToBattlefield(player1, new DramaticFinale());
        harness.addToBattlefield(player1, new DramaticFinale());
        harness.addToBattlefield(player1, new GrizzlyBears());

        destroyCreature(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Inkling")).isEqualTo(2);
        for (Permanent inkling : findPermanents(player1, "Inkling")) {
            assertThat(gqs.getEffectivePower(gd, inkling)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, inkling)).isEqualTo(3);
        }
    }

    @Test
    void canTriggerAgainOnTheNextPlayersTurn() {
        harness.addToBattlefield(player1, new DramaticFinale());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        destroyCreature(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Inkling")).isEqualTo(1);

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Inkling")).isEqualTo(2);
    }

    @Test
    void secondDeathBeforeTriggerResolvesDoesNotTriggerAgain() {
        harness.addToBattlefield(player1, new DramaticFinale());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        assertThat(countPermanents(player1, "Inkling")).isZero();
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        assertThat(countPermanents(player1, "Inkling")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void destroyCreature(com.github.laxika.magicalvibes.model.Player player, String name) {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID creatureId = harness.getPermanentId(player, name);
        harness.castAndResolveInstant(player2, 0, creatureId);
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
