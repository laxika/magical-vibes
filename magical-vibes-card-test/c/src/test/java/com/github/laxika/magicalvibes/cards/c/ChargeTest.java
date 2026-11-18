package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.m.MesaUnicorn;
import com.github.laxika.magicalvibes.cards.s.ShortSword;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Charge.class, MesaUnicorn.class, ShortSword.class})
class ChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack as INSTANT_SPELL")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new Charge()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Resolving boosts all own creatures +1/+1")
    void resolvingBoostsAllOwnCreatures() {
        harness.addToBattlefield(player1, new MesaUnicorn());
        harness.addToBattlefield(player1, new MesaUnicorn());
        harness.setHand(player1, List.of(new Charge()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(PermanentGrants.powerBoostOf(gd, p)).isEqualTo(1);
                assertThat(PermanentGrants.toughnessBoostOf(gd, p)).isEqualTo(1);
                assertThat(gqs.getEffectivePower(gd, p)).isEqualTo(3);
                assertThat(gqs.getEffectiveToughness(gd, p)).isEqualTo(3);
            }
        }
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new MesaUnicorn());
        harness.addToBattlefield(player2, new MesaUnicorn());
        harness.setHand(player1, List.of(new Charge()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        // Player1's creature is boosted
        List<Permanent> p1Battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : p1Battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(PermanentGrants.powerBoostOf(gd, p)).isEqualTo(1);
                assertThat(PermanentGrants.toughnessBoostOf(gd, p)).isEqualTo(1);
            }
        }

        // Player2's creature is NOT boosted
        List<Permanent> p2Battlefield = gd.playerBattlefields.get(player2.getId());
        for (Permanent p : p2Battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(PermanentGrants.powerBoostOf(gd, p)).isEqualTo(0);
                assertThat(PermanentGrants.toughnessBoostOf(gd, p)).isEqualTo(0);
            }
        }
    }

    @Test
    @DisplayName("Boost resets at cleanup step")
    void boostResetsAtCleanup() {
        harness.addToBattlefield(player1, new MesaUnicorn());
        harness.setHand(player1, List.of(new Charge()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(PermanentGrants.powerBoostOf(gd, p)).isEqualTo(0);
                assertThat(PermanentGrants.toughnessBoostOf(gd, p)).isEqualTo(0);
                assertThat(gqs.getEffectivePower(gd, p)).isEqualTo(2);
                assertThat(gqs.getEffectiveToughness(gd, p)).isEqualTo(2);
            }
        }
    }

    @Test
    @DisplayName("Works with empty battlefield (no crash)")
    void worksWithEmptyBattlefield() {
        harness.setHand(player1, List.of(new Charge()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the boost")
    void creaturesEnteringLaterAreNotBoosted() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new MesaUnicorn());
        harness.setHand(player1, List.of(new Charge()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);
        Permanent later = harness.enterBattlefieldAndReturn(player1, new MesaUnicorn());

        assertThat(gqs.getEffectivePower(gd, existing)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, existing)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering before resolution receive the boost")
    void creaturesEnteringBeforeResolutionAreBoosted() {
        harness.setHand(player1, List.of(new Charge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0);

        Permanent creature = harness.enterBattlefieldAndReturn(player1, new MesaUnicorn());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple Charges stack while noncreature permanents are unaffected")
    void multipleChargesStackAndIgnoreNoncreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MesaUnicorn());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new ShortSword());
        harness.setHand(player1, List.of(new Charge(), new Charge()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(PermanentGrants.powerBoostOf(gd, equipment)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, equipment)).isZero();
    }

    @Test
    @DisplayName("Charge goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new Charge()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Charge");
    }
}
