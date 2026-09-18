package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FlameBurst;
import com.github.laxika.magicalvibes.cards.i.IridescentAngel;
import com.github.laxika.magicalvibes.cards.k.Karma;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SphereOfTruth.class, IridescentAngel.class, FlameBurst.class, Karma.class, Swamp.class})
class SphereOfTruthTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents 2 damage from each white combat source")
    void preventsWhiteCombatDamagePerSource() {
        harness.addToBattlefield(player1, new SphereOfTruth());
        harness.setLife(player1, 20);

        addCreatureReady(player2, new IridescentAngel());
        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not prevent damage from a nonwhite source")
    void doesNotPreventNonwhiteDamage() {
        harness.addToBattlefield(player1, new SphereOfTruth());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new FlameBurst()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevents 2 damage from a white noncombat source")
    void preventsWhiteNoncombatDamage() {
        harness.addToBattlefield(player1, new SphereOfTruth());
        harness.addToBattlefield(player1, new Karma());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Prevents 2 damage from each white source")
    void preventsDamageFromEachWhiteSource() {
        harness.addToBattlefield(player1, new SphereOfTruth());
        harness.setLife(player1, 20);

        addCreatureReady(player2, new IridescentAngel());
        addCreatureReady(player2, new IridescentAngel());
        declareAttackers(player2, List.of(0, 1));
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Prevents damage only to its controller")
    void preventsDamageOnlyToItsController() {
        harness.addToBattlefield(player1, new SphereOfTruth());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new IridescentAngel());
        declareAttackers(player1, List.of(1));
        resolveCombat(player1);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }
}
