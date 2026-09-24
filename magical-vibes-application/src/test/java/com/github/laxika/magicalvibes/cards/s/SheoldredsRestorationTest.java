package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SheoldredsRestoration.class, GrizzlyBears.class, DarkRitual.class})
class SheoldredsRestorationTest extends BaseCardTest {

    @Test
    void returnsCreatureLosesLifeAndExilesItselfWithoutKicker() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new SheoldredsRestoration()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 18);
        harness.assertNotInGraveyard(player1, "Sheoldred's Restoration");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Sheoldred's Restoration"));
    }

    @Test
    void returnsCreatureGainsLifeAndExilesItselfWhenKicked() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new SheoldredsRestoration()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, creature.getId(), null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 22);
        harness.assertNotInGraveyard(player1, "Sheoldred's Restoration");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Sheoldred's Restoration"));
    }

    @Test
    void cannotTargetNonCreatureCard() {
        Card instant = new DarkRitual();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new SheoldredsRestoration()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
