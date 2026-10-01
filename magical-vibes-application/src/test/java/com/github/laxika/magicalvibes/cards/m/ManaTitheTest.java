package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.Harmonize;
import com.github.laxika.magicalvibes.cards.k.KavuPredator;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManaTithe.class, KavuPredator.class, Harmonize.class})
class ManaTitheTest extends BaseCardTest {

    @Test
    void countersSpellWhenItsControllerCannotPayOneMana() {
        KavuPredator predator = new KavuPredator();
        harness.setHand(player1, List.of(predator));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new ManaTithe()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, predator.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kavu Predator");
        harness.assertNotOnBattlefield(player1, "Kavu Predator");
    }

    @Test
    void resolvesSpellWhenItsControllerPaysOneMana() {
        KavuPredator predator = new KavuPredator();
        harness.setHand(player1, List.of(predator));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.setHand(player2, List.of(new ManaTithe()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, predator.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kavu Predator");
    }

    @Test
    void countersSpellWhenItsControllerDeclinesToPay() {
        KavuPredator predator = new KavuPredator();
        harness.setHand(player1, List.of(predator));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.setHand(player2, List.of(new ManaTithe()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, predator.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Kavu Predator");
        harness.assertNotOnBattlefield(player1, "Kavu Predator");
    }

    @Test
    void countersNoncreatureSpellWhenItsControllerCannotPayOneMana() {
        Harmonize harmonize = new Harmonize();
        harness.setHand(player1, List.of(harmonize));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new ManaTithe()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, harmonize.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Harmonize");
    }
}
