package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThassaGodOfTheSeaTest extends BaseCardTest {

    @Test
    @DisplayName("Thassa is not a creature below five devotion to blue")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent thassa = addThassa();
        addBluePermanents(3);

        assertThat(gqs.isCreature(gd, thassa)).isFalse();
        assertThat(gqs.isEnchantment(gd, thassa)).isTrue();
    }

    @Test
    @DisplayName("Thassa becomes a creature at five devotion to blue")
    void becomesCreatureAtDevotionThreshold() {
        Permanent thassa = addThassa();
        addBluePermanents(4);

        assertThat(gqs.isCreature(gd, thassa)).isTrue();
    }

    @Test
    @DisplayName("Thassa scries 1 at the beginning of its controller's upkeep")
    void scriesAtUpkeep() {
        addThassa();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("Thassa makes a creature you control unblockable until end of turn")
    void makesOwnCreatureUnblockable() {
        addThassa();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();
    }

    @Test
    @DisplayName("Thassa cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        addThassa();
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    private Permanent addThassa() {
        return harness.addToBattlefieldAndReturn(player1, new ThassaGodOfTheSea());
    }

    private void addBluePermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new FugitiveWizard());
        }
    }
}
