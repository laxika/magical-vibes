package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SplitTheParty.class, GrizzlyBears.class, HillGiant.class, Island.class})
class SplitThePartyTest extends BaseCardTest {

    @Test
    @DisplayName("Controller chooses half the target player's creatures, rounding up")
    void controllerChoosesHalfCreaturesRoundedUp() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstTargetCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondTargetCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent thirdTargetCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new SplitTheParty()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validIds()).containsExactly(
                firstTargetCreature.getId(), secondTargetCreature.getId(), thirdTargetCreature.getId());

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1,
                List.of(firstTargetCreature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultiplePermanentsChosen(player1,
                List.of(firstTargetCreature.getId(), secondTargetCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .containsExactly(ownCreature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactly(thirdTargetCreature.getId(), targetLand.getId());
        assertThat(gd.playerHands.get(player2.getId()))
                .contains(firstTargetCreature.getCard(), secondTargetCreature.getCard());
    }

    @Test
    @DisplayName("Does nothing when the target player controls no creatures")
    void noCreatures() {
        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new SplitTheParty()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactly(targetLand.getId());
    }
}
