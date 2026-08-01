package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EyeOfSingularityTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys duplicate-named non-basic permanents and spares unique ones")
    void etbDestroysDuplicates() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new AngelOfMercy());

        harness.setHand(player1, List.of(new EyeOfSingularity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve Eye
        harness.passBothPriorities(); // resolve ETB destroy-duplicates
        // Eye's own enters also queues destroy-others-with-name (no other Eyes)
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Angel of Mercy");
        harness.assertOnBattlefield(player1, "Eye of Singularity");
    }

    @Test
    @DisplayName("ETB does not destroy basic lands that share a name")
    void etbSparesBasicLands() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Plains());

        harness.setHand(player1, List.of(new EyeOfSingularity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertOnBattlefield(player1, "Plains");
        harness.assertOnBattlefield(player2, "Plains");
        assertThat(findPermanents(player1, "Plains")).hasSize(2);
    }

    @Test
    @DisplayName("Non-basic permanent entering destroys other permanents with that name")
    void enteringDestroysOthersWithSameName() {
        harness.addToBattlefield(player1, new EyeOfSingularity());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve Eye trigger

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Basic land entering does not trigger destroy-others")
    void basicLandEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new EyeOfSingularity());
        harness.addToBattlefield(player2, new Plains());

        harness.setHand(player1, List.of(new Plains()));
        harness.playLand(player1, 0);

        harness.assertOnBattlefield(player1, "Plains");
        harness.assertOnBattlefield(player2, "Plains");
        assertThat(gd.stack).isEmpty();
    }
}
