package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WintersRest.class, GrizzlyBears.class, SnowCoveredForest.class})
class WintersRestTest extends BaseCardTest {

    @Test
    @DisplayName("Enters attached to and taps the target creature")
    void entersAttachedAndTapsTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castWintersRest(creature);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Winter's Rest").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Does not lock the creature without another snow permanent")
    void creatureUntapsWithoutSnowPermanent() {
        Permanent creature = addReady(player2, new GrizzlyBears());
        Permanent aura = addReady(player1, new WintersRest());
        aura.setAttachedTo(creature.getId());
        creature.tap();

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Locks the enchanted creature while its controller has another snow permanent")
    void creatureDoesNotUntapWithSnowPermanent() {
        Permanent creature = addReady(player2, new GrizzlyBears());
        Permanent aura = addReady(player1, new WintersRest());
        aura.setAttachedTo(creature.getId());
        addReady(player1, new SnowCoveredForest());
        creature.tap();

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's snow permanent does not satisfy the condition")
    void opponentSnowPermanentDoesNotEnableLock() {
        Permanent creature = addReady(player2, new GrizzlyBears());
        Permanent aura = addReady(player1, new WintersRest());
        aura.setAttachedTo(creature.getId());
        addReady(player2, new SnowCoveredForest());
        creature.tap();

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    private void castWintersRest(Permanent target) {
        harness.setHand(player1, List.of(new WintersRest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
