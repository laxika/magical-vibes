package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AladdinsRing;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.k.KeeningStone;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.w.WallOfStone;
import com.github.laxika.magicalvibes.cards.w.Wiretapping;
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

@CardUsed({UnleashTheInferno.class, AladdinsRing.class, ChandraNalaar.class, DarksteelRelic.class,
        FountainOfYouth.class, KeeningStone.class, RagingGoblin.class, WallOfStone.class, Wiretapping.class})
class UnleashTheInfernoTest extends BaseCardTest {

    @Test
    @DisplayName("Deals excess damage and destroys an eligible artifact")
    void destroysArtifactForExcessDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        Permanent eligibleArtifact = harness.addToBattlefieldAndReturn(player2, new KeeningStone());
        Permanent ineligibleArtifact = harness.addToBattlefieldAndReturn(player2, new AladdinsRing());

        cast(creature);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ineligibleArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, eligibleArtifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Raging Goblin");
        harness.assertNotOnBattlefield(player2, "Keening Stone");
        harness.assertOnBattlefield(player2, "Aladdin's Ring");
    }

    @Test
    @DisplayName("Does not create the reflexive ability without excess damage")
    void doesNotDestroyArtifactWithoutExcessDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WallOfStone());
        harness.addToBattlefield(player2, new DarksteelRelic());

        castAndResolve(creature);

        assertThat(creature.getMarkedDamage()).isEqualTo(7);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Darksteel Relic");
    }

    @Test
    @DisplayName("Counts excess damage over a planeswalker's remaining loyalty")
    void countsPlaneswalkerExcessDamage() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 2);
        Permanent eligibleArtifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent ineligibleArtifact = harness.addToBattlefieldAndReturn(player2, new KeeningStone());

        cast(planeswalker);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ineligibleArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, eligibleArtifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Chandra Nalaar");
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertOnBattlefield(player2, "Keening Stone");
    }

    @Test
    @DisplayName("Only accepts a creature or planeswalker as the damage target")
    void rejectsOtherDamageTargets() {
        harness.setHand(player1, List.of(new UnleashTheInferno()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void destroysEnchantmentButRejectsOwnArtifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new Wiretapping());

        cast(creature);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wiretapping");
        harness.assertOnBattlefield(player1, "Fountain of Youth");
    }

    @Test
    void countsPreviouslyMarkedDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WallOfStone());
        creature.setMarkedDamage(7);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new KeeningStone());

        cast(creature);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wall of Stone");
        harness.assertNotOnBattlefield(player2, "Keening Stone");
    }

    @Test
    void exactLethalDamageDoesNotTriggerEvenForZeroManaValueArtifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WallOfStone());
        creature.setMarkedDamage(1);
        harness.addToBattlefield(player2, new FountainOfYouth());

        castAndResolve(creature);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Wall of Stone");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    void shieldCounterPreventsDamageAndReflexiveTrigger() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        creature.setCounterCount(CounterType.SHIELD, 1);
        harness.addToBattlefield(player2, new KeeningStone());

        castAndResolve(creature);

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Raging Goblin");
        harness.assertOnBattlefield(player2, "Keening Stone");
    }

    @Test
    void excessDamageWithNoEligibleTargetFinishesResolving() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        harness.addToBattlefield(player2, new AladdinsRing());

        castAndResolve(creature);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Raging Goblin");
        harness.assertOnBattlefield(player2, "Aladdin's Ring");
    }

    @Test
    void indestructibleArtifactIsLegalButSurvives() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DarksteelRelic());

        cast(creature);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Darksteel Relic");
        assertThat(gd.stack).isEmpty();
    }

    private void cast(Permanent target) {
        castAndResolve(target);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    private void castAndResolve(Permanent target) {
        harness.setHand(player1, List.of(new UnleashTheInferno()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
