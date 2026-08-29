package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnafenzaUnyieldingLineage.class, GrizzlyBears.class})
class AnafenzaUnyieldingLineageTest extends BaseCardTest {

    private static final String COUNTERS = "Put 2 +1/+1 counters on this permanent";
    private static final String SPIRIT = "Create a 2/2 white Spirit creature token";

    @Test
    @DisplayName("Another nontoken creature dying lets Anafenza put two +1/+1 counters on itself")
    void enduresWithCounters() {
        Permanent anafenza = addAnafenzaAndBear();

        killBear();
        harness.passBothPriorities();
        harness.handleListChoice(player1, COUNTERS);

        assertThat(anafenza.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Another nontoken creature dying lets Anafenza create a 2/2 white Spirit")
    void enduresWithSpirit() {
        addAnafenzaAndBear();

        killBear();
        harness.passBothPriorities();
        harness.handleListChoice(player1, SPIRIT);

        Permanent spirit = findPermanents(player1, "Spirit").getFirst();
        assertThat(spirit.getCard().isToken()).isTrue();
        assertThat(spirit.getCard().getPower()).isEqualTo(2);
        assertThat(spirit.getCard().getToughness()).isEqualTo(2);
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
    }

    @Test
    @DisplayName("If Anafenza leaves before endure resolves, a Spirit is created")
    void enduresWithSpiritWhenSourceLeaves() {
        Permanent anafenza = addAnafenzaAndBear();
        killBear();

        anafenza.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent spirit = findPermanents(player1, "Spirit").getFirst();
        assertThat(spirit.getCard().getPower()).isEqualTo(2);
        assertThat(spirit.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A token creature dying does not trigger Anafenza")
    void tokenCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new AnafenzaUnyieldingLineage());
        Card token = new Card();
        token.setName("Spirit");
        token.setType(CardType.CREATURE);
        token.setColor(CardColor.WHITE);
        token.setSubtypes(List.of(CardSubtype.SPIRIT));
        token.setPower(2);
        token.setToughness(2);
        token.setToken(true);
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, token);

        spirit.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addAnafenzaAndBear() {
        Permanent anafenza = harness.addToBattlefieldAndReturn(player1, new AnafenzaUnyieldingLineage());
        harness.addToBattlefield(player1, new GrizzlyBears());
        return anafenza;
    }

    private void killBear() {
        Permanent bear = findPermanent(player1, "Grizzly Bears");
        bear.setMarkedDamage(2);
        harness.runStateBasedActions();
    }
}
