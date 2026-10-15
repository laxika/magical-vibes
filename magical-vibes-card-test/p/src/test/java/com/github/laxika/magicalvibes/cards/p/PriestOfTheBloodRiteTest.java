package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PriestOfTheBloodRite.class})
class PriestOfTheBloodRiteTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 5/5 flying black Demon token when it enters")
    void etbCreatesDemonToken() {
        harness.setHand(player1, List.of(new PriestOfTheBloodRite()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the creature, ETB trigger goes on the stack
        harness.passBothPriorities(); // resolve the ETB trigger

        Permanent demon = findPermanent(player1, "Demon");
        assertThat(demon).isNotNull();
        assertThat(demon.getCard().getPower()).isEqualTo(5);
        assertThat(demon.getCard().getToughness()).isEqualTo(5);
        assertThat(demon.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Controller loses 2 life at the beginning of their upkeep")
    void upkeepTriggerLosesTwoLife() {
        harness.addToBattlefield(player1, new PriestOfTheBloodRite());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not lose life during the opponent's upkeep")
    void noLifeLossOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new PriestOfTheBloodRite());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each Priest creates exactly one Demon for its controller")
    void demonBelongsToController() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new PriestOfTheBloodRite()));
        harness.addMana(player2, ManaColor.BLACK, 5);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        assertThat(countPermanents(player2, "Demon")).isZero();
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Demon")).isEqualTo(1);
        assertThat(countPermanents(player1, "Demon")).isZero();
        Permanent demon = findPermanent(player2, "Demon");
        assertThat(demon.getCard().isToken()).isTrue();
        assertThat(demon.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(demon.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(demon.getCard().getSubtypes()).containsExactly(CardSubtype.DEMON);
        assertThat(demon.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Each Priest independently causes its controller to lose 2 life")
    void multiplePriestsLoseLifeSeparately() {
        harness.addToBattlefield(player1, new PriestOfTheBloodRite());
        harness.addToBattlefield(player1, new PriestOfTheBloodRite());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.passBothPriorities();
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }
}
