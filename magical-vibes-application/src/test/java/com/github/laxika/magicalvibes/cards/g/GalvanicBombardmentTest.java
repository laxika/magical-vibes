package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GalvanicBombardment.class, AvatarOfMight.class, FountainOfYouth.class})
class GalvanicBombardmentTest extends BaseCardTest {

    @Test
    void dealsTwoDamageWithNoCopiesInControllerGraveyard() {
        Permanent target = addCreatureReady(player2, new AvatarOfMight());

        castBombardment(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void dealsAdditionalDamageForEachCopyInControllerGraveyard() {
        Permanent target = addCreatureReady(player2, new AvatarOfMight());
        harness.setGraveyard(player1, List.of(new GalvanicBombardment(), new GalvanicBombardment()));

        castBombardment(target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void countsOnlyCopiesInControllerGraveyard() {
        Permanent target = addCreatureReady(player2, new AvatarOfMight());
        harness.setGraveyard(player2, List.of(new GalvanicBombardment(), new GalvanicBombardment()));

        castBombardment(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void resolvingCopyDoesNotCountItself() {
        Permanent target = addCreatureReady(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new GalvanicBombardment(), new GalvanicBombardment()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new GalvanicBombardment()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ignoresDifferentlyNamedCardsInControllerGraveyard() {
        Permanent target = addCreatureReady(player2, new AvatarOfMight());
        harness.setGraveyard(player1, List.of(new GalvanicBombardment(), new FountainOfYouth()));

        castBombardment(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void countsCopiesAddedToGraveyardAfterCasting() {
        Permanent target = addCreatureReady(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new GalvanicBombardment()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());

        harness.setGraveyard(player1, List.of(new GalvanicBombardment(), new GalvanicBombardment()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void doesNotCountCopiesRemovedFromGraveyardAfterCasting() {
        Permanent target = addCreatureReady(player2, new AvatarOfMight());
        harness.setGraveyard(player1, List.of(new GalvanicBombardment(), new GalvanicBombardment()));
        harness.setHand(player1, List.of(new GalvanicBombardment()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new GalvanicBombardment()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castBombardment(Permanent target) {
        harness.setHand(player1, List.of(new GalvanicBombardment()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
