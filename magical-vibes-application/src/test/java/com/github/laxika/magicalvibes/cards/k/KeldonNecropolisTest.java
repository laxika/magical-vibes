package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.n.NomadicElf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeldonNecropolis.class, NomadicElf.class})
class KeldonNecropolisTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana")
    void tappingAddsColorlessMana() {
        harness.addToBattlefield(player1, new KeldonNecropolis());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing a creature deals 2 damage to a player")
    void sacrificesCreatureToDamagePlayer() {
        harness.addToBattlefield(player1, new KeldonNecropolis());
        harness.addToBattlefield(player1, new NomadicElf());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(findPermanent(player1, "Keldon Necropolis").isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Nomadic Elf");
        harness.assertOnBattlefield(player1, "Keldon Necropolis");
    }

    @Test
    @DisplayName("Sacrificing a creature deals 2 damage to a creature")
    void sacrificesCreatureToDamageCreature() {
        harness.addToBattlefield(player1, new KeldonNecropolis());
        harness.addToBattlefield(player1, new NomadicElf());
        harness.addToBattlefield(player2, new NomadicElf());
        UUID targetId = harness.getPermanentId(player2, "Nomadic Elf");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nomadic Elf");
        harness.assertInGraveyard(player2, "Nomadic Elf");
    }

    @Test
    @DisplayName("The sacrifice, mana, and tap costs are paid before damage resolves")
    void paysCostsBeforeResolution() {
        harness.addToBattlefield(player1, new KeldonNecropolis());
        harness.addToBattlefield(player1, new NomadicElf());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());

        harness.assertInGraveyard(player1, "Nomadic Elf");
        harness.assertNotOnBattlefield(player1, "Nomadic Elf");
        assertThat(findPermanent(player1, "Keldon Necropolis").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotActivateWithoutControlledCreature() {
        harness.addToBattlefield(player1, new KeldonNecropolis());
        harness.addToBattlefield(player2, new NomadicElf());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Nomadic Elf");
        assertThat(findPermanent(player1, "Keldon Necropolis").isTapped()).isFalse();
    }

    @Test
    @DisplayName("The targeted creature may be sacrificed to pay the cost")
    void canSacrificeTheTargetedCreature() {
        harness.addToBattlefield(player1, new KeldonNecropolis());
        harness.addToBattlefield(player1, new NomadicElf());
        UUID targetId = harness.getPermanentId(player1, "Nomadic Elf");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, targetId);

        harness.assertInGraveyard(player1, "Nomadic Elf");
        harness.assertNotOnBattlefield(player1, "Nomadic Elf");

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Keldon Necropolis");
    }
}
