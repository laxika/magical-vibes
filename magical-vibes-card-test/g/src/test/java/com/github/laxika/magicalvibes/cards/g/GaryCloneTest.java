package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WatchfulRadstag;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GaryClone.class, WatchfulRadstag.class})
class GaryCloneTest extends BaseCardTest {

    @Test
    @DisplayName("Squad creates one token copy for each additional payment")
    void squadCreatesTokenCopies() {
        castGaryClone(List.of("{2}", "{2}"));
        resolveAllTriggers();

        List<Permanent> clones = findPermanents(player1, "Gary Clone");
        assertThat(clones).hasSize(3);
        assertThat(clones).filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
    }

    @Test
    @DisplayName("Attacking boosts each Gary Clone you control until end of turn")
    void attackingBoostsEachGaryCloneYouControl() {
        Permanent attacker = addCreatureReady(player1, new GaryClone());
        Permanent otherClone = addCreatureReady(player1, new GaryClone());
        Permanent radstag = addCreatureReady(player1, new WatchfulRadstag());
        Permanent opponentClone = addCreatureReady(player2, new GaryClone());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(otherClone.getPowerModifier()).isEqualTo(1);
        assertThat(radstag.getPowerModifier()).isZero();
        assertThat(opponentClone.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Casting without squad payments creates no tokens")
    void noSquadPaymentsCreateNoTokens() {
        castGaryClone(List.of());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Gary Clone")).hasSize(1)
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("A squad token retains the attack ability without copying squad payments")
    void squadTokenCanAttackAndBoostAllClones() {
        castGaryClone(List.of("{2}"));
        resolveAllTriggers();
        List<Permanent> clones = findPermanents(player1, "Gary Clone");
        assertThat(clones).hasSize(2);
        Permanent token = clones.stream().filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        token.setSummoningSick(false);
        addCreatureReady(player2, new GaryClone());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(token)));
        resolveAllTriggers();

        assertThat(clones).allSatisfy(permanent -> {
            assertThat(permanent.getPowerModifier()).isEqualTo(1);
            assertThat(permanent.getToughnessModifier()).isZero();
        });
        assertThat(findPermanents(player1, "Gary Clone")).hasSize(2);
    }

    @Test
    @DisplayName("Each attacking Gary Clone adds a separate boost that expires at end of turn")
    void multipleAttackTriggersStackAndExpire() {
        Permanent first = addCreatureReady(player1, new GaryClone());
        Permanent second = addCreatureReady(player1, new GaryClone());
        Permanent nonattacker = addCreatureReady(player1, new GaryClone());
        addCreatureReady(player2, new GaryClone());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        for (Permanent clone : List.of(first, second, nonattacker)) {
            assertThat(clone.getPowerModifier()).isEqualTo(2);
            assertThat(clone.getToughnessModifier()).isZero();
        }

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(player2, TurnStep.UPKEEP);

        for (Permanent clone : List.of(first, second, nonattacker)) {
            assertThat(clone.getPowerModifier()).isZero();
            assertThat(clone.getToughnessModifier()).isZero();
        }
    }

    private void castGaryClone(List<String> repeatedAdditionalCosts) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GaryClone()));
        harness.addMana(player1, ManaColor.COLORLESS, 1 + repeatedAdditionalCosts.size() * 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreatureWithRepeatedCosts(player1, 0, repeatedAdditionalCosts);
        harness.passBothPriorities();
    }
}
