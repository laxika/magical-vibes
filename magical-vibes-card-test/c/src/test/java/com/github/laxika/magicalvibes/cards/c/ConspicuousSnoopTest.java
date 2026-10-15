package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BrashTaunter;
import com.github.laxika.magicalvibes.cards.g.GoblinArsonist;
import com.github.laxika.magicalvibes.cards.g.GoblinFireslinger;
import com.github.laxika.magicalvibes.cards.h.Hobblefiend;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConspicuousSnoop.class, GoblinFireslinger.class, RagingGoblin.class, Shock.class,
        GoblinArsonist.class, BrashTaunter.class, Hobblefiend.class})
class ConspicuousSnoopTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast a Goblin spell from the top of the library")
    void castsGoblinFromLibraryTop() {
        harness.addToBattlefield(player1, new ConspicuousSnoop());
        Card goblin = new RagingGoblin();
        harness.setLibrary(player1, List.of(goblin));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Raging Goblin");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(goblin);
    }

    @Test
    @DisplayName("Cannot cast a non-Goblin spell from the top of the library")
    void cannotCastNonGoblinFromLibraryTop() {
        harness.addToBattlefield(player1, new ConspicuousSnoop());
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(shock);
    }

    @Test
    @DisplayName("Gains and can activate the top Goblin's activated abilities")
    void activatesAbilityFromGoblinOnTop() {
        addCreatureReady(player1, new ConspicuousSnoop());
        harness.setLibrary(player1, List.of(new GoblinFireslinger()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    void revealsNonGoblinTopCardToBothPlayers() {
        harness.addToBattlefield(player1, new ConspicuousSnoop());
        Card top = new Shock();
        harness.setLibrary(player1, List.of(top));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[{") && message.contains(top.getId().toString()));
        assertThat(harness.getConn2().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[{") && message.contains(top.getId().toString()));
    }

    @Test
    void stillPaysManaForGoblinSpell() {
        harness.addToBattlefield(player1, new ConspicuousSnoop());
        Card goblin = new GoblinArsonist();
        harness.setLibrary(player1, List.of(goblin));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(goblin);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotCastGoblinCreatureDuringCombat() {
        harness.addToBattlefield(player1, new ConspicuousSnoop());
        Card goblin = new GoblinArsonist();
        harness.setLibrary(player1, List.of(goblin));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(goblin);
    }

    @Test
    void canCastSuccessiveGoblinsWithoutOncePerTurnLimit() {
        harness.addToBattlefield(player1, new ConspicuousSnoop());
        Card first = new GoblinArsonist();
        Card second = new GoblinArsonist();
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveFromLibraryTop(player1);
        harness.castAndResolveFromLibraryTop(player1);

        assertThat(countPermanents(player1, "Goblin Arsonist")).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotGainActivatedAbilitiesOfNonGoblin() {
        addCreatureReady(player1, new ConspicuousSnoop());
        harness.setLibrary(player1, List.of(new Hobblefiend()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addToBattlefield(player1, new GoblinArsonist());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void losesInheritedAbilityWhenLibraryBecomesEmpty() {
        addCreatureReady(player1, new ConspicuousSnoop());
        harness.setLibrary(player1, List.of(new GoblinFireslinger()));
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        findPermanent(player1, "Conspicuous Snoop").untap();
        harness.setLibrary(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 19);
    }

    @Test
    void activatedAbilityResolvesAfterTopCardChangesButCannotBeActivatedAgain() {
        var snoop = addCreatureReady(player1, new ConspicuousSnoop());
        harness.setLibrary(player1, List.of(new GoblinFireslinger()));
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.setLibrary(player1, List.of(new Shock()));

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        snoop.untap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void inheritedTapAbilityStillRequiresSummoningSicknessToWearOff() {
        var snoop = harness.addToBattlefieldAndReturn(player1, new ConspicuousSnoop());
        snoop.setSummoningSick(true);
        harness.setLibrary(player1, List.of(new GoblinFireslinger()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(snoop.isTapped()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    void inheritedFightUsesSnoopWithoutGainingIndestructibleOrDamageTrigger() {
        addCreatureReady(player1, new ConspicuousSnoop());
        var opponent = harness.addToBattlefieldAndReturn(player2, new Hobblefiend());
        Card top = new BrashTaunter();
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, opponent.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Conspicuous Snoop");
        harness.assertInGraveyard(player2, "Hobblefiend");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void inheritedFightCannotTargetSnoopItself() {
        var snoop = addCreatureReady(player1, new ConspicuousSnoop());
        harness.setLibrary(player1, List.of(new BrashTaunter()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, snoop.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(snoop.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void inheritedFightStillRequiresItsManaCost() {
        var snoop = addCreatureReady(player1, new ConspicuousSnoop());
        var opponent = harness.addToBattlefieldAndReturn(player2, new Hobblefiend());
        harness.setLibrary(player1, List.of(new BrashTaunter()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(snoop.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removingSnoopRemovesCastingPermissionAndPublicRevelation() {
        var snoop = harness.addToBattlefieldAndReturn(player1, new ConspicuousSnoop());
        Card goblin = new GoblinArsonist();
        harness.setLibrary(player1, List.of(goblin));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, snoop.getId());

        harness.assertInGraveyard(player1, "Conspicuous Snoop");
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(goblin);
        harness.clearMessages();
        harness.publishState();
        assertThat(harness.getConn2().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }
}
