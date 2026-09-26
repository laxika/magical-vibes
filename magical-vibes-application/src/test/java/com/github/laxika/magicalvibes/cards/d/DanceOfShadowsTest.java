package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DanceOfShadows.class, HumbleBudoka.class})
class DanceOfShadowsTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +1/+0 and gain fear")
    void boostsAndGrantsFearToOwnCreatures() {
        addCreatureReady(player1, new HumbleBudoka());
        addCreatureReady(player1, new HumbleBudoka());
        harness.setHand(player1, List.of(new DanceOfShadows()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> ownCreatures = findPermanents(player1, "Humble Budoka");
        assertThat(ownCreatures).hasSize(2);
        for (Permanent p : ownCreatures) {
            assertThat(p.getEffectivePower()).isEqualTo(3);
            assertThat(p.getEffectiveToughness()).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, p, Keyword.FEAR)).isTrue();
        }
        harness.assertInGraveyard(player1, "Dance of Shadows");
    }

    @Test
    @DisplayName("Opponent's creatures are unaffected")
    void doesNotAffectOpponentCreatures() {
        addCreatureReady(player1, new HumbleBudoka());
        addCreatureReady(player2, new HumbleBudoka());
        harness.setHand(player1, List.of(new DanceOfShadows()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent theirs = findPermanent(player2, "Humble Budoka");
        assertThat(theirs.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, theirs, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Boost and fear wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        addCreatureReady(player1, new HumbleBudoka());
        harness.setHand(player1, List.of(new DanceOfShadows()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent creature = findPermanent(player1, "Humble Budoka");
        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after resolution are unaffected")
    void doesNotAffectCreaturesEnteringLater() {
        addCreatureReady(player1, new HumbleBudoka());
        harness.setHand(player1, List.of(new DanceOfShadows()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent laterCreature = addCreatureReady(player1, new HumbleBudoka());
        assertThat(laterCreature.getEffectivePower()).isEqualTo(2);
        assertThat(laterCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Granted fear prevents non-black non-artifact creatures from blocking")
    void grantedFearRestrictsBlocking() {
        Permanent attacker = addCreatureReady(player1, new HumbleBudoka());
        addCreatureReady(player2, new HumbleBudoka());
        harness.setHand(player1, List.of(new DanceOfShadows()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block")
                .hasMessageContaining("(fear)");
    }

    @Test
    @DisplayName("Resolves with an empty battlefield")
    void resolvesWithEmptyBattlefield() {
        harness.setHand(player1, List.of(new DanceOfShadows()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).isEmpty();
    }
}
