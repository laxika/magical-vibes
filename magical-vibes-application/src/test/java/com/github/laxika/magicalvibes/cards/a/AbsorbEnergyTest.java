package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbsorbEnergy.class, GrizzlyBears.class, AirElemental.class, Divination.class})
class AbsorbEnergyTest extends BaseCardTest {

    @Test
    void perpetuallyReducesMatchingCreatureCardsInHand() {
        GrizzlyBears target = new GrizzlyBears();
        AirElemental matchingCard = new AirElemental();

        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new AbsorbEnergy(), matchingCard));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(harness.getGameData().currentStep);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player2, 0);
    }

    @Test
    void doesNotReduceCardsWithoutASharedCardType() {
        GrizzlyBears target = new GrizzlyBears();

        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new AbsorbEnergy(), new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(harness.getGameData().currentStep);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void countersTheSpellAndReducesEveryMatchingCardInItsControllersHand() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setHand(player1, List.of(target, new AirElemental()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new AbsorbEnergy(), new AirElemental(), new AirElemental()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        assertThat(countPermanents(player2, "Air Elemental")).isEqualTo(2);
    }

    @Test
    void doesNotApplyASecondReductionWhenTargetHasAlreadyBeenCountered() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new AbsorbEnergy(), new AbsorbEnergy(), new AirElemental()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.castInstant(player2, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
    }

    @Test
    void reductionDoesNotPayColoredManaRequirements() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new AbsorbEnergy(), new AirElemental()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castCreature(player2, 0);
    }

    @Test
    void reductionsFromSeparateResolutionsAccumulate() {
        GrizzlyBears firstTarget = new GrizzlyBears();
        GrizzlyBears secondTarget = new GrizzlyBears();
        harness.setHand(player1, List.of(firstTarget, secondTarget));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new AbsorbEnergy(), new AbsorbEnergy(), new AirElemental()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player2, 0, firstTarget.getId());
        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player2, 0, secondTarget.getId());

        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
    }

    @Test
    void counteringAnInstantReducesMatchingInstants() {
        AbsorbEnergy target = new AbsorbEnergy();
        Divination originalSpell = new Divination();
        harness.setHand(player1, List.of(originalSpell, target));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.setHand(player2, List.of(new AbsorbEnergy(), new AbsorbEnergy()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0);
        harness.castInstant(player1, 0, originalSpell.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.assertInGraveyard(player1, "Absorb Energy");

        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, originalSpell.getId());
        harness.assertInGraveyard(player1, "Divination");
    }
}
