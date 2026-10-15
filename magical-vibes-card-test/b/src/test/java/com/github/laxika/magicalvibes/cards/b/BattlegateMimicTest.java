package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattlegateMimic.class})
class BattlegateMimicTest extends BaseCardTest {

    @BeforeEach
    void setUpTest() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    /** A raw creature spell that is both red and white. */
    private Card redWhiteSpell() {
        Card card = new Card();
        card.setName("Boros Test Bear");
        card.setType(CardType.CREATURE);
        card.setManaCost("{R}{W}");
        card.setColor(CardColor.RED);
        card.setColors(List.of(CardColor.RED, CardColor.WHITE));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    /** A raw creature spell that is only red. */
    private Card redSpell() {
        Card card = new Card();
        card.setName("Mono Red Test Bear");
        card.setType(CardType.CREATURE);
        card.setManaCost("{R}");
        card.setColor(CardColor.RED);
        card.setColors(List.of(CardColor.RED));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    @Test
    @DisplayName("Casting a red-and-white spell makes the Mimic 4/2 with first strike")
    void redWhiteSpellPumpsMimic() {
        Permanent mimic = addCreatureReady(player1, new BattlegateMimic());
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FIRST_STRIKE)).isFalse();

        harness.castFromHand(player1, redWhiteSpell(), "{R}{W}");
        harness.passBothPriorities(); // resolve the triggered ability

        assertThat(mimic.getEffectivePower()).isEqualTo(4);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Pump and first strike wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent mimic = addCreatureReady(player1, new BattlegateMimic());

        harness.castFromHand(player1, redWhiteSpell(), "{R}{W}");
        harness.passBothPriorities();
        assertThat(mimic.getEffectivePower()).isEqualTo(4);

        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mimic.getEffectivePower()).isEqualTo(2);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Casting a mono-red spell does not trigger the Mimic")
    void monoRedSpellDoesNotTrigger() {
        Permanent mimic = addCreatureReady(player1, new BattlegateMimic());

        harness.castFromHand(player1, redSpell(), "{R}");
        harness.passBothPriorities();

        assertThat(mimic.getEffectivePower()).isEqualTo(2);
        assertThat(mimic.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void hybridSpellTriggersEvenWhenPaidWithOnlyRedMana() {
        Permanent mimic = addCreatureReady(player1, new BattlegateMimic());
        harness.setHand(player1, List.of(new BattlegateMimic()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FIRST_STRIKE)).isFalse();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FIRST_STRIKE)).isTrue();
        resolveAllTriggers();

        Permanent enteringMimic = findPermanents(player1, "Battlegate Mimic").stream()
                .filter(permanent -> !permanent.getId().equals(mimic.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, enteringMimic)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, enteringMimic, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void opponentsRedWhiteSpellDoesNotTrigger() {
        Permanent mimic = addCreatureReady(player1, new BattlegateMimic());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new BattlegateMimic()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void repeatedTriggersSetBasePowerRatherThanAddingPower() {
        Permanent mimic = addCreatureReady(player1, new BattlegateMimic());
        for (int i = 0; i < 2; i++) {
            harness.setHand(player1, List.of(new BattlegateMimic()));
            harness.addMana(player1, ManaColor.WHITE, 2);
            harness.castCreature(player1, 0);
            resolveAllTriggers();
        }

        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void countersApplyOnTopOfTheNewBasePowerAndToughness() {
        Permanent mimic = addCreatureReady(player1, new BattlegateMimic());
        mimic.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new BattlegateMimic()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, mimic, Keyword.FIRST_STRIKE)).isTrue();
    }
}
