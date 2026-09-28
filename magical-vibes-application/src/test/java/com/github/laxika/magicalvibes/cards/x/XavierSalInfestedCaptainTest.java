package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({XavierSalInfestedCaptain.class, GrizzlyBears.class})
class XavierSalInfestedCaptainTest extends BaseCardTest {

    @Test
    void removesCounterAndPopulates() {
        addCaptain();
        Permanent token = addCreatureReady(player1, creatureToken());
        token.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        activate(0);
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(countOf(player1, "Soldier Token")).isEqualTo(2);
    }

    @Test
    void sacrificesAnotherCreatureAndProliferates() {
        addCaptain();
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        activate(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    void cannotPayEitherAbilityWithOnlyTheCaptain() {
        addCaptain();

        assertThatThrownBy(() -> activate(0)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> activate(1)).isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCaptain() {
        return addCreatureReady(player1, new XavierSalInfestedCaptain());
    }

    private void activate(int abilityIndex) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, abilityIndex, null, null);
    }

    private long countOf(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> name.equals(permanent.getCard().getName()))
                .count();
    }

    private static Card creatureToken() {
        Card card = new Card();
        card.setName("Soldier Token");
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
