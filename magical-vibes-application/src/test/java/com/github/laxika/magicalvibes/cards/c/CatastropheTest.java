package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatastropheTest extends BaseCardTest {

    @Test
    @DisplayName("The land mode destroys all lands but leaves creatures intact")
    void destroysAllLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castCatastrophe(0);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Plains");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The creature mode destroys all creatures but leaves lands intact")
    void destroysAllCreatures() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castCatastrophe(1);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Plains");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creatures destroyed by the creature mode cannot be regenerated")
    void creaturesCannotBeRegenerated() {
        Permanent skeletons = harness.addToBattlefieldAndReturn(player2, new DrudgeSkeletons());
        skeletons.setRegenerationShield(1);

        castCatastrophe(1);

        harness.assertInGraveyard(player2, "Drudge Skeletons");
    }

    @Test
    @DisplayName("Choosing an invalid mode is rejected at cast time")
    void invalidModeIsRejected() {
        harness.setHand(player1, List.of(new Catastrophe()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 99))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid mode index");
    }

    private void castCatastrophe(int mode) {
        harness.setHand(player1, List.of(new Catastrophe()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castSorcery(player1, 0, mode);
        harness.passBothPriorities();
    }
}
