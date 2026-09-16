package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.g.GluttonousZombie;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Infest.class, GlorySeeker.class, GluttonousZombie.class})
class InfestTest extends BaseCardTest {

    @Test
    @DisplayName("Gives -2/-2 to all creatures, both players")
    void debuffsAllCreatures() {
        harness.addToBattlefield(player1, new GluttonousZombie()); // 3/3
        harness.addToBattlefield(player2, new GluttonousZombie()); // 3/3

        castInfest();

        Permanent own = findPermanent(player1, "Gluttonous Zombie");
        Permanent opp = findPermanent(player2, "Gluttonous Zombie");
        assertThat(own.getEffectivePower()).isEqualTo(1);
        assertThat(own.getEffectiveToughness()).isEqualTo(1);
        assertThat(opp.getEffectivePower()).isEqualTo(1);
        assertThat(opp.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures reduced to 0 toughness are destroyed")
    void killsSmallCreatures() {
        harness.addToBattlefield(player2, new GlorySeeker()); // 2/2

        castInfest();

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Glory Seeker"))).isFalse();
    }

    @Test
    @DisplayName("Effect wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GluttonousZombie()); // 3/3

        castInfest();
        assertThat(findPermanent(player1, "Gluttonous Zombie").getEffectivePower()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Gluttonous Zombie").getEffectivePower()).isEqualTo(3);
        assertThat(findPermanent(player1, "Gluttonous Zombie").getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not affect creatures that enter after it resolves")
    void doesNotAffectCreaturesEnteringAfterResolution() {
        Permanent existing = addCreatureReady(player1, new GluttonousZombie());

        castInfest();

        Permanent later = addCreatureReady(player2, new GluttonousZombie());

        assertThat(existing.getEffectivePower()).isEqualTo(1);
        assertThat(existing.getEffectiveToughness()).isEqualTo(1);
        assertThat(later.getEffectivePower()).isEqualTo(3);
        assertThat(later.getEffectiveToughness()).isEqualTo(3);
    }

    private void castInfest() {
        harness.castFromHand(player1, new Infest(), "{1}{B}{B}");
        harness.passBothPriorities();
    }
}
