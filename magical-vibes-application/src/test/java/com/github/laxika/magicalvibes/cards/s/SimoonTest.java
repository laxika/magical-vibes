package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChromaticSphere;
import com.github.laxika.magicalvibes.cards.l.LlanowarElite;
import com.github.laxika.magicalvibes.cards.n.NoblePanther;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChromaticSphere.class, LlanowarElite.class, NoblePanther.class, Simoon.class})
class SimoonTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each creature the targeted opponent controls")
    void damagesOpponentsCreatures() {
        Permanent enemyPanther = addCreatureReady(player2, new NoblePanther());
        Permanent otherEnemyPanther = addCreatureReady(player2, new NoblePanther());

        castSimoon(player2.getId());

        assertThat(enemyPanther.getMarkedDamage()).isEqualTo(1);
        assertThat(otherEnemyPanther.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Kills 1-toughness creatures controlled by the opponent")
    void kills1ToughnessCreatures() {
        harness.addToBattlefield(player2, new LlanowarElite());

        castSimoon(player2.getId());

        harness.assertNotOnBattlefield(player2, "Llanowar Elite");
        harness.assertInGraveyard(player2, "Llanowar Elite");
    }

    @Test
    @DisplayName("Does not damage the caster's creatures or the opponent's non-creatures")
    void doesNotAffectOwnCreaturesOrNonCreatures() {
        Permanent ownPanther = addCreatureReady(player1, new NoblePanther());
        Permanent enemyArtifact = harness.addToBattlefieldAndReturn(player2, new ChromaticSphere());

        castSimoon(player2.getId());

        assertThat(ownPanther.getMarkedDamage()).isZero();
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

    @Test
    @DisplayName("Resolves against an opponent with no creatures without damaging either player")
    void resolvesAgainstOpponentWithNoCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castSimoon(player2.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Simoon");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an opponent's creature directly")
    void cannotTargetCreature() {
        Permanent enemyPanther = addCreatureReady(player2, new NoblePanther());
        harness.setHand(player1, List.of(new Simoon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enemyPanther.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damages creatures that enter after casting but before resolution")
    void damagesCreaturesPresentAtResolution() {
        harness.setHand(player1, List.of(new Simoon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, player2.getId());

        Permanent latePanther = harness.enterBattlefieldAndReturn(player2, new NoblePanther());
        harness.passBothPriorities();

        assertThat(latePanther.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Noble Panther");
        harness.assertInGraveyard(player1, "Simoon");
    }

    private void castSimoon(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Simoon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, targetPlayerId);
    }
}
