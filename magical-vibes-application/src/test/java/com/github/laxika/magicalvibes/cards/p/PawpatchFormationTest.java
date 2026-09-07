package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PawpatchFormation.class, AirElemental.class, GloriousAnthem.class, GrizzlyBears.class})
class PawpatchFormationTest extends BaseCardTest {

    @Test
    void destroysTargetFlyingCreature() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new PawpatchFormation()));
        addMana();

        Permanent target = findPermanent(player2, "Air Elemental");
        harness.castInstant(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    void destroysTargetEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new PawpatchFormation()));
        addMana();

        Permanent target = findPermanent(player2, "Glorious Anthem");
        harness.castInstant(player1, 0, 1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    void drawsCardAndCreatesFood() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new PawpatchFormation()));
        addMana();

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    void flyingCreatureModeRejectsNonFlyingCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        harness.setHand(player1, List.of(new PawpatchFormation()));
        addMana();

        Permanent target = findPermanent(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
