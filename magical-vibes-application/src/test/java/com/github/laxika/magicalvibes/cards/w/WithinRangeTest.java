package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WithinRange.class, GrizzlyBears.class, SwordsToPlowshares.class})
class WithinRangeTest extends BaseCardTest {

    @Test
    @DisplayName("When Within Range enters, it creates two Warrior tokens")
    void enteringCreatesTwoWarriorTokens() {
        castWithinRange();

        List<Permanent> warriors = findPermanents(player1, "Warrior");
        assertThat(warriors).hasSize(2);
        assertThat(warriors).allMatch(warrior -> warrior.getCard().isToken());
    }

    @Test
    @DisplayName("Each opponent loses life equal to the number of creatures attacking them")
    void attackCausesLifeLossForEachAttackingCreature() {
        castWithinRange();
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker)));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    private void castWithinRange() {
        harness.castFromHand(player1, new WithinRange(), "{3}{B}");
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Life loss counts attackers remaining when the ability resolves")
    void removedAttackerDoesNotCountForLifeLoss() {
        castWithinRange();
        List<Permanent> warriors = findPermanents(player1, "Warrior");
        warriors.forEach(warrior -> warrior.setSummoningSick(false));
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(warriors.stream()
                    .map(warrior -> gd.playerBattlefields.get(player1.getId()).indexOf(warrior)).toList());
            harness.castAndResolveInstant(player2, 0, warriors.getFirst().getId());
            resolveAllTriggers();

            harness.assertLife(player2, 19);
            harness.assertLife(player1, 21);
            assertThat(findPermanents(player1, "Warrior")).hasSize(1);
        });
    }

    @Test
    @DisplayName("An attack whose only attacker is exiled causes no life loss")
    void noLifeLossWhenAllAttackersLeaveCombat() {
        castWithinRange();
        Permanent warrior = findPermanents(player1, "Warrior").getFirst();
        warrior.setSummoningSick(false);
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(warrior)));
            harness.castAndResolveInstant(player2, 0, warrior.getId());
            resolveAllTriggers();

            harness.assertLife(player2, 20);
            harness.assertLife(player1, 21);
        });
    }
}
