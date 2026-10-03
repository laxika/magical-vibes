package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ExpeditionEnvoy;
import com.github.laxika.magicalvibes.cards.o.Outnumber;
import com.github.laxika.magicalvibes.cards.s.SnappingGnarlid;
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

@CardUsed({ChasmGuide.class, ExpeditionEnvoy.class, SnappingGnarlid.class, Outnumber.class})
class ChasmGuideTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's Ally entry does not trigger Rally")
    void opponentAllyDoesNotTrigger() {
        Permanent guide = harness.addToBattlefieldAndReturn(player1, new ChasmGuide());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new ExpeditionEnvoy()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, guide, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Rally affects only your creatures present when it resolves")
    void laterCreaturesAndOpponentsDoNotGainHaste() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new SnappingGnarlid());
        harness.setHand(player1, List.of(new ChasmGuide(), new SnappingGnarlid()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent guide = findPermanent(player1, "Chasm Guide");
        assertThat(gqs.hasKeyword(gd, guide, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.HASTE)).isFalse();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Snapping Gnarlid"), Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, guide, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Rally resolves after Chasm Guide leaves the battlefield")
    void rallySurvivesSourceRemoval() {
        Permanent gnarlid = harness.addToBattlefieldAndReturn(player1, new SnappingGnarlid());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new SnappingGnarlid());
        harness.addToBattlefield(player2, new ExpeditionEnvoy());
        harness.setHand(player1, List.of(new ChasmGuide()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player2, List.of(new Outnumber()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent guide = findPermanent(player1, "Chasm Guide");
        harness.castInstant(player2, 0, guide.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(guide);
        assertThat(gqs.hasKeyword(gd, gnarlid, Keyword.HASTE)).isFalse();

        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, gnarlid, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Its own Ally entry gives haste to your creatures")
    void ownAllyEntryGrantsHasteToYourCreatures() {
        Permanent gnarlid = addCreatureReady(player1, new SnappingGnarlid());
        harness.setHand(player1, List.of(new ChasmGuide()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent guide = findPermanent(player1, "Chasm Guide");
        assertThat(gqs.hasKeyword(gd, guide, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, gnarlid, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Another Ally entry gives haste to all your creatures")
    void anotherAllyEntryGrantsHasteToYourCreatures() {
        Permanent guide = addCreatureReady(player1, new ChasmGuide());
        Permanent gnarlid = addCreatureReady(player1, new SnappingGnarlid());
        harness.setHand(player1, List.of(new ExpeditionEnvoy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent ally = findPermanent(player1, "Expedition Envoy");
        assertThat(gqs.hasKeyword(gd, guide, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, gnarlid, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("A non-Ally creature entry does not trigger Chasm Guide")
    void nonAllyEntryDoesNotTrigger() {
        Permanent guide = harness.addToBattlefieldAndReturn(player1, new ChasmGuide());
        harness.setHand(player1, List.of(new SnappingGnarlid()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guide, Keyword.HASTE)).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Granted haste wears off at end of turn")
    void hasteWearsOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new ChasmGuide()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent guide = findPermanent(player1, "Chasm Guide");
        assertThat(gqs.hasKeyword(gd, guide, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guide, Keyword.HASTE)).isFalse();
    }
}
