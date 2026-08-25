package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.n.NornsInquisitor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({GlissaHeraldOfPredation.class, NornsInquisitor.class, GrizzlyBears.class})
class GlissaHeraldOfPredationTest extends BaseCardTest {

    @Test
    @DisplayName("Incubate mode creates two Incubator tokens with two +1/+1 counters each")
    void incubatesTwice() {
        addGlissa(player1);

        advanceToCombat(player1);
        harness.handleListChoice(player1, "Incubate 2 twice");
        harness.passBothPriorities();

        List<Permanent> incubators = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Incubator"))
                .toList();
        assertThat(incubators).hasSize(2);
        assertThat(incubators).allSatisfy(incubator ->
                assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2));
    }

    @Test
    @DisplayName("Transform mode transforms all Incubator tokens you control")
    void transformsAllIncubators() {
        addIncubatorSource();
        addGlissa(player1);

        List<Permanent> incubators = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Incubator"))
                .toList();
        assertThat(incubators).hasSize(2);

        advanceToCombat(player1);
        harness.handleListChoice(player1, "Transform all Incubator tokens you control");
        harness.passBothPriorities();

        assertThat(incubators).allSatisfy(incubator -> assertThat(incubator.isTransformed()).isTrue());
    }

    @Test
    @DisplayName("Keyword mode affects only Phyrexians you control until end of turn")
    void grantsKeywordsToControlledPhyrexians() {
        Permanent glissa = addGlissa(player1);
        Permanent nonPhyrexian = addCreature(player1);
        Permanent opposingGlissa = addGlissa(player2);

        advanceToCombat(player1);
        harness.handleListChoice(player1,
                "Phyrexians you control gain first strike and deathtouch until end of turn");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, glissa, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, glissa, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonPhyrexian, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonPhyrexian, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingGlissa, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingGlissa, Keyword.DEATHTOUCH)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, glissa, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, glissa, Keyword.DEATHTOUCH)).isFalse();
    }

    private Permanent addGlissa(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GlissaHeraldOfPredation());
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    private void addIncubatorSource() {
        harness.setHand(player1, List.of(new NornsInquisitor(), new NornsInquisitor()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
