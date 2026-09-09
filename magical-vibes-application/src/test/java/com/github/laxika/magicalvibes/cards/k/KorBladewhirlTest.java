package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.ExpeditionEnvoy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({KorBladewhirl.class, ExpeditionEnvoy.class, GrizzlyBears.class})
class KorBladewhirlTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry gives your creatures first strike")
    void ownAllyEntryGrantsFirstStrikeToYourCreatures() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KorBladewhirl()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bladewhirl = findPermanent(player1, "Kor Bladewhirl");
        assertThat(gqs.hasKeyword(gd, bladewhirl, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Another Ally entry gives all your creatures first strike")
    void anotherAllyEntryGrantsFirstStrikeToYourCreatures() {
        Permanent bladewhirl = addCreatureReady(player1, new KorBladewhirl());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExpeditionEnvoy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent ally = findPermanent(player1, "Expedition Envoy");
        assertThat(gqs.hasKeyword(gd, bladewhirl, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A non-Ally creature entry does not trigger Kor Bladewhirl")
    void nonAllyEntryDoesNotTrigger() {
        Permanent bladewhirl = addCreatureReady(player1, new KorBladewhirl());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bladewhirl, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("First strike wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new KorBladewhirl()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bladewhirl = findPermanent(player1, "Kor Bladewhirl");
        assertThat(gqs.hasKeyword(gd, bladewhirl, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bladewhirl, Keyword.FIRST_STRIKE)).isFalse();
    }
}
