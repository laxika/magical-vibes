package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.k.KrumarInitiate;
import com.github.laxika.magicalvibes.cards.k.KinTreeSeverance;
import com.github.laxika.magicalvibes.cards.s.SarkhansResolve;
import com.github.laxika.magicalvibes.cards.s.SeizeOpportunity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlameholdGrappler.class, KrumarInitiate.class, KinTreeSeverance.class,
        SarkhansResolve.class, SeizeOpportunity.class})
class FlameholdGrapplerTest extends BaseCardTest {

    @Test
    @DisplayName("Copies the next creature spell as a token")
    void copiesNextCreatureSpellAsToken() {
        harness.castFromHand(player1, new FlameholdGrappler(), "{U}{R}{W}");
        resolveAllTriggers();
        harness.castFromHand(player1, new KrumarInitiate(), "{1}{B}");
        resolveAllTriggers();

        List<Card> creatures = gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getCard())
                .filter(card -> "Krumar Initiate".equals(card.getName()))
                .toList();
        assertThat(creatures).hasSize(2);
        assertThat(creatures).anyMatch(Card::isToken);
        assertThat(creatures).filteredOn(card -> !card.isToken()).hasSize(1);
    }

    @Test
    void copiesOnlyTheNextSpell() {
        harness.castFromHand(player1, new FlameholdGrappler(), "{U}{R}{W}");
        resolveAllTriggers();
        harness.castFromHand(player1, new KrumarInitiate(), "{1}{B}");
        resolveAllTriggers();
        harness.castFromHand(player1, new KrumarInitiate(), "{1}{B}");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> "Krumar Initiate".equals(p.getCard().getName())).hasSize(3);
    }

    @Test
    void mayRetargetCopyWithoutChangingOriginal() {
        harness.castFromHand(player1, new FlameholdGrappler(), "{U}{R}{W}");
        resolveAllTriggers();
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new KrumarInitiate());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player2, new KrumarInitiate());
        harness.setHand(player1, List.of(new SarkhansResolve()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, 0, originalTarget.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        resolveAllTriggers();

        assertThat(originalTarget.getPowerModifier()).isEqualTo(3);
        assertThat(copyTarget.getPowerModifier()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Sarkhan's Resolve");
    }

    @Test
    void mayKeepOriginalTargetForCopy() {
        harness.castFromHand(player1, new FlameholdGrappler(), "{U}{R}{W}");
        resolveAllTriggers();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KrumarInitiate());
        harness.setHand(player1, List.of(new SarkhansResolve()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, 0, target.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(target.getPowerModifier()).isEqualTo(6);
        assertThat(target.getToughnessModifier()).isEqualTo(6);
    }

    @Test
    void spellCastBeforeEntersTriggerResolvesDoesNotConsumeCopy() {
        harness.castFromHand(player1, new FlameholdGrappler(), "{U}{R}{W}");
        harness.passBothPriorities();
        Permanent target = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new SarkhansResolve()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, 0, target.getId());
        resolveAllTriggers();
        assertThat(target.getPowerModifier()).isEqualTo(3);

        harness.castFromHand(player1, new KrumarInitiate(), "{1}{B}");
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> "Krumar Initiate".equals(p.getCard().getName())).hasSize(2);
    }

    @Test
    void opponentsSpellDoesNotConsumeCopy() {
        harness.castFromHand(player1, new FlameholdGrappler(), "{U}{R}{W}");
        resolveAllTriggers();
        Permanent target = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player2, List.of(new SarkhansResolve()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, 0, target.getId());
        resolveAllTriggers();
        assertThat(target.getPowerModifier()).isEqualTo(3);

        harness.castFromHand(player1, new KrumarInitiate(), "{1}{B}");
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> "Krumar Initiate".equals(p.getCard().getName())).hasSize(2);
    }

    @Test
    void unusedCopyExpiresAtEndOfTurn() {
        harness.castFromHand(player1, new FlameholdGrappler(), "{U}{R}{W}");
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new KrumarInitiate(), "{1}{B}");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> "Krumar Initiate".equals(p.getCard().getName())).hasSize(1);
    }

    @Test
    void copiedGrapplerRegistersItsOwnEntersTrigger() {
        harness.castFromHand(player1, new FlameholdGrappler(), "{U}{R}{W}");
        resolveAllTriggers();
        harness.castFromHand(player1, new FlameholdGrappler(), "{U}{R}{W}");
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> "Flamehold Grappler".equals(p.getCard().getName())).hasSize(3);

        harness.castFromHand(player1, new KrumarInitiate(), "{1}{B}");
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> "Krumar Initiate".equals(p.getCard().getName())).hasSize(3);
    }

    @Test
    void mayRetargetCopyWhenOnlyOneOfUpToTwoTargetsWasChosen() {
        harness.castFromHand(player1, new FlameholdGrappler(), "{U}{R}{W}");
        resolveAllTriggers();
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new KrumarInitiate());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player2, new KrumarInitiate());
        harness.setHand(player1, List.of(new SeizeOpportunity()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castModalInstant(player1, 0, 1, List.of(originalTarget.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        resolveAllTriggers();
        assertThat(originalTarget.getPowerModifier()).isEqualTo(2);
        assertThat(originalTarget.getToughnessModifier()).isEqualTo(1);
        assertThat(copyTarget.getPowerModifier()).isEqualTo(2);
        assertThat(copyTarget.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    void delayedCopySurvivesGrapplerLeavingBattlefield() {
        harness.castFromHand(player1, new FlameholdGrappler(), "{U}{R}{W}");
        resolveAllTriggers();
        Permanent grappler = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player2, List.of(new KinTreeSeverance()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, grappler.getId());
        harness.assertNotOnBattlefield(player1, "Flamehold Grappler");

        harness.castFromHand(player1, new KrumarInitiate(), "{1}{B}");
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> "Krumar Initiate".equals(p.getCard().getName())).hasSize(2);
    }

    @Test
    void spellWithZeroChosenTargetsStillConsumesCopy() {
        harness.castFromHand(player1, new FlameholdGrappler(), "{U}{R}{W}");
        resolveAllTriggers();
        harness.setHand(player1, List.of(new SeizeOpportunity()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castModalInstant(player1, 0, 1, List.of());
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Seize Opportunity");

        harness.castFromHand(player1, new KrumarInitiate(), "{1}{B}");
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> "Krumar Initiate".equals(p.getCard().getName())).hasSize(1);
    }
}
