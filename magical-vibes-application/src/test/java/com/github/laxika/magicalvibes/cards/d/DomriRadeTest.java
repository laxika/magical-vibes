package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomriRadeTest extends BaseCardTest {

    @Test
    @DisplayName("+1 with a creature on top — accepting reveals it to hand")
    void plusOneCreatureOnTopAcceptToHand() {
        Permanent domri = addReadyDomri(player1);

        Card topBears = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topBears);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(domri.getCounterCount(CounterType.LOYALTY)).isEqualTo(4); // 3 + 1
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(topBears.getId()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(topBears.getId()));
    }

    @Test
    @DisplayName("+1 with a creature on top — declining leaves it on top, never in the graveyard")
    void plusOneCreatureOnTopDeclineLeavesOnTop() {
        addReadyDomri(player1);

        Card topBears = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topBears);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(topBears.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(topBears.getId()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("+1 with a noncreature on top — no choice is offered and the card stays on top")
    void plusOneNonCreatureOnTopStaysOnTop() {
        addReadyDomri(player1);

        Card topBolt = new LightningBolt();
        gd.playerDecks.get(player1.getId()).addFirst(topBolt);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(topBolt.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(topBolt.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(topBolt.getId()));
    }

    @Test
    @DisplayName("-2 makes the chosen creatures deal damage equal to their power to each other")
    void minusTwoFights() {
        Permanent domri = addReadyDomri(player1);
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID angelId = harness.getPermanentId(player1, "Serra Angel");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(angelId, bearsId));
        harness.passBothPriorities();

        assertThat(domri.getCounterCount(CounterType.LOYALTY)).isEqualTo(1); // 3 - 2

        Permanent angel = findPermanent(player1, "Serra Angel");
        assertThat(angel.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("-2 cannot fight a creature Domri's controller does not control in the first slot")
    void minusTwoFirstTargetMustBeControlled() {
        addReadyDomri(player1);
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID angelId = harness.getPermanentId(player1, "Serra Angel");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() ->
                harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(bearsId, angelId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-2 cannot pick the same creature twice (\"another target creature\")")
    void minusTwoTargetsMustBeDifferent() {
        addReadyDomri(player1);
        harness.addToBattlefield(player1, new SerraAngel());

        UUID angelId = harness.getPermanentId(player1, "Serra Angel");

        assertThatThrownBy(() ->
                harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(angelId, angelId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-2 cannot target a noncreature permanent")
    void minusTwoCannotTargetLand() {
        addReadyDomri(player1);
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player2, new Plains());

        UUID angelId = harness.getPermanentId(player1, "Serra Angel");
        UUID plainsId = harness.getPermanentId(player2, "Plains");

        assertThatThrownBy(() ->
                harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(angelId, plainsId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-7 emblem gives the controller's creatures double strike, trample, hexproof and haste")
    void ultimateGrantsKeywordsToOwnCreatures() {
        Permanent domri = addReadyDomri(player1);
        domri.setCounterCount(CounterType.LOYALTY, 7);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.emblems).hasSize(1);
        assertThat(gd.emblems.getFirst().controllerId()).isEqualTo(player1.getId());

        Permanent own = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, own, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, own, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, own, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, own, Keyword.HASTE)).isTrue();

        Permanent opponentCreature = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HEXPROOF)).isFalse();

        // The emblem only grants to creatures — Domri himself is untouched.
        assertThat(gqs.hasKeyword(gd, domri, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("-7 emblem applies to creatures entering later")
    void ultimateAppliesToLaterCreatures() {
        Permanent domri = addReadyDomri(player1);
        domri.setCounterCount(CounterType.LOYALTY, 7);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent later = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, later, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, later, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate -7 with only starting loyalty")
    void cannotUltimateWithoutEnoughLoyalty() {
        addReadyDomri(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyDomri(Player player) {
        Permanent perm = new Permanent(new DomriRade());
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
