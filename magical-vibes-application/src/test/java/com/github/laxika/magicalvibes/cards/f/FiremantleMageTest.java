package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.ExpeditionEnvoy;
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

@CardUsed({FiremantleMage.class, SnappingGnarlid.class, ExpeditionEnvoy.class})
class FiremantleMageTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's Ally entry does not trigger rally")
    void opponentsAllyEntryDoesNotTrigger() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new FiremantleMage());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new ExpeditionEnvoy()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, mage, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Expedition Envoy"), Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Rally affects only your creatures present when it resolves")
    void laterCreatureDoesNotGainMenace() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new SnappingGnarlid());
        harness.setHand(player1, List.of(new FiremantleMage(), new SnappingGnarlid()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent mage = findPermanent(player1, "Firemantle Mage");
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, mage, Keyword.MENACE)).isFalse();
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, mage, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.MENACE)).isFalse();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Snapping Gnarlid"), Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, mage, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Its own Ally entry gives your creatures menace")
    void ownAllyEntryGrantsMenaceToYourCreatures() {
        Permanent gnarlid = harness.addToBattlefieldAndReturn(player1, new SnappingGnarlid());
        harness.setHand(player1, List.of(new FiremantleMage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent mage = findPermanent(player1, "Firemantle Mage");
        assertThat(gqs.hasKeyword(gd, mage, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, gnarlid, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Another Ally entry gives all your creatures menace")
    void anotherAllyEntryGrantsMenaceToYourCreatures() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new FiremantleMage());
        Permanent gnarlid = harness.addToBattlefieldAndReturn(player1, new SnappingGnarlid());
        harness.setHand(player1, List.of(new ExpeditionEnvoy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent ally = findPermanent(player1, "Expedition Envoy");
        assertThat(gqs.hasKeyword(gd, mage, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, gnarlid, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("A non-Ally creature entry does not trigger Firemantle Mage")
    void nonAllyEntryDoesNotTrigger() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new FiremantleMage());
        harness.setHand(player1, List.of(new SnappingGnarlid()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, mage, Keyword.MENACE)).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Granted menace wears off at end of turn")
    void menaceWearsOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new FiremantleMage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent mage = findPermanent(player1, "Firemantle Mage");
        assertThat(gqs.hasKeyword(gd, mage, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, mage, Keyword.MENACE)).isFalse();
    }
}
