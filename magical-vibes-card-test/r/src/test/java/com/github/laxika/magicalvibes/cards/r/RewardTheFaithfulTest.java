package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DelverOfSecrets;
import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.cards.s.Stabilizer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RewardTheFaithful.class, GoblinBrigand.class, ScornfulEgotist.class,
        Stabilizer.class, DelverOfSecrets.class})
class RewardTheFaithfulTest extends BaseCardTest {

    @Test
    void eachTargetedPlayerGainsGreatestManaValueAmongControllerPermanents() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 12);
        harness.addToBattlefield(player1, new GoblinBrigand());
        harness.addToBattlefield(player2, new ScornfulEgotist());
        harness.setHand(player1, List.of(new RewardTheFaithful()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, List.of(player1.getId(), player2.getId()));

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    @Test
    void evaluatesGreatestManaValueAtResolution() {
        harness.setLife(player2, 10);
        harness.addToBattlefield(player1, new ScornfulEgotist());
        harness.setHand(player1, List.of(new RewardTheFaithful()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, List.of(player2.getId()));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.addToBattlefield(player1, new GoblinBrigand());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
    }

    @Test
    void canBeCastWithNoTargets() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new RewardTheFaithful()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, List.of());

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void targetedPlayersGainNoLifeWhenControllerHasNoPermanents() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 12);
        harness.setHand(player1, List.of(new RewardTheFaithful()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, List.of(player1.getId(), player2.getId()));

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
    }

    @Test
    void usesGreatestManaValueRatherThanSumOfControlledPermanents() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new GoblinBrigand());
        harness.addToBattlefield(player1, new ScornfulEgotist());
        harness.setHand(player1, List.of(new RewardTheFaithful()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, List.of(player1.getId()));

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void includesNoncreaturePermanentsInGreatestManaValue() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new Stabilizer());
        harness.setHand(player1, List.of(new RewardTheFaithful()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, List.of(player1.getId()));

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
    }

    @Test
    void faceDownCreatureHasZeroManaValueUntilTurnedFaceUp() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new ScornfulEgotist(),
                new RewardTheFaithful(), new RewardTheFaithful()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(player1.getId()));

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, 0);
        harness.castAndResolveInstant(player1, 0, List.of(player1.getId()));

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void transformedPermanentRetainsFrontFaceManaValue() {
        harness.setLife(player1, 10);
        var delver = harness.addToBattlefieldAndReturn(player1, new DelverOfSecrets());
        harness.setLibrary(player1, List.of(new RewardTheFaithful()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(delver.isTransformed()).isTrue();

        harness.setHand(player1, List.of(new RewardTheFaithful()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, List.of(player1.getId()));

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
    }
}
