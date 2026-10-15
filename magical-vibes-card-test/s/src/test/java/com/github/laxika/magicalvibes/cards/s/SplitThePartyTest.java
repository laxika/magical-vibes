package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.n.NeverwinterDryad;
import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
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

@CardUsed({SplitTheParty.class, NeverwinterDryad.class, HillGiantHerdgorger.class, Island.class})
class SplitThePartyTest extends BaseCardTest {

    @Test
    @DisplayName("Controller chooses half the target player's creatures, rounding up")
    void controllerChoosesHalfCreaturesRoundedUp() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new NeverwinterDryad());
        Permanent firstTargetCreature = harness.addToBattlefieldAndReturn(player2, new NeverwinterDryad());
        Permanent secondTargetCreature = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        Permanent thirdTargetCreature = harness.addToBattlefieldAndReturn(player2, new NeverwinterDryad());
        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new SplitTheParty()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

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

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactly(targetLand.getId());
    }

    @Test
    @DisplayName("Can target yourself and returns exactly half an even number of creatures")
    void targetsSelfWithEvenCreatureCount() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new NeverwinterDryad());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new NeverwinterDryad());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new NeverwinterDryad());
        harness.setHand(player1, List.of(new SplitTheParty()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds()).containsExactly(first.getId(), second.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .containsExactly(first.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactly(opposing.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second.getCard());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A single stolen creature returns to its owner rather than its controller")
    void stolenCreatureReturnsToOwner() {
        NeverwinterDryad card = new NeverwinterDryad();
        card.setOwnerId(player1.getId());
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, card);
        gd.stolenCreatures.put(stolen.getId(), player1.getId());
        harness.setHand(player1, List.of(new SplitTheParty()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player2, List.of(stolen.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(stolen.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Counts creatures at resolution rather than when the spell is cast")
    void countsCreaturesAtResolution() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new NeverwinterDryad());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new NeverwinterDryad());
        harness.setHand(player1, List.of(new SplitTheParty()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, player2.getId());

        Permanent third = harness.addToBattlefieldAndReturn(player2, new NeverwinterDryad());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validIds()).containsExactly(first.getId(), second.getId(), third.getId());
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1,
                List.of(first.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), third.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactly(second.getId());
        assertThat(gd.playerHands.get(player2.getId())).contains(first.getCard(), third.getCard());
    }
}
