package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MazeGlider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RuricTharTheUnbowed.class, Divination.class, GrizzlyBears.class, MazeGlider.class})
class RuricTharTheUnbowedTest extends BaseCardTest {

    private Permanent addRuricThar(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new RuricTharTheUnbowed());
    }

    @Test
    @DisplayName("Dealing 6 damage to an opponent who casts a noncreature spell")
    void damagesOpponentCastingNoncreatureSpell() {
        addRuricThar(player1);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castSorcery(player2, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 6);
    }

    @Test
    @DisplayName("Dealing 6 damage to its own controller when they cast a noncreature spell")
    void damagesOwnControllerCastingNoncreatureSpell() {
        addRuricThar(player1);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 6);
    }

    @Test
    @DisplayName("Not triggering when a creature spell is cast")
    void noDamageWhenCreatureSpellCast() {
        addRuricThar(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Declaring no attackers while Ruric Thar can attack throws exception")
    void mustAttackWhenAble() {
        addRuricThar(player1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Ruric Thar attacks for 6 and stays untapped thanks to vigilance")
    void attacksForSixAndStaysUntapped() {
        harness.setLife(player2, 20);
        Permanent ruricThar = addRuricThar(player1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(ruricThar.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Ruric Thar is not required to attack")
    void tappedRuricTharCanStayBack() {
        addRuricThar(player1).tap();
        declareAttackers(player1, List.of());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A summoning-sick Ruric Thar is not required to attack")
    void summoningSickRuricTharCanStayBack() {
        addRuricThar(player1).setSummoningSick(true);
        declareAttackers(player1, List.of());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Casting Ruric Thar does not trigger its ability")
    void doesNotTriggerForItsOwnCreatureSpell() {
        harness.setHand(player1, List.of(new RuricTharTheUnbowed()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Ruric Thar, the Unbowed");
    }

    @Test
    @DisplayName("A noncreature spell's trigger still deals damage after Ruric Thar leaves")
    void triggerSurvivesSourceRemoval() {
        Permanent ruricThar = addRuricThar(player1);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castSorcery(player2, 0, 0);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, ruricThar));
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Ruric Thar, the Unbowed");
    }

    @Test
    @DisplayName("Ruric Thars controlled by different players each damage the caster")
    void bothPlayersRuricTharsTriggerForSameSpell() {
        addRuricThar(player1);
        addRuricThar(player2);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 8);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Reach lets Ruric Thar block a flying creature")
    void canBlockFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new MazeGlider());
        attacker.setAttacking(true);
        Permanent ruricThar = addRuricThar(player2);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Maze Glider");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ruricThar);
    }
}
