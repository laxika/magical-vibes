package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CloudDjinn;
import com.github.laxika.magicalvibes.cards.r.RedwoodTreefolk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloudDjinn.class, Downdraft.class, DuskriderFalcon.class, RedwoodTreefolk.class})
class DowndraftTest extends BaseCardTest {

    @Test
    @DisplayName("{G} strips flying from a target creature until end of turn")
    void stripsFlyingUntilEndOfTurn() {
        harness.addToBattlefield(player1, new Downdraft());
        harness.addToBattlefield(player2, new CloudDjinn());
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent target = findPermanent(player2, "Cloud Djinn");
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Sacrificing deals 2 damage to each creature with flying only")
    void sacrificeDamagesFlyersOnly() {
        harness.addToBattlefield(player1, new Downdraft());
        harness.addToBattlefield(player1, new DuskriderFalcon());
        harness.addToBattlefield(player2, new CloudDjinn());
        harness.addToBattlefield(player2, new RedwoodTreefolk());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Downdraft");
        harness.assertInGraveyard(player1, "Duskrider Falcon");
        harness.assertOnBattlefield(player2, "Cloud Djinn");
        assertThat(findPermanent(player2, "Cloud Djinn").getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanent(player2, "Redwood Treefolk").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A creature that lost flying is not damaged by the sacrifice")
    void strippedCreatureAvoidsTheDamage() {
        harness.addToBattlefield(player1, new Downdraft());
        harness.addToBattlefield(player2, new CloudDjinn());
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent target = findPermanent(player2, "Cloud Djinn");
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Sacrificing does not damage players")
    void sacrificeDoesNotDamagePlayers() {
        harness.addToBattlefield(player1, new Downdraft());
        harness.addToBattlefield(player2, new CloudDjinn());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The flying-removal ability can target a creature without flying")
    void canTargetCreatureWithoutFlying() {
        harness.addToBattlefield(player1, new Downdraft());
        harness.addToBattlefield(player2, new RedwoodTreefolk());
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent target = findPermanent(player2, "Redwood Treefolk");
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Redwood Treefolk");
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice is paid immediately and the earlier removal ability still resolves")
    void removalResolvesAfterSourceIsSacrificed() {
        harness.addToBattlefield(player1, new Downdraft());
        harness.addToBattlefield(player2, new CloudDjinn());
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent target = findPermanent(player2, "Cloud Djinn");
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertInGraveyard(player1, "Downdraft");
        harness.assertNotOnBattlefield(player1, "Downdraft");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        harness.passBothPriorities();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Flying is checked when the sacrifice ability resolves")
    void flyingLostInResponseAvoidsDamage() {
        harness.addToBattlefield(player1, new Downdraft());
        harness.addToBattlefield(player2, new Downdraft());
        harness.addToBattlefield(player2, new DuskriderFalcon());
        harness.addMana(player2, ManaColor.GREEN, 1);

        Permanent target = findPermanent(player2, "Duskrider Falcon");
        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player2, 0, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Duskrider Falcon");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Downdraft");
    }
}
