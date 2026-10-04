package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.n.NornsInquisitor;
import com.github.laxika.magicalvibes.cards.t.TimberlandAncient;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({GlissaHeraldOfPredation.class, NornsInquisitor.class, TimberlandAncient.class})
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
        assertThat(incubators).allSatisfy(incubator -> {
            assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
            assertThat(incubator.getCard().getSubtypes()).contains(CardSubtype.INCUBATOR);
            assertThat(gqs.isCreature(gd, incubator)).isFalse();
        });
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

    @Test
    void incubatedTokenCanTransformByPayingTwoMana() {
        addGlissa(player1);
        advanceToCombat(player1);
        harness.handleListChoice(player1, "Incubate 2 twice");
        resolveAllTriggers();

        Permanent incubator = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Incubator"))
                .findFirst().orElseThrow();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(incubator),
                null, null);
        resolveAllTriggers();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(gqs.isCreature(gd, incubator)).isTrue();
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(2);
        assertThat(incubator.getCard().getSubtypes()).contains(CardSubtype.PHYREXIAN);
        assertThat(incubator.getCard().getName()).isEqualTo("Phyrexian Token");
    }

    @Test
    void transformModeLeavesOpposingAndAlreadyTransformedTokensAlone() {
        addGlissa(player2);
        advanceToCombat(player2);
        harness.handleListChoice(player2, "Incubate 2 twice");
        resolveAllTriggers();
        List<Permanent> opposingTokens = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();

        addGlissa(player1);
        advanceToCombat(player1);
        harness.handleListChoice(player1, "Incubate 2 twice");
        resolveAllTriggers();
        List<Permanent> ownTokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();

        advanceToCombat(player1);
        harness.handleListChoice(player1, "Transform all Incubator tokens you control");
        resolveAllTriggers();
        advanceToCombat(player1);
        harness.handleListChoice(player1, "Transform all Incubator tokens you control");
        resolveAllTriggers();

        assertThat(ownTokens).hasSize(2).allSatisfy(token -> {
            assertThat(token.isTransformed()).isTrue();
            assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        });
        assertThat(opposingTokens).hasSize(2)
                .allSatisfy(token -> assertThat(token.isTransformed()).isFalse());
    }

    @Test
    void keywordModeDoesNotAffectPhyrexiansEnteringAfterResolution() {
        Permanent glissa = addGlissa(player1);
        advanceToCombat(player1);
        harness.handleListChoice(player1,
                "Phyrexians you control gain first strike and deathtouch until end of turn");
        resolveAllTriggers();

        Permanent laterPhyrexian = harness.addToBattlefieldAndReturn(player1, new NornsInquisitor());
        assertThat(gqs.hasKeyword(gd, glissa, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, glissa, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterPhyrexian, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, laterPhyrexian, Keyword.DEATHTOUCH)).isFalse();
    }

    private Permanent addGlissa(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GlissaHeraldOfPredation());
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new TimberlandAncient());
    }

    private void addIncubatorSource() {
        harness.setHand(player1, List.of(new NornsInquisitor(), new NornsInquisitor()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
    }
}
