package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnchantedCarriage.class, GrizzlyBears.class})
class EnchantedCarriageTest extends BaseCardTest {

    @Test
    void entersAndCreatesTwoWhiteMouseTokens() {
        harness.setHand(player1, List.of(new EnchantedCarriage()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> mice = findPermanents(player1, "Mouse");
        assertThat(mice).hasSize(2);
        assertThat(mice).allMatch(mouse -> mouse.getCard().isToken()
                && mouse.getCard().getPower() == 1
                && mouse.getCard().getToughness() == 1
                && mouse.getCard().getColor() == CardColor.WHITE
                && mouse.getCard().getSubtypes().contains(CardSubtype.MOUSE));
    }

    @Test
    void crewAnimatesCarriageAndTapsTheCrewUntilEndOfTurn() {
        Permanent carriage = addCarriageReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, carriage)).isTrue();
        assertThat(crew.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, carriage)).isFalse();
    }

    @Test
    void cannotCrewWithoutEnoughCreaturePower() {
        addCarriageReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void newlyCreatedMiceCanCrewBeforeTheyCanAttack() {
        harness.setHand(player1, List.of(new EnchantedCarriage()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent carriage = findPermanent(player1, "Enchanted Carriage");
        assertThat(findPermanents(player1, "Mouse")).isEmpty();
        assertThat(gqs.isCreature(gd, carriage)).isFalse();

        harness.passBothPriorities();
        List<Permanent> mice = findPermanents(player1, "Mouse");
        assertThat(mice).hasSize(2);
        assertThat(mice).allMatch(Permanent::isSummoningSick);

        harness.activateAbility(player1, 0, null, null);

        assertThat(mice).allMatch(Permanent::isTapped);
        assertThat(carriage.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, carriage)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, carriage)).isTrue();
        assertThat(carriage.isSummoningSick()).isTrue();
        assertThat(findPermanents(player1, "Mouse")).hasSize(2);
    }

    @Test
    void oneUntappedMouseIsNotEnoughToCrew() {
        harness.setHand(player1, List.of(new EnchantedCarriage()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        List<Permanent> mice = findPermanents(player1, "Mouse");
        mice.getFirst().tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");

        assertThat(mice.getLast().isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, findPermanent(player1, "Enchanted Carriage"))).isFalse();
    }

    @Test
    void opposingCreaturesCannotPayCrewCost() {
        addCarriageReady(player1);
        Permanent opposingCrew = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");

        assertThat(opposingCrew.isTapped()).isFalse();
    }

    @Test
    void anAnimatedCarriageCannotCrewItself() {
        Permanent carriage = addCarriageReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, carriage)).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(carriage.isTapped()).isFalse();
    }

    private Permanent addCarriageReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new EnchantedCarriage());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
