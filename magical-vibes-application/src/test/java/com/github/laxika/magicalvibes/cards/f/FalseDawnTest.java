package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BattlefieldForge;
import com.github.laxika.magicalvibes.cards.b.BirgiGodOfStorytelling;
import com.github.laxika.magicalvibes.cards.d.DegaDisciple;
import com.github.laxika.magicalvibes.cards.g.GoblinLegionnaire;
import com.github.laxika.magicalvibes.cards.h.HarnfelHornOfBounty;
import com.github.laxika.magicalvibes.cards.j.Jilt;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.ReflectingPool;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FalseDawn.class, BattlefieldForge.class, DegaDisciple.class, Jilt.class,
        GoblinLegionnaire.class, BirgiGodOfStorytelling.class, HarnfelHornOfBounty.class,
        Mountain.class, ReflectingPool.class})
class FalseDawnTest extends BaseCardTest {

    @Test
    @DisplayName("Turns colored mana from your abilities white and lets you spend it as any color")
    void replacesManaAndAllowsWhiteAsAnyColor() {
        harness.addToBattlefield(player1, new BattlefieldForge());
        harness.addToBattlefield(player1, new BattlefieldForge());
        var target = harness.addToBattlefieldAndReturn(player2, new DegaDisciple());
        harness.setHand(player1, List.of(new FalseDawn(), new Jilt()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Dega Disciple");
        harness.assertInHand(player2, "Dega Disciple");
    }

    @Test
    @DisplayName("Does not replace colorless mana")
    void leavesColorlessManaUnchanged() {
        harness.addToBattlefield(player1, new BattlefieldForge());
        harness.setHand(player1, List.of(new FalseDawn()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Replaces colored mana only from spells and abilities you control")
    void doesNotReplaceOpponentsColoredMana() {
        harness.addToBattlefield(player2, new BattlefieldForge());
        harness.setHand(player1, List.of(new FalseDawn()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.activateAbility(player2, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Only white mana gains the any-color spending permission")
    void doesNotMakeOtherManaAnyColor() {
        var target = harness.addToBattlefieldAndReturn(player2, new DegaDisciple());
        harness.setHand(player1, List.of(new FalseDawn(), new Jilt()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void existingColoredManaCanPayItsOwnRequirementWithoutConsumingRequiredWhite() {
        harness.setHand(player1, List.of(new FalseDawn(), new GoblinLegionnaire()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goblin Legionnaire");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void drawsExactlyOneCard() {
        var drawn = new DegaDisciple();
        var remaining = new Jilt();
        harness.setHand(player1, List.of(new FalseDawn()));
        harness.setLibrary(player1, List.of(drawn, remaining));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }

    @Test
    void whiteManaPaysColoredActivatedAbilityCosts() {
        var disciple = addCreatureReady(player1, new DegaDisciple());
        harness.setHand(player1, List.of(new FalseDawn()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.activateAbility(player1, 0, 1, null, disciple.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, disciple)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void replacementAndSpendingPermissionExpireAtEndOfTurn() {
        harness.addToBattlefield(player1, new BattlefieldForge());
        var target = harness.addToBattlefieldAndReturn(player2, new DegaDisciple());
        harness.setHand(player1, List.of(new FalseDawn(), new Jilt()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        harness.addMana(player1, ManaColor.WHITE, 1);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({BirgiGodOfStorytelling.class, HarnfelHornOfBounty.class})
    void persistentManaFromControlledTriggersIsAlsoReplacedWithWhite() {
        harness.setHand(player1, List.of(new FalseDawn(), new DegaDisciple()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.addToBattlefield(player1, new BirgiGodOfStorytelling());

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @CardUsed({ReflectingPool.class, Mountain.class})
    void replacesManaWhenOnlyOneLandManaTypeIsAvailable() {
        harness.addToBattlefield(player1, new ReflectingPool());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new FalseDawn()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }
}
