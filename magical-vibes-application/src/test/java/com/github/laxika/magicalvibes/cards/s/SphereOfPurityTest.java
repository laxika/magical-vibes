package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GraniteShard;
import com.github.laxika.magicalvibes.cards.m.MyrEnforcer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SphereOfPurity.class, GraniteShard.class, ShrapnelBlast.class, MyrEnforcer.class})
class SphereOfPurityTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents 1 damage from an artifact source")
    void preventsDamageFromArtifactSource() {
        harness.addToBattlefield(player1, new SphereOfPurity());
        harness.addToBattlefield(player2, new GraniteShard());
        harness.setLife(player1, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not prevent damage from a non-artifact source")
    void doesNotPreventNonArtifactDamage() {
        harness.addToBattlefield(player1, new SphereOfPurity());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new ShrapnelBlast()));
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new GraniteShard());
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player2, 0, player1.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Prevents 1 of combat damage from an artifact creature")
    void preventsArtifactCombatDamage() {
        harness.addToBattlefield(player1, new SphereOfPurity());
        harness.setLife(player1, 20);

        addCreatureReady(player2, new MyrEnforcer());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Prevents 1 damage from each artifact source in combat")
    void preventsDamageFromEachArtifactSource() {
        harness.addToBattlefield(player1, new SphereOfPurity());
        harness.setLife(player1, 20);

        addCreatureReady(player2, new MyrEnforcer());
        addCreatureReady(player2, new MyrEnforcer());

        declareAttackersAndPrepareBlockers(player2, List.of(0, 1));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
    }
}
