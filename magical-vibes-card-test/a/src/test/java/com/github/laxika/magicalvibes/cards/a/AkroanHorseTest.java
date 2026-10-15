package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.v.VoyagesEnd;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkroanHorse.class, VoyagesEnd.class})
class AkroanHorseTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives Akroan Horse to an opponent")
    void etbGivesControlToOpponent() {
        harness.setHand(player1, List.of(new AkroanHorse()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Akroan Horse");
        harness.assertOnBattlefield(player2, "Akroan Horse");
    }

    @Test
    @DisplayName("At its controller's upkeep each opponent creates a 1/1 white Soldier")
    void eachOpponentCreatesSoldier() {
        harness.addToBattlefield(player1, new AkroanHorse());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
        List<Permanent> soldiers = findPermanents(player2, "Soldier");
        assertThat(soldiers).hasSize(1);
        assertThat(soldiers.getFirst().getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(soldiers.getFirst().getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
    }

    @Test
    @DisplayName("After the ETB transfer, the new controller's upkeep creates a Soldier for the original controller")
    void transferredHorseCreatesSoldierForOriginalController() {
        harness.setHand(player1, List.of(new AkroanHorse()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
        Permanent soldier = findPermanent(player1, "Soldier");
        assertThat(soldier.getCard().isToken()).isTrue();
        assertThat(soldier.getCard().getPower()).isEqualTo(1);
        assertThat(soldier.getCard().getToughness()).isEqualTo(1);
        assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(soldier.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
        assertThat(findPermanents(player2, "Soldier")).isEmpty();
        harness.assertOnBattlefield(player2, "Akroan Horse");
    }

    @Test
    @DisplayName("The Horse does not trigger during its opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new AkroanHorse());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
        assertThat(findPermanents(player2, "Soldier")).isEmpty();
    }

    @Test
    @DisplayName("The upkeep trigger creates its Soldier even if the Horse leaves the battlefield")
    void upkeepTriggerResolvesWithoutHorse() {
        harness.addToBattlefield(player1, new AkroanHorse());
        harness.setHand(player1, List.of(new VoyagesEnd()));
        harness.setLibrary(player1, List.of());
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Akroan Horse"));
        harness.assertNotOnBattlefield(player1, "Akroan Horse");
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Soldier")).hasSize(1);
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }
}
