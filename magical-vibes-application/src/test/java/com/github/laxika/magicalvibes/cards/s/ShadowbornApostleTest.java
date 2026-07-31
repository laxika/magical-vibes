package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.ReaperFromTheAbyss;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShadowbornApostleTest extends BaseCardTest {

    private List<UUID> addApostles(int count) {
        List<UUID> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Permanent apostle = harness.addToBattlefieldAndReturn(player1, new ShadowbornApostle());
            apostle.setSummoningSick(false);
            ids.add(apostle.getId());
        }
        harness.addMana(player1, ManaColor.BLACK, 1);
        return ids;
    }

    @Test
    @DisplayName("Cannot activate with fewer than six Apostles")
    void cannotActivateWithoutSixApostles() {
        addApostles(5);
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Sacrifice prompt only offers creatures named Shadowborn Apostle")
    void promptOffersOnlyApostles() {
        // Seven Apostles, so the six-permanent cost is a genuine choice rather than an auto-payment.
        List<UUID> apostles = addApostles(7);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrderElementsOf(apostles);
    }

    @Test
    @DisplayName("Paying the cost sacrifices six Apostles, including the activating one")
    void payingSacrificesSixApostles() {
        List<UUID> apostles = addApostles(6);

        harness.activateAbility(player1, 0, null, null);
        for (UUID id : apostles) {
            if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
                harness.handlePermanentChosen(player1, id);
            }
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
    }

    @Test
    @DisplayName("Resolving searches for a Demon creature card and puts it onto the battlefield")
    void resolvingPutsDemonOntoBattlefield() {
        List<UUID> apostles = addApostles(6);

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new ReaperFromTheAbyss(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        for (UUID id : apostles) {
            if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
                harness.handlePermanentChosen(player1, id);
            }
        }
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).allMatch(c -> c.getName().equals("Reaper from the Abyss"));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Reaper from the Abyss");
    }
}
