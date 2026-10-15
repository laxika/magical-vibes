package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TerrorsOfTheTrack.class, GrizzlyBears.class, Shock.class, Pyroclasm.class})
class TerrorsOfTheTrackTest extends BaseCardTest {

    @Test
    @DisplayName("A creature dying makes each opponent lose 1 life and gains you 1 life")
    void creatureDeathDrainsOpponent() {
        harness.addToBattlefield(player1, new TerrorsOfTheTrack());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killWithShock(player2, creature);

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Terrors of the Track triggers when it dies")
    void selfDeathDrainsOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent terrors = harness.addToBattlefieldAndReturn(player1, new TerrorsOfTheTrack());
        killWithShock(player2, terrors);

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The death trigger fires only once each turn")
    void triggersOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new TerrorsOfTheTrack());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killWithShock(player2, firstCreature);
        killWithShock(player2, secondCreature);

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking but permits a flying blocker")
    void flyingRestrictsBlockers() {
        Permanent terrors = addCreatureReady(player1, new TerrorsOfTheTrack());
        Permanent groundBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent flyingBlocker = addCreatureReady(player2, new TerrorsOfTheTrack());

        assertThat(bls.canBlockAttacker(gd, groundBlocker, terrors,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, flyingBlocker, terrors,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("A creature token dying triggers the drain")
    void tokenDeathDrainsOpponent() {
        harness.addToBattlefield(player1, new TerrorsOfTheTrack());
        GrizzlyBears token = new GrizzlyBears();
        token.setToken(true);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, token);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killWithShock(player1, creature);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("An opposing creature dying also triggers the drain")
    void opposingCreatureDeathDrainsOpponent() {
        harness.addToBattlefield(player1, new TerrorsOfTheTrack());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killWithShock(player1, creature);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Self-death cannot trigger again after another creature died that turn")
    void selfDeathSharesLimitWithOtherCreatureDeaths() {
        Permanent terrors = harness.addToBattlefieldAndReturn(player1, new TerrorsOfTheTrack());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killWithShock(player2, creature);
        killWithShock(player2, terrors);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Terrors of the Track");
    }

    @Test
    @DisplayName("Each copy has an independent once-per-turn death trigger")
    void multipleCopiesTriggerIndependently() {
        harness.addToBattlefield(player1, new TerrorsOfTheTrack());
        harness.addToBattlefield(player1, new TerrorsOfTheTrack());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killWithShock(player1, creature);

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The death trigger is available again on the opponent's turn")
    void deathTriggerResetsOnNextTurn() {
        harness.addToBattlefield(player1, new TerrorsOfTheTrack());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killWithShock(player1, first);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        killWithShock(player2, second);

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Simultaneous self-death and other creature deaths trigger only once")
    void simultaneousDeathsTriggerOnlyOnce() {
        harness.addToBattlefield(player1, new TerrorsOfTheTrack());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Double team conjures a duplicate and removes itself from both cards")
    void doubleTeamConjuresDuplicate() {
        Permanent terrors = addCreatureReady(player1, new TerrorsOfTheTrack());
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).singleElement().satisfies(card -> {
            assertThat(card).isInstanceOf(TerrorsOfTheTrack.class);
            assertThat(card.hasKeyword(Keyword.DOUBLE_TEAM)).isFalse();
        });
        assertThat(gqs.hasKeyword(gd, terrors, Keyword.DOUBLE_TEAM)).isFalse();
    }

    @Test
    @DisplayName("A token with double team does not conjure a duplicate")
    void tokenDoesNotConjureDuplicate() {
        TerrorsOfTheTrack token = new TerrorsOfTheTrack();
        token.setToken(true);
        addCreatureReady(player1, token);
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void killWithShock(com.github.laxika.magicalvibes.model.Player caster, Permanent creature) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, creature.getId());
        resolveAllTriggers();
    }
}
