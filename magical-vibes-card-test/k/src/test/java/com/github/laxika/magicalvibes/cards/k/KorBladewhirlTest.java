package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.ExpeditionEnvoy;
import com.github.laxika.magicalvibes.cards.s.SilentSkimmer;
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

@CardUsed({KorBladewhirl.class, ExpeditionEnvoy.class, SilentSkimmer.class})
class KorBladewhirlTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's Ally does not trigger Rally")
    void opponentsAllyDoesNotTrigger() {
        Permanent bladewhirl = addCreatureReady(player1, new KorBladewhirl());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new ExpeditionEnvoy()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, bladewhirl, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Rally affects only your creatures present when it resolves")
    void rallyDoesNotAffectOpponentsOrLaterCreatures() {
        Permanent opponent = addCreatureReady(player2, new SilentSkimmer());
        harness.setHand(player1, List.of(new KorBladewhirl(), new SilentSkimmer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent bladewhirl = findPermanent(player1, "Kor Bladewhirl");
        assertThat(gqs.hasKeyword(gd, bladewhirl, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.FIRST_STRIKE)).isFalse();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent laterCreature = findPermanent(player1, "Silent Skimmer");
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bladewhirl, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Its own Ally entry gives your creatures first strike")
    void ownAllyEntryGrantsFirstStrikeToYourCreatures() {
        Permanent skimmer = addCreatureReady(player1, new SilentSkimmer());
        harness.setHand(player1, List.of(new KorBladewhirl()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent bladewhirl = findPermanent(player1, "Kor Bladewhirl");
        assertThat(gqs.hasKeyword(gd, bladewhirl, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, skimmer, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Another Ally entry gives all your creatures first strike")
    void anotherAllyEntryGrantsFirstStrikeToYourCreatures() {
        Permanent bladewhirl = addCreatureReady(player1, new KorBladewhirl());
        Permanent skimmer = addCreatureReady(player1, new SilentSkimmer());
        harness.setHand(player1, List.of(new ExpeditionEnvoy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent ally = findPermanent(player1, "Expedition Envoy");
        assertThat(gqs.hasKeyword(gd, bladewhirl, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, skimmer, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A non-Ally creature entry does not trigger Kor Bladewhirl")
    void nonAllyEntryDoesNotTrigger() {
        Permanent bladewhirl = addCreatureReady(player1, new KorBladewhirl());
        harness.setHand(player1, List.of(new SilentSkimmer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

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
        resolveAllTriggers();

        Permanent bladewhirl = findPermanent(player1, "Kor Bladewhirl");
        assertThat(gqs.hasKeyword(gd, bladewhirl, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bladewhirl, Keyword.FIRST_STRIKE)).isFalse();
    }
}
