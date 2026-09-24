package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UginsMastery.class, Ornithopter.class, GrizzlyBears.class})
class UginsMasteryTest extends BaseCardTest {

    @Test
    void manifestsTopCardWhenYouCastAColorlessCreature() {
        harness.addToBattlefield(player1, new UginsMastery());
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested() && permanent.isFaceDown());
    }

    @Test
    void doesNotManifestWhenYouCastAColoredCreature() {
        harness.addToBattlefield(player1, new UginsMastery());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(Permanent::isManifested);
    }

    @Test
    void mayTurnOneOfYourFaceDownCreaturesFaceUpWhenAttackingPowerIsAtLeastSix() {
        harness.addToBattlefield(player1, new UginsMastery());
        Permanent otherFaceDown = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        otherFaceDown.setFaceDownAsCloaked();
        Permanent faceDown = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        faceDown.setFaceDownAsCloaked();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(3, 4, 5));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, faceDown.getId());

        assertThat(faceDown.isFaceDown()).isFalse();
        assertThat(otherFaceDown.isFaceDown()).isTrue();
    }

    @Test
    void doesNotTriggerWhenAttackingPowerIsLessThanSix() {
        harness.addToBattlefield(player1, new UginsMastery());
        Permanent faceDown = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        faceDown.setFaceDownAsCloaked();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(2, 3));

        assertThat(gd.stack).isEmpty();
        assertThat(faceDown.isFaceDown()).isTrue();
    }
}
