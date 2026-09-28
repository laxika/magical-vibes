package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.s.SelesnyaGuildmage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VindictiveMob.class, BorosRecruit.class, SelesnyaGuildmage.class})
class VindictiveMobTest extends BaseCardTest {

    @Test
    @DisplayName("ETB sacrifices Vindictive Mob itself when it is the only creature")
    void etbSacrificesItselfWhenOnlyCreature() {
        castVindictiveMob();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vindictive Mob");
        harness.assertInGraveyard(player1, "Vindictive Mob");
    }

    @Test
    @DisplayName("ETB lets the controller sacrifice another creature")
    void etbSacrificesAnotherCreature() {
        addCreatureReady(player1, new BorosRecruit());
        castVindictiveMob();

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent recruit = findPermanent(player1, "Boros Recruit");
        harness.handlePermanentChosen(player1, recruit.getId());

        harness.assertOnBattlefield(player1, "Vindictive Mob");
        harness.assertNotOnBattlefield(player1, "Boros Recruit");
        harness.assertInGraveyard(player1, "Boros Recruit");
    }

    @Test
    @DisplayName("Vindictive Mob can't be blocked by a Saproling")
    void cannotBeBlockedBySaproling() {
        attackingMob();
        Permanent blocker = createSaproling();

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Vindictive Mob can be blocked by a non-Saproling creature")
    void canBeBlockedByNonSaprolingCreature() {
        attackingMob();
        Permanent blocker = addCreatureReady(player2, new BorosRecruit());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void castVindictiveMob() {
        harness.castFromHand(player1, new VindictiveMob(), "{4}{B}{B}");
    }

    private Permanent attackingMob() {
        Permanent attacker = addCreatureReady(player1, new VindictiveMob());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent createSaproling() {
        Permanent guildmage = addCreatureReady(player2, new SelesnyaGuildmage());
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.GREEN, 1);
        int guildmageIndex = gd.playerBattlefields.get(player2.getId()).indexOf(guildmage);
        harness.activateAbility(player2, guildmageIndex, 0, null, null);
        harness.passBothPriorities();

        Permanent blocker = findPermanent(player2, "Saproling");
        blocker.setSummoningSick(false);
        return blocker;
    }

}
