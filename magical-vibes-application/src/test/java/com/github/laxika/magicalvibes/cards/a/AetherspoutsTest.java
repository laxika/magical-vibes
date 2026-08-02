package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AetherspoutsTest extends BaseCardTest {

    @Test
    @DisplayName("Each attacking creature's owner chooses top or bottom")
    void ownersChooseTopOrBottom() {
        Permanent topCreature = addAttacker(player2, new GrizzlyBears());
        Permanent bottomCreature = addAttacker(player2, new HillGiant());

        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(new Forest(), new Mountain()));

        castAetherspouts();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.playerId()).isEqualTo(player2.getId());
        assertThat(firstChoice.validIds()).containsExactly(topCreature.getId(), bottomCreature.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(topCreature.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getName()).isEqualTo("Hill Giant");
        harness.assertInGraveyard(player1, "Aetherspouts");
    }

    @Test
    @DisplayName("Does not affect creatures that are not attacking")
    void leavesNonAttackingCreaturesAlone() {
        addAttacker(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        castAetherspouts();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player2, List.of());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Does nothing when there are no attacking creatures")
    void doesNothingWithoutAttackers() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAetherspouts();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Aetherspouts");
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player player,
                                  com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void castAetherspouts() {
        harness.setHand(player1, List.of(new Aetherspouts()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0);
    }
}
