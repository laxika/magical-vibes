package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.cards.s.Skullcrack;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeroesReunion.class, RagingKavu.class, Skullcrack.class})
class HeroesReunionTest extends BaseCardTest {

    @Test
    @DisplayName("Target player gains 7 life")
    void gains7LifeForTargetPlayer() {
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new HeroesReunion()));
        addMana();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        harness.setLife(player1, 7);
        harness.setHand(player1, List.of(new HeroesReunion()));
        addMana();

        harness.castAndResolveInstant(player1, 0, player1.getId());

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

    @Test
    @DisplayName("Life gain can exceed the starting life total")
    void gainsLifeAboveStartingTotal() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new HeroesReunion()));
        addMana();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 27);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Heroes' Reunion");
    }

    @Test
    @DisplayName("Skullcrack prevents the targeted player from gaining life")
    void cannotGainLifeAfterSkullcrack() {
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new Skullcrack(), new HeroesReunion()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 7);

        addMana();
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 7);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Heroes' Reunion");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
