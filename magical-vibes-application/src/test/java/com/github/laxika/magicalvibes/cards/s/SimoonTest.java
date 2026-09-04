package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.q.QuirionRanger;
import com.github.laxika.magicalvibes.cards.s.SisaysRing;
import com.github.laxika.magicalvibes.cards.w.Warthog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuirionRanger.class, Simoon.class, SisaysRing.class, Warthog.class})
class SimoonTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each creature the targeted opponent controls")
    void damagesOpponentsCreatures() {
        Permanent enemyBear = addCreatureReady(player2, new Warthog());
        Permanent otherEnemyBear = addCreatureReady(player2, new Warthog());

        castSimoon(player2.getId());

        assertThat(enemyBear.getMarkedDamage()).isEqualTo(1);
        assertThat(otherEnemyBear.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Kills 1-toughness creatures controlled by the opponent")
    void kills1ToughnessCreatures() {
        harness.addToBattlefield(player2, new QuirionRanger());

        castSimoon(player2.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Quirion Ranger");
    }

    @Test
    @DisplayName("Does not damage the caster's creatures or the opponent's non-creatures")
    void doesNotAffectOwnCreaturesOrNonCreatures() {
        Permanent ownBear = addCreatureReady(player1, new Warthog());
        Permanent enemyArtifact = harness.addToBattlefieldAndReturn(player2, new SisaysRing());

        castSimoon(player2.getId());

        assertThat(ownBear.getMarkedDamage()).isZero();
        assertThat(enemyArtifact.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Simoon");
    }

    @Test
    @DisplayName("Cannot target its own controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new Simoon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSimoon(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Simoon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }
}
