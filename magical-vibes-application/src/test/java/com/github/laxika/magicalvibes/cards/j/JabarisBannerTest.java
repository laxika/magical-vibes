package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.d.DuskriderFalcon;
import com.github.laxika.magicalvibes.cards.r.RedwoodTreefolk;
import com.github.laxika.magicalvibes.cards.s.ShadowRider;
import com.github.laxika.magicalvibes.cards.w.WindingCanyons;
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

@CardUsed({JabarisBanner.class, RedwoodTreefolk.class, DuskriderFalcon.class, ShadowRider.class,
        WindingCanyons.class})
class JabarisBannerTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants flanking to target creature")
    void grantsFlanking() {
        harness.addToBattlefield(player1, new JabarisBanner());
        Permanent treefolk = harness.addToBattlefieldAndReturn(player1, new RedwoodTreefolk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, treefolk.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, treefolk, Keyword.FLANKING)).isTrue();
    }

    @Test
    @DisplayName("Granted flanking wears off at end of turn")
    void flankingWearsOff() {
        harness.addToBattlefield(player1, new JabarisBanner());
        Permanent treefolk = harness.addToBattlefieldAndReturn(player1, new RedwoodTreefolk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, treefolk.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, treefolk, Keyword.FLANKING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, treefolk, Keyword.FLANKING)).isFalse();
    }

    @Test
    @DisplayName("Granted flanking triggers against a blocker without flanking")
    void grantedFlankingTriggers() {
        harness.addToBattlefield(player1, new JabarisBanner());
        Permanent treefolk = addCreatureReady(player1, new RedwoodTreefolk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, treefolk.getId());
        harness.passBothPriorities();

        treefolk.setAttacking(true);
        addCreatureReady(player2, new DuskriderFalcon());

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treefolk);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, attackerIndex)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Duskrider Falcon");
    }

    @Test
    @DisplayName("Ability can only target creatures")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new JabarisBanner());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new WindingCanyons());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability can target a creature controlled by an opponent")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new JabarisBanner());
        Permanent falcon = harness.addToBattlefieldAndReturn(player2, new DuskriderFalcon());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, falcon.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, falcon, Keyword.FLANKING)).isTrue();
    }

    @Test
    @DisplayName("Each flanking instance triggers separately")
    void multipleFlankingInstancesTriggerSeparately() {
        harness.addToBattlefield(player1, new JabarisBanner());
        Permanent attacker = addCreatureReady(player1, new ShadowRider());
        Permanent blocker = addCreatureReady(player2, new RedwoodTreefolk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, attacker.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, attackerIndex)));
        resolveAllTriggers();

        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(4);
    }
}
