package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HengeGuardian;
import com.github.laxika.magicalvibes.cards.j.JhovallQueen;
import com.github.laxika.magicalvibes.cards.t.TidalBore;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Crackdown.class, CinderElemental.class, HengeGuardian.class, JhovallQueen.class, TidalBore.class})
class CrackdownTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped nonwhite creature with power 3 or greater does not untap")
    void power3NonwhiteCreatureStaysTapped() {
        addCreatureReady(player1, new Crackdown());
        Permanent giant = addCreatureReady(player1, new HengeGuardian());
        giant.tap();

        advanceToUpkeep(player1);

        assertThat(giant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped nonwhite creature with power under 3 untaps normally")
    void power2NonwhiteCreatureUntaps() {
        addCreatureReady(player1, new Crackdown());
        Permanent bears = addCreatureReady(player1, new CinderElemental());
        bears.tap();

        advanceToUpkeep(player1);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapped white creature with power 3 or greater untaps normally")
    void whiteCreatureUntaps() {
        addCreatureReady(player1, new Crackdown());
        Permanent angel = addCreatureReady(player1, new JhovallQueen());
        angel.tap();

        advanceToUpkeep(player1);

        assertThat(angel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Crackdown affects nonwhite creatures during an opponent's untap step")
    void affectsOpponentCreatures() {
        addCreatureReady(player1, new Crackdown());
        Permanent opponentGiant = addCreatureReady(player2, new HengeGuardian());
        opponentGiant.tap();

        advanceToUpkeep(player2);

        assertThat(opponentGiant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Power raised to three by a counter prevents untapping")
    void increasedPowerPreventsUntap() {
        addCreatureReady(player1, new Crackdown());
        Permanent creature = addCreatureReady(player1, new CinderElemental());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.tap();

        advanceToUpkeep(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Power reduced below three allows untapping")
    void reducedPowerAllowsUntap() {
        addCreatureReady(player1, new Crackdown());
        Permanent creature = addCreatureReady(player1, new HengeGuardian());
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        creature.tap();

        advanceToUpkeep(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The untap restriction ends when Crackdown leaves the battlefield")
    void removalEndsRestriction() {
        Permanent crackdown = harness.addToBattlefieldAndReturn(player1, new Crackdown());
        Permanent creature = addCreatureReady(player1, new HengeGuardian());
        creature.tap();
        advanceToUpkeep(player1);
        assertThat(creature.isTapped()).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(crackdown);
        advanceToUpkeep(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An untap spell can untap a creature restricted by Crackdown")
    void spellCanUntapRestrictedCreature() {
        addCreatureReady(player1, new Crackdown());
        Permanent creature = addCreatureReady(player1, new HengeGuardian());
        creature.tap();
        advanceToUpkeep(player1);
        assertThat(creature.isTapped()).isTrue();
        harness.setHand(player1, List.of(new TidalBore()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.isTapped()).isFalse();
    }
}
