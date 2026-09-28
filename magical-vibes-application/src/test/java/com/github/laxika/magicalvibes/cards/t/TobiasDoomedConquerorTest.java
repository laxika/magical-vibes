package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TobiasDoomedConqueror.class, GrizzlyBears.class, Shock.class})
class TobiasDoomedConquerorTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Zombie for each nontoken creature controlled by Tobias's controller that died this turn")
    void createsZombiesForOwnNontokenDeathsIncludingTobias() {
        Permanent tobias = addCreatureReady(player1, new TobiasDoomedConqueror());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownToken = addCreatureReady(player1, tokenCreature());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        killWithShock(player2, ownCreature);
        killWithShock(player2, ownToken);
        killWithShock(player1, opposingCreature);
        killWithShock(player2, tobias);
        harness.passBothPriorities();

        List<Permanent> zombies = findPermanents(player1, "Zombie");
        assertThat(zombies).hasSize(2);
        assertThat(zombies).allSatisfy(zombie -> {
            assertThat(zombie.getCard().isToken()).isTrue();
            assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(zombie.getEffectivePower()).isEqualTo(2);
            assertThat(zombie.getEffectiveToughness()).isEqualTo(2);
        });
    }

    private void killWithShock(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }

    private Card tokenCreature() {
        Card card = new Card();
        card.setName("Soldier Token");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
