package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Drownyard Temple")
class DrownyardTempleTest extends BaseCardTest {

    @Test
    @DisplayName("Can tap for colorless mana")
    void canTapForColorlessMana() {
        harness.addToBattlefield(player1, new DrownyardTemple());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns itself from the graveyard to the battlefield tapped")
    void returnsItselfFromGraveyardTapped() {
        DrownyardTemple temple = new DrownyardTemple();
        harness.setGraveyard(player1, List.of(temple));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent permanent = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Drownyard Temple"))
                .findFirst()
                .orElseThrow();
        assertThat(permanent.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Drownyard Temple");
    }

    @Test
    @DisplayName("Pays three colorless mana for the graveyard ability")
    void paysGraveyardAbilityCost() {
        harness.setGraveyard(player1, List.of(new DrownyardTemple()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate the graveyard ability without three mana")
    void cannotActivateWithoutEnoughMana() {
        harness.setGraveyard(player1, List.of(new DrownyardTemple()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
