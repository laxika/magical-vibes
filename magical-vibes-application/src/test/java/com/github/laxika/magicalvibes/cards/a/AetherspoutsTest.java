package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.NetcasterSpider;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Aetherspouts.class, RuneclawBear.class, NetcasterSpider.class, Forest.class, Mountain.class})
class AetherspoutsTest extends BaseCardTest {

    @Test
    @DisplayName("Each attacking creature's owner chooses top or bottom")
    void ownersChooseTopOrBottom() {
        Permanent topCreature = addAttacker(player2, new RuneclawBear());
        Permanent bottomCreature = addAttacker(player2, new NetcasterSpider());

        harness.setLibrary(player2, List.of(new Forest(), new Mountain()));

        castAetherspouts();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.playerId()).isEqualTo(player2.getId());
        assertThat(firstChoice.validIds()).containsExactly(topCreature.getId(), bottomCreature.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(topCreature.getId()));

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player2, "Netcaster Spider");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Runeclaw Bear");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getName()).isEqualTo("Netcaster Spider");
        harness.assertInGraveyard(player1, "Aetherspouts");
    }

    @Test
    @DisplayName("Does not affect creatures that are not attacking")
    void leavesNonAttackingCreaturesAlone() {
        addAttacker(player2, new RuneclawBear());
        harness.addToBattlefield(player2, new NetcasterSpider());

        castAetherspouts();

        harness.handleMultiplePermanentsChosen(player2, List.of());

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertOnBattlefield(player2, "Netcaster Spider");
    }

    @Test
    @DisplayName("Does nothing when there are no attacking creatures")
    void doesNothingWithoutAttackers() {
        harness.addToBattlefield(player2, new RuneclawBear());

        castAetherspouts();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Aetherspouts");
    }

    @Test
    @DisplayName("Owner can choose the order of multiple creatures sent to the top")
    void ownerOrdersTopCreatures() {
        Permanent first = addAttacker(player2, new RuneclawBear());
        Permanent second = addAttacker(player2, new NetcasterSpider());
        harness.setLibrary(player2, List.of(new Forest()));

        castAetherspouts();
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId(), first.getId()));

        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(com.github.laxika.magicalvibes.model.Card::getName)
                .containsExactly("Netcaster Spider", "Runeclaw Bear", "Forest");
    }

    @Test
    @DisplayName("Owner must be allowed to order multiple creatures sent to the bottom")
    void ownerCanOrderBottomCreatures() {
        addAttacker(player2, new RuneclawBear());
        addAttacker(player2, new NetcasterSpider());
        harness.setLibrary(player2, List.of(new Forest()));

        castAetherspouts();
        harness.handleMultiplePermanentsChosen(player2, List.of());

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.playerId()).isEqualTo(player2.getId());
        assertThat(reorder.toBottom()).isTrue();
        assertThat(reorder.cards()).extracting(com.github.laxika.magicalvibes.model.Card::getName)
                .containsExactlyInAnyOrder("Runeclaw Bear", "Netcaster Spider");
    }

    @Test
    @DisplayName("The owner chooses and receives an attacker controlled by another player")
    void ownerChoosesForStolenAttacker() {
        RuneclawBear stolen = new RuneclawBear();
        stolen.setOwnerId(player1.getId());
        Permanent attacker = addAttacker(player2, stolen);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setLibrary(player2, List.of(new Mountain()));

        castAetherspouts();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(attacker.getId()));

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(stolen, forest);
        assertThat(gd.playerDecks.get(player2.getId())).extracting(com.github.laxika.magicalvibes.model.Card::getName)
                .containsExactly("Mountain");
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player player,
                                  com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        return permanent;
    }

    private void castAetherspouts() {
        harness.setHand(player1, List.of(new Aetherspouts()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0);
    }
}
