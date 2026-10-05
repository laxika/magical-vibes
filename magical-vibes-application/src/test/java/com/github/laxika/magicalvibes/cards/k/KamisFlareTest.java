package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NinjasKunai;
import com.github.laxika.magicalvibes.cards.s.ShortCircuit;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KamisFlare.class, GarrukWildspeaker.class, GrizzlyBears.class, NinjasKunai.class, ShortCircuit.class})
class KamisFlareTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a target planeswalker without the modified-creature bonus")
    void dealsThreeDamageWithoutModifiedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        target.setCounterCount(CounterType.LOYALTY, 5);
        prepareKamisFlare();
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Deals 2 damage to the target permanent's controller when you control a modified creature")
    void dealsBonusDamageWhenControllerHasModifiedCreature() {
        Permanent modifiedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        target.setCounterCount(CounterType.LOYALTY, 5);
        castKamisFlare(target);
        modifiedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        prepareKamisFlare();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void lethalCreatureDamageStillDealsControllerDamage() {
        Permanent modified = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareKamisFlare();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    void ownLethallyDamagedModifiedTargetStillQualifiesDuringResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        prepareKamisFlare();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void opponentsModifiedCreatureDoesNotQualify() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        prepareKamisFlare();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player2, 20);
    }

    @Test
    void countersOnNoncreatureDoNotQualify() {
        harness.addToBattlefield(player1, new GarrukWildspeaker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareKamisFlare();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    void losingModificationBeforeResolutionRemovesBonus() {
        Permanent modified = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castKamisFlare(target);
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    void illegalSoleTargetPreventsBothDamageInstructions() {
        Permanent modified = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castKamisFlare(target);
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Kami's Flare");
    }

    @Test
    void equipmentControlledByOpponentStillModifiesYourCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new NinjasKunai());
        equipment.setAttachedTo(creature.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareKamisFlare();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 18);
    }

    @Test
    void auraYouControlModifiesYourCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ShortCircuit());
        aura.setAttachedTo(creature.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareKamisFlare();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 18);
    }

    @Test
    void opponentsAuraDoesNotModifyYourCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new ShortCircuit());
        aura.setAttachedTo(creature.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareKamisFlare();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotTargetNoncreatureArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NinjasKunai());
        prepareKamisFlare();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castKamisFlare(Permanent target) {
        prepareKamisFlare();
        harness.castInstant(player1, 0, target.getId());
    }

    private void prepareKamisFlare() {
        harness.setHand(player1, List.of(new KamisFlare()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
