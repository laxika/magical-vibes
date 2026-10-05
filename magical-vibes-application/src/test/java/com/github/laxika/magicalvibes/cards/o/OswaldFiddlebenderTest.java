package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OswaldFiddlebender.class, DarksteelIngot.class, MindStone.class})
class OswaldFiddlebenderTest extends BaseCardTest {

    @Test
    void sacrificesAnArtifactAndPutsAnArtifactWithManaValueOneHigherOntoTheBattlefield() {
        addCreatureReady(player1, new OswaldFiddlebender());
        harness.addToBattlefield(player1, new MindStone());
        harness.setLibrary(player1, List.of(new DarksteelIngot()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Mind Stone");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Darksteel Ingot");
    }

    @Test
    void offersOnlyArtifactsWithManaValueOneHigher() {
        addCreatureReady(player1, new OswaldFiddlebender());
        harness.addToBattlefield(player1, new MindStone());
        harness.setLibrary(player1, List.of(new DarksteelIngot(), new OswaldFiddlebender()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(com.github.laxika.magicalvibes.model.Card::getName)
                .containsExactly("Darksteel Ingot");
    }

    @Test
    void canOnlyBeActivatedAtSorcerySpeed() {
        harness.addToBattlefield(player1, new OswaldFiddlebender());
        harness.addToBattlefield(player1, new MindStone());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void canFailToFindEvenWhenAnEligibleArtifactExists() {
        addCreatureReady(player1, new OswaldFiddlebender());
        harness.addToBattlefield(player1, new MindStone());
        harness.setLibrary(player1, List.of(new DarksteelIngot()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Mind Stone");
        harness.assertNotOnBattlefield(player1, "Darksteel Ingot");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotFindAnArtifactWithTheSameManaValue() {
        addCreatureReady(player1, new OswaldFiddlebender());
        harness.addToBattlefield(player1, new MindStone());
        harness.setLibrary(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mind Stone");
        harness.assertNotOnBattlefield(player1, "Mind Stone");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canResolveWithAnEmptyLibraryAndStillPaysCosts() {
        Permanent oswald = addCreatureReady(player1, new OswaldFiddlebender());
        harness.addToBattlefield(player1, new MindStone());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(oswald.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Mind Stone");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSacrificeAnOpponentsArtifact() {
        addCreatureReady(player1, new OswaldFiddlebender());
        harness.addToBattlefield(player2, new MindStone());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");

        harness.assertOnBattlefield(player2, "Mind Stone");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new OswaldFiddlebender());
        harness.addToBattlefield(player1, new MindStone());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        harness.assertOnBattlefield(player1, "Mind Stone");
        assertThat(gd.stack).isEmpty();
    }
}
