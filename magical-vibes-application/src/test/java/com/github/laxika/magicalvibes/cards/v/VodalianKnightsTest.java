package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.h.HavenwoodBattleground;
import com.github.laxika.magicalvibes.cards.h.Homarid;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.StreambedAquitects;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VodalianKnights.class, Island.class, Homarid.class, HavenwoodBattleground.class,
        StreambedAquitects.class, ZuranOrb.class})
class VodalianKnightsTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificed when controller controls no Islands")
    void sacrificedWhenNoIslands() {
        harness.setHand(player1, List.of(new VodalianKnights()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Vodalian Knights");
        harness.assertInGraveyard(player1, "Vodalian Knights");
    }

    @Test
    @DisplayName("Sacrificed when only the opponent controls an Island")
    void sacrificedWhenOnlyOpponentHasIsland() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new VodalianKnights()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Vodalian Knights");
        harness.assertInGraveyard(player1, "Vodalian Knights");
    }

    @Test
    @DisplayName("Cannot attack when defending player controls no Island")
    void cannotAttackWithoutDefendingIsland() {
        harness.addToBattlefield(player1, new Island());
        Permanent knights = addCreatureReady(player1, new VodalianKnights());

        int knightsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(knights);
        assertThatThrownBy(() -> declareAttackers(List.of(knightsIndex)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack when defending player controls an Island")
    void canAttackWithDefendingIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        Permanent knights = addCreatureReady(player1, new VodalianKnights());

        int knightsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(knights);
        declareAttackers(List.of(knightsIndex));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("First strike lets Knights survive combat with a 2/2 blocker")
    void firstStrikeDealsDamageBeforeBlocker() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        Permanent knights = addCreatureReady(player1, new VodalianKnights());
        Permanent blocker = addCreatureReady(player2, new Homarid());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(knights)));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(knights))));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(knights);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Paying blue mana grants flying until end of turn")
    void payingBlueManaGrantsFlying() {
        harness.addToBattlefield(player1, new Island());
        Permanent knights = addCreatureReady(player1, new VodalianKnights());
        harness.addMana(player1, ManaColor.BLUE, 1);

        int knightsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(knights);
        harness.activateAbility(player1, knightsIndex, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, knights, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying ability cannot be activated without blue mana")
    void flyingAbilityRequiresBlueMana() {
        harness.addToBattlefield(player1, new Island());
        Permanent knights = addCreatureReady(player1, new VodalianKnights());

        int knightsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(knights);
        assertThatThrownBy(() -> harness.activateAbility(player1, knightsIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new Island());
        Permanent knights = addCreatureReady(player1, new VodalianKnights());
        harness.addMana(player1, ManaColor.BLUE, 1);

        int knightsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(knights);
        harness.activateAbility(player1, knightsIndex, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, knights, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A land that has become an Island prevents the sacrifice trigger")
    void transformedLandCountsAsIsland() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent knights = addCreatureReady(player1, new VodalianKnights());
        Permanent aquitects = addCreatureReady(player1, new StreambedAquitects());
        Permanent transformedLand = harness.addToBattlefieldAndReturn(player1, new HavenwoodBattleground());
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new ZuranOrb());

        int aquitectsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(aquitects);
        harness.activateAbility(player1, aquitectsIndex, 1, null, transformedLand.getId());
        harness.passBothPriorities();
        assertThat(gqs.effectiveBasicLandTypes(gd, transformedLand)).contains(CardSubtype.ISLAND);

        int orbIndex = gd.playerBattlefields.get(player1.getId()).indexOf(orb);
        harness.activateAbility(player1, orbIndex, null, null);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, island.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(knights);
    }
}
