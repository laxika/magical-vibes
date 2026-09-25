package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeroesReunion.class, RagingKavu.class})
class HeroesReunionTest extends BaseCardTest {

    @Test
    @DisplayName("Target player gains 7 life")
    void gains7LifeForTargetPlayer() {
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new HeroesReunion()));
        addMana();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        harness.setLife(player1, 7);
        harness.setHand(player1, List.of(new HeroesReunion()));
        addMana();

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = addCreatureReady(player2, new RagingKavu());

        harness.setHand(player1, List.of(new HeroesReunion()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
