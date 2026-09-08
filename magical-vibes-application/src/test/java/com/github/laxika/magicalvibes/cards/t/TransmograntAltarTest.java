package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransmograntAltarTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature adds three colorless mana")
    void sacrificesCreatureForThreeColorlessMana() {
        harness.addToBattlefield(player1, new TransmograntAltar());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sorcery-speed ability creates a 3/3 colorless Zombie artifact creature")
    void createsZombieArtifactCreatureToken() {
        harness.addToBattlefield(player1, new TransmograntAltar());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(zombie.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(zombie.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(zombie.getCard().getColor()).isNull();
        assertThat(zombie.getCard().getSubtypes()).containsExactly(com.github.laxika.magicalvibes.model.CardSubtype.ZOMBIE);
        assertThat(zombie.getCard().getPower()).isEqualTo(3);
        assertThat(zombie.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Token ability cannot be activated outside your main phase")
    void tokenAbilityIsSorcerySpeedOnly() {
        harness.addToBattlefield(player1, new TransmograntAltar());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
