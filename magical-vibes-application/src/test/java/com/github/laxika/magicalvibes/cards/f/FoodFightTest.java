package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FoodFight.class, Spellbook.class, GrizzlyBears.class})
class FoodFightTest extends BaseCardTest {

    @Test
    @DisplayName("Food Fight gives your artifacts the ability to sacrifice themselves for damage")
    void artifactCanSacrificeForDamage() {
        Permanent foodFight = addFoodFight(player1);
        Permanent artifact = addArtifact(player1);
        Permanent target = addCreature(player2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        activateArtifact(artifact, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(foodFight);
    }

    @Test
    @DisplayName("Food Fight damage counts all Food Fights you control")
    void damageScalesWithFoodFightsYouControl() {
        addFoodFight(player1);
        addFoodFight(player1);
        Permanent artifact = addArtifact(player1);

        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        activateArtifact(artifact, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Food Fight does not grant the ability to an opponent's artifacts")
    void doesNotGrantAbilityToOpponentsArtifacts() {
        addFoodFight(player1);
        Permanent opponentArtifact = addArtifact(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(opponentArtifact), null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    private Permanent addFoodFight(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new FoodFight());
    }

    private Permanent addArtifact(com.github.laxika.magicalvibes.model.Player player) {
        Permanent artifact = harness.addToBattlefieldAndReturn(player, new Spellbook());
        artifact.setSummoningSick(false);
        return artifact;
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private void activateArtifact(Permanent artifact, java.util.UUID targetId) {
        int artifactIndex = gd.playerBattlefields.get(player1.getId()).indexOf(artifact);
        harness.activateAbility(player1, artifactIndex, null, targetId);
        harness.passBothPriorities();
    }
}
