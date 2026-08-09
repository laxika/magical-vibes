package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntangibleVirtue;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

class VivienReidTest extends BaseCardTest {

    @Test
    @DisplayName("+1 puts a chosen creature or land into hand and randomizes the rest on the bottom")
    void plusOneChoosesCreatureOrLand() {
        Permanent vivien = addReadyVivien(player1);
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card spell = new Shock();
        Card flyer = new SerraAngel();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(creature, land, spell, flyer));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).allCards())
                .containsExactly(creature, land, spell, flyer);

        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, spell, flyer);
    }

    @Test
    @DisplayName("-3 destroys an artifact")
    void minusThreeDestroysArtifact() {
        Permanent vivien = addReadyVivien(player1);
        harness.addToBattlefield(player2, new LiquimetalCoating());
        UUID artifactId = harness.getPermanentId(player2, "Liquimetal Coating");

        harness.activateAbility(player1, 0, 1, null, artifactId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Liquimetal Coating");
        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-3 destroys an enchantment and a creature with flying, but not a ground creature")
    void minusThreeTargetRestrictions() {
        Permanent vivien = addReadyVivien(player1);
        harness.addToBattlefield(player2, new IntangibleVirtue());
        Permanent enchantment = findPermanent(player2, "Intangible Virtue");

        harness.activateAbility(player1, 0, 1, null, enchantment.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Intangible Virtue");

        vivien.setCounterCount(CounterType.LOYALTY, 5);
        vivien.setLoyaltyActivationsThisTurn(0);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addToBattlefield(player2, new SerraAngel());
        Permanent angel = findPermanent(player2, "Serra Angel");
        harness.activateAbility(player1, 0, 1, null, angel.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Serra Angel");
    }

    @Test
    @DisplayName("-8 gives the controller's creatures +2/+2 and three keywords")
    void ultimateCreatesCreatureBoostEmblem() {
        Permanent vivien = addReadyVivien(player1);
        vivien.setCounterCount(CounterType.LOYALTY, 8);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent ownCreature = findPermanent(player1, "Grizzly Bears");
        Permanent opponentCreature = findPermanent(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.emblems).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addReadyVivien(Player player) {
        Permanent perm = new Permanent(new VivienReid());
        perm.setCounterCount(CounterType.LOYALTY, 5);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
