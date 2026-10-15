package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IncubatorDrone.class})
class IncubatorDroneTest extends BaseCardTest {

    @Test
    @DisplayName("When Incubator Drone enters, it creates an Eldrazi Scion token")
    void enteringCreatesEldraziScion() {
        castIncubatorDrone();

        Permanent scion = findPermanents(player1, "Eldrazi Scion").getFirst();
        assertThat(scion.getCard().isToken()).isTrue();
        assertThat(scion.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(scion.getCard().getColor()).isNull();
        assertThat(scion.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.SCION);
        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Eldrazi Scion token can be sacrificed for colorless mana")
    void scionCanBeSacrificedForColorlessMana() {
        castIncubatorDrone();

        Permanent scion = findPermanents(player1, "Eldrazi Scion").getFirst();
        int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);
        harness.activateAbility(player1, scionIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
    }

    @Test
    @DisplayName("The Scion is created only when the enter trigger resolves")
    void tokenCreationUsesTheStack() {
        harness.setHand(player1, List.of(new IncubatorDrone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);

        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Incubator Drone")).hasSize(1);
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
        assertThat(findPermanents(player2, "Eldrazi Scion")).isEmpty();
    }

    @Test
    @DisplayName("A tapped Scion can be sacrificed immediately without using the stack")
    void tappedScionCanProduceManaImmediately() {
        castIncubatorDrone();
        Permanent scion = findPermanents(player1, "Eldrazi Scion").getFirst();
        scion.tap();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(scion),
                0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        assertThat(findPermanents(player1, "Incubator Drone")).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private void castIncubatorDrone() {
        harness.setHand(player1, List.of(new IncubatorDrone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
