package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        Permanent island = findPermanent(player1, "Island");
        assertThat(island.isTapped()).isTrue();
    }

    @Test
    void sacrificeIsPaidBeforeSearchResolves() {
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new SamisCuriosity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent lander = findPermanent(player1, "Lander");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lander), null, null);

        harness.assertNotOnBattlefield(player1, "Lander");
        harness.assertNotOnBattlefield(player1, "Island");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Island");
    }

    @Test
    void tappedLanderCannotActivate() {
        harness.setHand(player1, List.of(new SamisCuriosity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent lander = findPermanent(player1, "Lander");
        lander.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(lander), null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Lander");
    }

    @Test
    void canFailToFindEvenWhenBasicLandIsAvailable() {
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new SamisCuriosity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent lander = findPermanent(player1, "Lander");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lander), null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Lander");
        harness.assertNotOnBattlefield(player1, "Island");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void searchCannotPutNonlandCardOntoBattlefield() {
        harness.setLibrary(player1, List.of(new SamisCuriosity()));
        harness.setHand(player1, List.of(new SamisCuriosity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent lander = findPermanent(player1, "Lander");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lander), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
