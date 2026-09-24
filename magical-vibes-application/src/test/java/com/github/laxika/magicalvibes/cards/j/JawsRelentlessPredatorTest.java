package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BasilicaSkullbomb;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.q.QasaliPridemage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JawsRelentlessPredator.class, BasilicaSkullbomb.class, QasaliPridemage.class,
        LeoninScimitar.class})
class JawsRelentlessPredatorTest extends BaseCardTest {

    @Test
    @DisplayName("Creates Blood tokens equal to combat damage dealt to a player")
    void createsBloodTokensEqualToCombatDamage() {
        Permanent jaws = addCreatureReady(player1, new JawsRelentlessPredator());
        jaws.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Deals 1 damage to each opponent when a noncreature artifact is sacrificed")
    void damagesOpponentsWhenArtifactIsSacrificed() {
        addCreatureReady(player1, new JawsRelentlessPredator());
        harness.addToBattlefield(player1, new BasilicaSkullbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to each opponent when a noncreature artifact is destroyed")
    void damagesOpponentsWhenArtifactIsDestroyed() {
        addCreatureReady(player1, new JawsRelentlessPredator());
        addCreatureReady(player1, new QasaliPridemage());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, scimitar.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
