package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({BreakOfDay.class, GrizzlyBears.class})
class BreakOfDayTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting puts it on the stack as INSTANT_SPELL")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new BreakOfDay()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Break of Day");
    }

    @Test
    @DisplayName("Resolving boosts all own creatures +1/+1")
    void resolvingBoostsAllOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BreakOfDay()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getPowerModifier()).isEqualTo(1);
                assertThat(p.getToughnessModifier()).isEqualTo(1);
                assertThat(p.getEffectivePower()).isEqualTo(3);
                assertThat(p.getEffectiveToughness()).isEqualTo(3);
            }
        }
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BreakOfDay()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);

        List<Permanent> p2Battlefield = gd.playerBattlefields.get(player2.getId());
        for (Permanent p : p2Battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getPowerModifier()).isEqualTo(0);
                assertThat(p.getToughnessModifier()).isEqualTo(0);
            }
        }
    }

    @Test
    @DisplayName("Boost resets at cleanup step")
    void boostResetsAtCleanup() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BreakOfDay()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getPowerModifier()).isEqualTo(0);
                assertThat(p.getToughnessModifier()).isEqualTo(0);
            }
        }
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new BreakOfDay()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Break of Day");
    }

    @Test
    @DisplayName("Fateful hour grants indestructible when at 5 life")
    void fatefulHourGrantsIndestructibleAtFiveLife() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BreakOfDay()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 5);

        harness.castAndResolveInstant(player1, 0);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getPowerModifier()).isEqualTo(1);
                assertThat(p.getToughnessModifier()).isEqualTo(1);
                assertThat(p.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
            }
        }
    }

    @Test
    @DisplayName("Fateful hour grants indestructible when below 5 life")
    void fatefulHourGrantsIndestructibleBelowFiveLife() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BreakOfDay()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 1);

        harness.castAndResolveInstant(player1, 0);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
            }
        }
    }

    @Test
    @DisplayName("Fateful hour does not grant indestructible when above 5 life")
    void fatefulHourDoesNotGrantIndestructibleAboveFiveLife() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BreakOfDay()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 6);

        harness.castAndResolveInstant(player1, 0);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getPowerModifier()).isEqualTo(1);
                assertThat(p.getToughnessModifier()).isEqualTo(1);
                assertThat(p.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
            }
        }
    }

    @Test
    @DisplayName("Fateful hour does not grant indestructible at default 20 life")
    void fatefulHourDoesNotGrantIndestructibleAtDefaultLife() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BreakOfDay()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
            }
        }
    }

    @Test
    void fatefulHourUsesLifeAtResolutionAfterLifeLoss() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BreakOfDay()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 6);

        harness.castInstant(player1, 0);
        harness.setLife(player1, 5);
        harness.passBothPriorities();

        Permanent creature = findPermanent(player1, "Grizzly Bears");
        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void fatefulHourUsesLifeAtResolutionAfterLifeGain() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BreakOfDay()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 5);

        harness.castInstant(player1, 0);
        harness.setLife(player1, 6);
        harness.passBothPriorities();

        Permanent creature = findPermanent(player1, "Grizzly Bears");
        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void grantedIndestructiblePersistsAfterLifeGainAndExpiresAtCleanup() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BreakOfDay()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 5);
        harness.castAndResolveInstant(player1, 0);

        Permanent creature = findPermanent(player1, "Grizzly Bears");
        harness.setLife(player1, 6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void creaturesEnteringLaterDoNotReceiveEitherEffect() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BreakOfDay()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 5);
        harness.castAndResolveInstant(player1, 0);
        Permanent original = findPermanent(player1, "Grizzly Bears");

        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent newcomer = gd.playerBattlefields.get(player1.getId()).getLast();

        assertThat(original.getEffectivePower()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, original, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(newcomer.getEffectivePower()).isEqualTo(2);
        assertThat(newcomer.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void fatefulHourDoesNotGrantIndestructibleToOpponentCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BreakOfDay()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 5);
        harness.setLife(player2, 5);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.INDESTRUCTIBLE)).isTrue();
        Permanent opponent = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(opponent.getEffectivePower()).isEqualTo(2);
        assertThat(opponent.getEffectiveToughness()).isEqualTo(2);
    }
}
