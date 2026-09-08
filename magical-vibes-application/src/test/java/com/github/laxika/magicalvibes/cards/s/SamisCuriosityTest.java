package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SamisCuriosity.class, Island.class})
class SamisCuriosityTest extends BaseCardTest {

    @Test
    void gainsLifeAndCreatesLander() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SamisCuriosity()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(findPermanents(player1, "Lander")).hasSize(1);
    }

    @Test
    void landerSacrificesAndPutsBasicLandOntoBattlefieldTapped() {
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new SamisCuriosity()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent lander = findPermanent(player1, "Lander");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lander), null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        Permanent island = findPermanent(player1, "Island");
        assertThat(island.isTapped()).isTrue();
    }
}
