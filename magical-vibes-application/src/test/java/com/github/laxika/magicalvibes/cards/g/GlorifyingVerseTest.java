package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SpittingEarth;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlorifyingVerse.class, HillGiant.class, Pacifism.class, Shock.class, SpittingEarth.class})
class GlorifyingVerseTest extends BaseCardTest {

    @Test
    void reparteeConjuresGlorifyingVerseWhenCreatureIsTargeted() {
        harness.addToBattlefield(player1, new GlorifyingVerse());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Hill Giant"));

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Glorifying Verse");
    }

    @Test
    void reparteeDoesNotTriggerWhenPlayerIsTargeted() {
        harness.addToBattlefield(player1, new GlorifyingVerse());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void exaltedBoostsLoneAttackerUntilEndOfTurn() {
        harness.addToBattlefield(player1, new GlorifyingVerse());
        Permanent giant = addCreatureReady(player1, new HillGiant());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    void eachVerseBoostsTheLoneAttacker() {
        harness.addToBattlefield(player1, new GlorifyingVerse());
        harness.addToBattlefield(player1, new GlorifyingVerse());
        Permanent giant = addCreatureReady(player1, new HillGiant());

        declareAttackers(player1, List.of(2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(5);
    }

    @Test
    void exaltedDoesNotTriggerForMultipleAttackers() {
        harness.addToBattlefield(player1, new GlorifyingVerse());
        Permanent first = addCreatureReady(player1, new HillGiant());
        Permanent second = addCreatureReady(player1, new HillGiant());

        declareAttackers(player1, List.of(1, 2));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
    }

    @Test
    void exaltedDoesNotBoostOpponentsAttacker() {
        harness.addToBattlefield(player1, new GlorifyingVerse());
        Permanent giant = addCreatureReady(player2, new HillGiant());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    void reparteeTriggersForSorceryTargetingOpponentsCreature() {
        harness.addToBattlefield(player1, new GlorifyingVerse());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new SpittingEarth()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Hill Giant"));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Glorifying Verse");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void reparteeDoesNotTriggerForOpponentsSpell() {
        harness.addToBattlefield(player1, new GlorifyingVerse());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Hill Giant"));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void reparteeDoesNotTriggerForAuraTargetingCreature() {
        harness.addToBattlefield(player1, new GlorifyingVerse());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, harness.getPermanentId(player2, "Hill Giant"));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Pacifism");
    }

    @Test
    void eachVerseConjuresOneCardForTheSameSpell() {
        harness.addToBattlefield(player1, new GlorifyingVerse());
        harness.addToBattlefield(player1, new GlorifyingVerse());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Hill Giant"));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Glorifying Verse", "Glorifying Verse");
    }

    @Test
    void reparteeStillConjuresAfterTargetCreatureLeaves() {
        harness.addToBattlefield(player1, new GlorifyingVerse());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, giant.getId());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, giant));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Glorifying Verse");
    }
}
