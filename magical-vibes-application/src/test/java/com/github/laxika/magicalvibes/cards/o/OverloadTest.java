package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CallousGiant;
import com.github.laxika.magicalvibes.cards.c.ChromaticSphere;
import com.github.laxika.magicalvibes.cards.d.DrakeSkullCameo;
import com.github.laxika.magicalvibes.cards.d.DromarsAttendant;
import com.github.laxika.magicalvibes.cards.p.PlanarPortal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Overload.class, ChromaticSphere.class, DrakeSkullCameo.class,
        DromarsAttendant.class, PlanarPortal.class, CallousGiant.class})
class OverloadTest extends BaseCardTest {

    @Test
    void destroysArtifactWithManaValueTwoOrLess() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player2, new ChromaticSphere());
        harness.setHand(player1, List.of(new Overload()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Chromatic Sphere"));

        harness.assertNotOnBattlefield(player2, "Chromatic Sphere");
        harness.assertInGraveyard(player2, "Chromatic Sphere");
    }

    @Test
    void cannotTargetArtifactWithManaValueAboveTwoWithoutKicker() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player2, new DrakeSkullCameo());
        harness.setHand(player1, List.of(new Overload()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Drake-Skull Cameo")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value 2 or less");
    }

    @Test
    void cannotTargetNonArtifactWithKicker() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player2, new CallousGiant());
        harness.setHand(player1, List.of(new Overload()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0,
                harness.getPermanentId(player2, "Callous Giant")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void kickedOverloadDestroysArtifactWithManaValueFive() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player2, new DromarsAttendant());
        harness.setHand(player1, List.of(new Overload()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castKickedInstant(player1, 0,
                harness.getPermanentId(player2, "Dromar's Attendant"));

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Dromar's Attendant");
        harness.assertInGraveyard(player2, "Dromar's Attendant");
    }

    @Test
    void kickedOverloadCannotTargetArtifactWithManaValueAboveFive() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player2, new PlanarPortal());
        harness.setHand(player1, List.of(new Overload()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0,
                harness.getPermanentId(player2, "Planar Portal")))
                .isInstanceOf(IllegalStateException.class);
    }
}
