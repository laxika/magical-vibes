package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArastaOfTheEndlessWeb.class, Divination.class, GrizzlyBears.class, Shock.class})
class ArastaOfTheEndlessWebTest extends BaseCardTest {

    @Test
    void opponentInstantCreatesSpiderTokenWithReach() {
        setUpArasta();
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bearId);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spider")).isEqualTo(1);
        Permanent spider = findPermanent(player1, "Spider");
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, spider, Keyword.REACH)).isTrue();
    }

    @Test
    void opponentSorceryCreatesSpiderToken() {
        setUpArasta();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spider")).isEqualTo(1);
    }

    @Test
    void opponentCreatureSpellDoesNotCreateSpiderToken() {
        setUpArasta();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spider")).isZero();
    }

    @Test
    void ownInstantDoesNotCreateSpiderToken() {
        setUpArasta();
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spider")).isZero();
    }

    private void setUpArasta() {
        harness.addToBattlefield(player1, new ArastaOfTheEndlessWeb());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
