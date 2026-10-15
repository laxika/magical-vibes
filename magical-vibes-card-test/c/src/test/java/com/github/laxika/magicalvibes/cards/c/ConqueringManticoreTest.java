package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LagacLizard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConqueringManticore.class, LagacLizard.class})
class ConqueringManticoreTest extends BaseCardTest {

    @Test
    @DisplayName("Steals, untaps and grants haste to an opponent's creature")
    void stealsUntapsAndGrantsHaste() {
        Permanent target = addCreature(new LagacLizard(), player2);
        target.tap();

        castManticore(target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        harness.assertOnBattlefield(player1, "Lagac Lizard");
        harness.assertNotOnBattlefield(player2, "Lagac Lizard");
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = addCreature(new LagacLizard(), player2);

        castManticore(target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        harness.assertOnBattlefield(player2, "Lagac Lizard");
        harness.assertNotOnBattlefield(player1, "Lagac Lizard");
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void rejectsOwnCreature() {
        Permanent own = addCreature(new LagacLizard(), player1);

        assertThatThrownBy(() -> castManticore(own.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Manticore can enter when no opponent controls a creature")
    void entersWithoutLegalTargets() {
        castManticore(null);

        harness.assertOnBattlefield(player1, "Conquering Manticore");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The triggered ability resolves even if Manticore leaves the battlefield")
    void triggerSurvivesSourceRemoval() {
        Permanent target = addCreature(new LagacLizard(), player2);
        target.tap();
        castManticore(target.getId());
        Permanent source = findPermanent(player1, "Conquering Manticore");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lagac Lizard");
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Removing the target before resolution does not affect another creature")
    void removedTargetMakesTriggerHaveNoEffect() {
        Permanent target = addCreature(new LagacLizard(), player2);
        Permanent other = addCreature(new LagacLizard(), player2);
        other.tap();
        castManticore(target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lagac Lizard");
        harness.assertOnBattlefield(player2, "Lagac Lizard");
        assertThat(other.isTapped()).isTrue();
        assertThat(other.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addCreature(Card card, Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void castManticore(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ConqueringManticore()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
    }
}
