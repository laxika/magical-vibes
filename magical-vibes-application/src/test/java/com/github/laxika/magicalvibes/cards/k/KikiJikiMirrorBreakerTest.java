package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KikiJikiMirrorBreaker.class, WanderingOnes.class, Forest.class})
class KikiJikiMirrorBreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping creates a hasty token copy scheduled to be sacrificed at the next end step")
    void createsHastyTokenCopySacrificedAtEndStep() {
        addKikiJikiReady(player1);
        harness.addToBattlefield(player1, new WanderingOnes());

        UUID targetId = harness.getPermanentId(player1, "Wandering Ones");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Wandering Ones")).isEqualTo(2);

        Permanent token = findPermanents(player1, "Wandering Ones").stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(token.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
    }

    @Test
    @DisplayName("The copy token is sacrificed at the beginning of the next end step")
    void sacrificesCopyTokenAtNextEndStep() {
        addKikiJikiReady(player1);
        harness.addToBattlefield(player1, new WanderingOnes());

        UUID targetId = harness.getPermanentId(player1, "Wandering Ones");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Wandering Ones").stream()
                .filter(p -> p.getCard().isToken())
                .count()).isOne();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Wandering Ones").stream()
                .filter(p -> p.getCard().isToken())
                .toList()).isEmpty();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).isEmpty();
    }

    @Test
    @DisplayName("A creature an opponent controls is not a legal target")
    void cannotCopyOpponentCreature() {
        addKikiJikiReady(player1);
        harness.addToBattlefield(player2, new WanderingOnes());

        UUID targetId = harness.getPermanentId(player2, "Wandering Ones");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonlegendary creature you control");
    }

    @Test
    @DisplayName("A noncreature permanent is not a legal target")
    void cannotCopyNonCreaturePermanent() {
        addKikiJikiReady(player1);
        harness.addToBattlefield(player1, new Forest());

        UUID targetId = harness.getPermanentId(player1, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonlegendary creature you control");
    }

    @Test
    @DisplayName("A legendary creature is not a legal target")
    void cannotCopyLegendaryCreature() {
        Permanent kiki = addKikiJikiReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, kiki.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonlegendary creature you control");
    }

    private Permanent addKikiJikiReady(Player player) {
        return addCreatureReady(player, new KikiJikiMirrorBreaker());
    }
}
