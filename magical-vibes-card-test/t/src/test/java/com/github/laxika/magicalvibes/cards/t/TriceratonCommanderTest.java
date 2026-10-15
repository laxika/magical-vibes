package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FrogButler;
import com.github.laxika.magicalvibes.cards.z.ZogTriceratonCastaway;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TriceratonCommander.class, ZogTriceratonCastaway.class, FrogButler.class})
class TriceratonCommanderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X 2/2 white Dinosaur Soldier tokens")
    void entersWithXTokens() {
        harness.setHand(player1, List.of(new TriceratonCommander()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0, 2);
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Dinosaur Soldier");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.DINOSAUR, CardSubtype.SOLDIER);
        });
    }

    @Test
    @DisplayName("Attacking boosts and grants flying to other Dinosaurs only")
    void attackingBoostsOtherDinosaurs() {
        Permanent commander = addCreatureReady(player1, new TriceratonCommander());
        Permanent dinosaur = addCreatureReady(player1, new ZogTriceratonCastaway());
        Permanent nonDinosaur = addCreatureReady(player1, new FrogButler());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, commander)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, dinosaur)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, dinosaur)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nonDinosaur)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nonDinosaur)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, nonDinosaur, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dinosaur)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, dinosaur)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Casting with X zero creates no tokens")
    void zeroXCreatesNoTokens() {
        harness.setHand(player1, List.of(new TriceratonCommander()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Triceraton Commander")).isEqualTo(1);
        assertThat(findPermanents(player1, "Dinosaur Soldier")).isEmpty();
        assertThat(findPermanents(player2, "Dinosaur Soldier")).isEmpty();
    }

    @Test
    @DisplayName("Attack trigger boosts its tokens and another Commander but not opposing or later Dinosaurs")
    void attackAffectsOnlyDinosaursControlledAtResolution() {
        harness.setHand(player1, List.of(new TriceratonCommander()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0, 1);
        resolveAllTriggers();
        Permanent commander = findPermanent(player1, "Triceraton Commander");
        commander.setSummoningSick(false);
        Permanent token = findPermanent(player1, "Dinosaur Soldier");
        Permanent otherCommander = addCreatureReady(player1, new TriceratonCommander());
        Permanent opposingDinosaur = addCreatureReady(player2, new ZogTriceratonCastaway());

        declareAttackers(List.of(0));
        Permanent dinosaurBeforeResolution = addCreatureReady(player1, new ZogTriceratonCastaway());
        resolveAllTriggers();
        Permanent dinosaurAfterResolution = addCreatureReady(player1, new TriceratonCommander());

        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, commander)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, otherCommander)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherCommander)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, dinosaurBeforeResolution)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, dinosaurBeforeResolution)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, dinosaurBeforeResolution, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingDinosaur)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opposingDinosaur)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, opposingDinosaur, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, dinosaurAfterResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dinosaurAfterResolution)).isEqualTo(2);
    }
}
