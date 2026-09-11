package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
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
        Permanent oswald = harness.addToBattlefieldAndReturn(player1, new OswaldFiddlebender());
        oswald.setSummoningSick(false);
        harness.addToBattlefield(player1, new MindStone());
        harness.setLibrary(player1, List.of(new DarksteelIngot()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Mind Stone");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Darksteel Ingot");
    }

    @Test
    void offersOnlyArtifactsWithManaValueOneHigher() {
        Permanent oswald = harness.addToBattlefieldAndReturn(player1, new OswaldFiddlebender());
        oswald.setSummoningSick(false);
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
}
