package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FrostOgre;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.i.IwamoriOfTheOpenFist;
import com.github.laxika.magicalvibes.cards.k.KodamaOfTheCenterTree;
import com.github.laxika.magicalvibes.cards.k.KamiOfFalseHope;
import com.github.laxika.magicalvibes.cards.m.MendingHands;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        Lifespinner.class,
        GnarledMass.class,
        FrostOgre.class,
        KodamaOfTheCenterTree.class,
        KamiOfFalseHope.class,
        IwamoriOfTheOpenFist.class,
        MendingHands.class
})
class LifespinnerTest extends BaseCardTest {

    private Permanent addSpirit() {
        return addCreatureReady(player1, new GnarledMass());
    }

    @Test
    @DisplayName("Cannot activate without three Spirits to sacrifice")
    void cannotActivateWithoutThreeSpirits() {
        Permanent lifespinner = addCreatureReady(player1, new Lifespinner());
        addSpirit();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
        assertThat(lifespinner.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Sacrifices three Spirits and searches for a legendary Spirit permanent")
    void sacrificesSpiritsAndSearchesForLegendarySpiritPermanent() {
        Permanent lifespinner = addCreatureReady(player1, new Lifespinner());
        List<UUID> spiritIds = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            spiritIds.add(addSpirit().getId());
        }
        Permanent nonSpirit = addCreatureReady(player1, new FrostOgre());

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new KodamaOfTheCenterTree(),
                new KamiOfFalseHope(),
                new IwamoriOfTheOpenFist(),
                new MendingHands()));

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds())
                .containsExactlyInAnyOrderElementsOf(List.of(
                        lifespinner.getId(), spiritIds.get(0), spiritIds.get(1), spiritIds.get(2)));
        assertThat(choice.validPermanentIds()).doesNotContain(nonSpirit.getId());

        for (UUID spiritId : spiritIds) {
            harness.handlePermanentChosen(player1, spiritId);
        }

        assertThat(lifespinner.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Lifespinner");
        harness.assertOnBattlefield(player1, "Frost Ogre");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(card -> card.getName())
                .containsExactly("Kodama of the Center Tree");

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Kodama of the Center Tree");
    }
}
