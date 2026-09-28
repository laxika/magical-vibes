package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.r.RoninCavekeeper;
import com.github.laxika.magicalvibes.cards.s.SpiralingEmbers;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FootstepsOfTheGoryo.class, RoninCavekeeper.class, SpiralingEmbers.class})
class FootstepsOfTheGoryoTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature and sacrifices it at the next end step")
    void returnsCreatureAndSacrificesItAtNextEndStep() {
        Card creature = new RoninCavekeeper();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new FootstepsOfTheGoryo()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent reanimated = findPermanent(player1, "Ronin Cavekeeper");
        assertThat(reanimated.getCard().getId()).isEqualTo(creature.getId());
        harness.assertNotInGraveyard(player1, "Ronin Cavekeeper");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ronin Cavekeeper");
        harness.assertInGraveyard(player1, "Ronin Cavekeeper");
    }

    @Test
    @DisplayName("Cannot target a noncreature card in a graveyard")
    void cannotTargetNonCreatureCard() {
        Card noncreature = new SpiralingEmbers();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setHand(player1, List.of(new FootstepsOfTheGoryo()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card creature = new RoninCavekeeper();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new FootstepsOfTheGoryo()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }
}
