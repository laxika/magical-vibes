package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Slay;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YavimayaElder.class, Forest.class, Plains.class, Slay.class})
class YavimayaElderTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the death trigger searches for up to two basic lands")
    void deathTriggerSearchesForBasicLands() {
        harness.addToBattlefield(player1, new YavimayaElder());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Forest forest = new Forest();
        Plains plains = new Plains();
        YavimayaElder nonBasic = new YavimayaElder();
        harness.setLibrary(player1, List.of(forest, plains, nonBasic));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactlyInAnyOrder(forest, plains);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).contains(forest, plains);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonBasic);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
    }

    @Test
    @DisplayName("The death trigger may find only one basic land")
    void deathTriggerMayFindOnlyOneBasicLand() {
        harness.addToBattlefield(player1, new YavimayaElder());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Forest forest = new Forest();
        Plains plains = new Plains();
        YavimayaElder nonBasic = new YavimayaElder();
        harness.setLibrary(player1, List.of(forest, plains, nonBasic));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(plains, nonBasic);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Accepting the death trigger with no basic lands finds nothing")
    void deathTriggerCanFindNoBasicLand() {
        harness.addToBattlefield(player1, new YavimayaElder());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        YavimayaElder nonBasic = new YavimayaElder();
        harness.setLibrary(player1, List.of(nonBasic));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(nonBasic);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A destruction death also triggers the basic land search")
    void destructionTriggersDeathAbility() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new YavimayaElder());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.setHand(player2, List.of(new Slay()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, elder.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Yavimaya Elder");
        harness.assertInGraveyard(player1, "Yavimaya Elder");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the death trigger still allows the sacrifice ability to draw")
    void decliningDeathTriggerStillDraws() {
        harness.addToBattlefield(player1, new YavimayaElder());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new YavimayaElder()));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof YavimayaElder);
    }
}
