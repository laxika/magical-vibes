package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Plaxmanta.class, MistralCharger.class})
class PlaxmantaTest extends BaseCardTest {

    @Test
    @DisplayName("Both enters abilities trigger separately even when green mana was spent")
    void entersAbilitiesAreSeparateTriggers() {
        harness.setHand(player1, List.of(new Plaxmanta()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Plaxmanta");
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "Plaxmanta");
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Plaxmanta");
    }

    @Test
    @DisplayName("Entering without being cast still grants shroud and requires sacrifice")
    void enteringWithoutCastingRequiresSacrifice() {
        Permanent charger = addCreatureReady(player1, new MistralCharger());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.enterBattlefieldAndReturn(player1, new Plaxmanta());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, charger, Keyword.SHROUD)).isTrue();
        harness.assertNotOnBattlefield(player1, "Plaxmanta");
        harness.assertInGraveyard(player1, "Plaxmanta");
    }

    @Test
    @DisplayName("Creatures entering after the shroud ability resolves do not gain shroud")
    void laterCreaturesDoNotGainShroud() {
        Permanent charger = addCreatureReady(player1, new MistralCharger());
        harness.setHand(player1, List.of(new Plaxmanta()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent laterCharger = harness.enterBattlefieldAndReturn(player1, new MistralCharger());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, charger, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterCharger, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Gives your creatures, but not an opponent's creatures, shroud until end of turn when green mana was spent")
    void givesYourCreaturesShroudWhenGreenManaWasSpent() {
        Permanent charger = addCreatureReady(player1, new MistralCharger());
        Permanent opponentCharger = addCreatureReady(player2, new MistralCharger());
        harness.setHand(player1, List.of(new Plaxmanta()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent plaxmanta = findPermanent(player1, "Plaxmanta");
        assertThat(gqs.hasKeyword(gd, charger, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, plaxmanta, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCharger, Keyword.SHROUD)).isFalse();
        harness.assertOnBattlefield(player1, "Plaxmanta");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, charger, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, plaxmanta, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Still grants shroud before being sacrificed when green mana was not spent")
    void isSacrificedWithoutGreenMana() {
        Permanent charger = addCreatureReady(player1, new MistralCharger());
        harness.setHand(player1, List.of(new Plaxmanta()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, charger, Keyword.SHROUD)).isTrue();
        harness.assertNotOnBattlefield(player1, "Plaxmanta");
        harness.assertInGraveyard(player1, "Plaxmanta");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, charger, Keyword.SHROUD)).isFalse();
    }
}
