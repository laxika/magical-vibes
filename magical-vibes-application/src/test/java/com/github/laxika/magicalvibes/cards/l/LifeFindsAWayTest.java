package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LifeFindsAWay.class, AirElemental.class, GrizzlyBears.class})
class LifeFindsAWayTest extends BaseCardTest {

    @Test
    @DisplayName("Populates after a nontoken creature with power 4 or greater enters")
    void populatesAfterHighPowerNontokenCreatureEnters() {
        harness.addToBattlefield(player1, new LifeFindsAWay());
        harness.addToBattlefield(player1, soldierToken());
        harness.addToBattlefield(player1, creatureToken(2, 2));
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        Permanent soldier = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Soldier Token".equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
        harness.handlePermanentChosen(player1, soldier.getId());

        assertThat(countOf(player1, "Air Elemental")).isEqualTo(1);
        assertThat(countOf(player1, "Soldier Token")).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for a nontoken creature with power less than 4")
    void doesNotTriggerForLowPowerNontokenCreature() {
        harness.addToBattlefield(player1, new LifeFindsAWay());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(countOf(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for a creature token with power 4 or greater")
    void doesNotTriggerForHighPowerCreatureToken() {
        harness.addToBattlefield(player1, new LifeFindsAWay());

        harness.enterBattlefieldAndReturn(player1, creatureToken(4, 4));

        assertThat(gd.stack).isEmpty();
        assertThat(countOf(player1, "Creature Token")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's high-power creature")
    void doesNotTriggerForOpponentsCreature() {
        harness.addToBattlefield(player1, new LifeFindsAWay());

        harness.enterBattlefieldAndReturn(player2, new AirElemental());

        assertThat(gd.stack).isEmpty();
        assertThat(countOf(player2, "Air Elemental")).isEqualTo(1);
    }

    private long countOf(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> name.equals(permanent.getCard().getName()))
                .count();
    }

    private static Card soldierToken() {
        Card card = creatureToken(1, 1);
        card.setName("Soldier Token");
        card.setColor(CardColor.WHITE);
        return card;
    }

    private static Card creatureToken(int power, int toughness) {
        Card card = new Card();
        card.setName("Creature Token");
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        card.setToken(true);
        return card;
    }
}
