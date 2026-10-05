package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed(LongBodiedGreyDog.class)
class LongBodiedGreyDogTest extends BaseCardTest {

    @Test
    @DisplayName("When Long-Bodied Grey Dog enters, it creates a tapped Treasure token")
    void entersCreatesTappedTreasure() {
        harness.setHand(player1, List.of(new LongBodiedGreyDog()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.isTapped()).isTrue();
        assertThat(treasure.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(treasure.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
        assertThat(treasure.getCard().isToken()).isTrue();
    }

    @Test
    void canBeCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new LongBodiedGreyDog()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.ensurePriority(player1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Long-Bodied Grey Dog")).isEqualTo(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1)
                .allMatch(Permanent::isTapped);
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }

    @Test
    void entryTriggerResolvesAfterDogLeavesBattlefield() {
        Permanent dog = harness.enterBattlefieldAndReturn(player1, new LongBodiedGreyDog());
        gd.playerBattlefields.get(player1.getId()).remove(dog);
        harness.setExile(player1, List.of(dog.getCard()));

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1)
                .allMatch(Permanent::isTapped);
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }

    @Test
    void treasureRequiresUntappingBeforeItCanProduceMana() {
        harness.enterBattlefieldAndReturn(player1, new LongBodiedGreyDog());
        resolveAllTriggers();
        Permanent treasure = findPermanent(player1, "Treasure");
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);

        harness.performUntapStep(player1);
        harness.activateAbility(player1, index, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
