package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.cards.g.GlassGolem;
import com.github.laxika.magicalvibes.cards.o.OathswornGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EtherealUsher.class, BorosSignet.class, GlassGolem.class, OathswornGiant.class})
class EtherealUsherTest extends BaseCardTest {

    @Test
    void activatedAbilityMakesTargetCreatureUnblockableThisTurn() {
        Permanent usher = addCreatureReady(player1, new EtherealUsher());
        Permanent attacker = addCreatureReady(player1, new GlassGolem());
        Permanent blocker = addCreatureReady(player2, new GlassGolem());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, indexOf(player1, usher), 0, null, attacker.getId());
        assertThat(usher.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(indexOf(player1, attacker)));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void activatedAbilityWearsOffAtEndOfTurn() {
        Permanent usher = addCreatureReady(player1, new EtherealUsher());
        Permanent attacker = addCreatureReady(player1, new GlassGolem());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, indexOf(player1, usher), 0, null, attacker.getId());
        harness.passBothPriorities();
        assertThat(attacker.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.isCantBeBlocked()).isFalse();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent usher = addCreatureReady(player1, new EtherealUsher());
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new BorosSignet());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, usher), 0, null, signet.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void transmuteSearchesForTheSameManaValue() {
        EtherealUsher usher = new EtherealUsher();
        OathswornGiant matchingCard = new OathswornGiant();
        GlassGolem differentManaValue = new GlassGolem();
        harness.setHand(player1, List.of(usher));
        harness.setLibrary(player1, List.of(matchingCard, differentManaValue));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCard);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Ethereal Usher");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(differentManaValue);
    }

    @Test
    void transmuteCanOnlyBeActivatedAtSorcerySpeed() {
        EtherealUsher usher = new EtherealUsher();
        harness.setHand(player1, List.of(usher));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(usher);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

}
