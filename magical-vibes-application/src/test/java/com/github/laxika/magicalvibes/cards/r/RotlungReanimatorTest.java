package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CabalArchon;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.i.Infest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RotlungReanimator.class, CabalArchon.class, GlorySeeker.class, Infest.class, Shock.class})
class RotlungReanimatorTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Zombie when it dies")
    void createsZombieWhenItDies() {
        harness.addToBattlefield(player1, new RotlungReanimator());

        killWithShock(player2, player1, "Rotlung Reanimator");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(zombieTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Creates a Zombie when an opponent's Cleric dies")
    void createsZombieWhenOpponentsClericDies() {
        harness.addToBattlefield(player1, new RotlungReanimator());
        harness.addToBattlefield(player2, new CabalArchon());

        killWithShock(player1, player2, "Cabal Archon");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(zombieTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Creates a Zombie when an ally's Cleric dies")
    void createsZombieWhenAllysClericDies() {
        harness.addToBattlefield(player1, new RotlungReanimator());
        harness.addToBattlefield(player1, new CabalArchon());

        killWithShock(player2, player1, "Cabal Archon");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(zombieTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Creates one Zombie for each Cleric that dies simultaneously")
    void createsOneZombiePerClericThatDiesSimultaneously() {
        harness.addToBattlefield(player1, new RotlungReanimator());
        harness.addToBattlefield(player1, new CabalArchon());

        harness.castFromHand(player1, new Infest(), "{1}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();

        assertThat(zombieTokens(player1)).hasSize(2);
    }

    @Test
    @DisplayName("Does not trigger when a non-Cleric dies")
    void doesNotTriggerForNonCleric() {
        harness.addToBattlefield(player1, new RotlungReanimator());
        harness.addToBattlefield(player1, new GlorySeeker());

        killWithShock(player2, player1, "Glory Seeker");

        assertThat(gd.stack).isEmpty();
        assertThat(zombieTokens(player1)).isEmpty();
    }

    private void killWithShock(com.github.laxika.magicalvibes.model.Player caster,
                               com.github.laxika.magicalvibes.model.Player targetController,
                               String targetName) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castAndResolveInstant(caster, 0, targetId);
    }

    private List<Permanent> zombieTokens(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Zombie"))
                .toList();
    }
}
