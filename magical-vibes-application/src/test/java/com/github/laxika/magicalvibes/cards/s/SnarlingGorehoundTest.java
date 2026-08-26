package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SnarlingGorehound.class, GrizzlyBears.class, HillGiant.class})
class SnarlingGorehoundTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils 1 when another creature with power 2 or less enters under its controller's control")
    void surveilsWhenSmallAllyEnters() {
        harness.addToBattlefield(player1, new SnarlingGorehound());
        Card topCard = new HillGiant();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Does not trigger for a creature with power greater than 2")
    void doesNotTriggerForLargeCreature() {
        harness.addToBattlefield(player1, new SnarlingGorehound());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).isEmpty();
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's creature")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new SnarlingGorehound());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.addToBattlefield(player2, new GrizzlyBears());

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).isEmpty();
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not trigger for itself entering the battlefield")
    void doesNotTriggerForItself() {
        harness.setHand(player1, List.of(new SnarlingGorehound()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).isEmpty();
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }
}
