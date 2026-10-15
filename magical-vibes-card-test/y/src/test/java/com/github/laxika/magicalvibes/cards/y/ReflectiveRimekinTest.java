package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReflectiveRimekin.class, LavaAxe.class, Shock.class, Divination.class, Fireball.class})
class ReflectiveRimekinTest extends BaseCardTest {

    @Test
    @DisplayName("Copies the next instant or sorcery with mana value 3 or less, even after a turn ends")
    void copiesNextSmallInstantOrSorceryOnceAcrossTurns() {
        harness.setHand(player1, List.of(new ReflectiveRimekin()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of());
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        Shock firstShock = new Shock();
        Shock secondShock = new Shock();
        harness.setHand(player1, List.of(firstShock, secondShock));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Does not consume the boon on an instant or sorcery with mana value greater than 3")
    void ignoresExpensiveSorcery() {
        harness.setHand(player1, List.of(new ReflectiveRimekin(), new LavaAxe(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
    }

    @Test
    void mayChooseNewTargetForCopyWithoutChangingOriginal() {
        harness.castFromHand(player1, new ReflectiveRimekin(), "{2}{U}");
        resolveAllTriggers();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void opponentsSpellDoesNotConsumeBoonAndBoonSurvivesSourceLeaving() {
        harness.castFromHand(player1, new ReflectiveRimekin(), "{2}{U}");
        resolveAllTriggers();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Reflective Rimekin"));
        harness.assertInGraveyard(player1, "Reflective Rimekin");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, 16);
    }

    @Test
    void copiesUntargetedSorceryAtManaValueThree() {
        harness.castFromHand(player1, new ReflectiveRimekin(), "{2}{U}");
        resolveAllTriggers();
        harness.setHand(player1, List.of(new Divination()));
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    void chosenXAboveManaValueLimitDoesNotConsumeBoon() {
        harness.castFromHand(player1, new ReflectiveRimekin(), "{2}{U}");
        resolveAllTriggers();
        harness.setHand(player1, List.of(new Fireball(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 3, player2.getId());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertLife(player2, 17);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, 13);
    }
}
