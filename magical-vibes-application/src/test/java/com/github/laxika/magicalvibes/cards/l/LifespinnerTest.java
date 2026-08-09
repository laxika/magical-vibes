package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IwamoriOfTheOpenFist;
import com.github.laxika.magicalvibes.cards.k.KodamaOfTheCenterTree;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MoaningSpirit;
import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LifespinnerTest extends BaseCardTest {

    private Permanent addSpirit() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        TestCards.mutableCard(spirit).setSubtypes(List.of(CardSubtype.SPIRIT));
        spirit.setSummoningSick(false);
        return spirit;
    }

    @Test
    @DisplayName("Cannot activate without three Spirits to sacrifice")
    void cannotActivateWithoutThreeSpirits() {
        Permanent lifespinner = harness.addToBattlefieldAndReturn(player1, new Lifespinner());
        lifespinner.setSummoningSick(false);
        addSpirit();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Sacrifices three Spirits and searches for a legendary Spirit permanent")
    void sacrificesSpiritsAndSearchesForLegendarySpiritPermanent() {
        Permanent lifespinner = harness.addToBattlefieldAndReturn(player1, new Lifespinner());
        lifespinner.setSummoningSick(false);
        List<UUID> spiritIds = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            spiritIds.add(addSpirit().getId());
        }
        harness.addToBattlefield(player1, new LlanowarElves());

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new KodamaOfTheCenterTree(),
                new MoaningSpirit(),
                new IwamoriOfTheOpenFist(),
                new ReachThroughMists()));

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds())
                .containsExactlyInAnyOrderElementsOf(List.of(lifespinner.getId(), spiritIds.get(0), spiritIds.get(1), spiritIds.get(2)));

        for (UUID spiritId : spiritIds) {
            harness.handlePermanentChosen(player1, spiritId);
        }

        harness.assertOnBattlefield(player1, "Lifespinner");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(card -> card.getName())
                .containsExactly("Kodama of the Center Tree");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Kodama of the Center Tree");
    }
}
