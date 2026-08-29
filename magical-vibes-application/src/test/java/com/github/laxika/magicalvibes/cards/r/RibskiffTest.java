package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RibskiffTest extends BaseCardTest {

    @Test
    void enteringBattlefieldDrawsACard() {
        harness.setHand(player1, List.of(new Ribskiff()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 1);
    }

    @Test
    void crewAnimatesRibskiffAndTapsCrew() {
        Permanent ribskiff = addRibskiffReady(player1);
        Permanent crew = addCreatureReady(player1, new SerraAngel());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ribskiff.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, ribskiff)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void toxicDealsTwoPoisonCountersOnCombatDamage() {
        Permanent ribskiff = addRibskiffReady(player1);
        addCreatureReady(player1, new SerraAngel());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        ribskiff.setAttacking(true);

        resolveCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    void cannotCrewWithoutEnoughPower() {
        addRibskiffReady(player1);
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    private Permanent addRibskiffReady(Player player) {
        Permanent permanent = new Permanent(new Ribskiff());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

}
