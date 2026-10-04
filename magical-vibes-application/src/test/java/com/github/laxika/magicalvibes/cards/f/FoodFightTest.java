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

    @Test
    void opponentsFoodFightsDoNotIncreaseDamage() {
        addFoodFight(player1);
        addFoodFight(player2);
        addFoodFight(player2);
        Permanent artifact = addArtifact(player1);

        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        activateArtifact(artifact, player2.getId());

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    void damageCountsFoodFightsAtResolution() {
        addFoodFight(player1);
        Permanent artifact = addArtifact(player1);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(artifact), null, player2.getId());

        addFoodFight(player1);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    void abilityStillDealsOneDamageAfterLastFoodFightLeaves() {
        Permanent foodFight = addFoodFight(player1);
        Permanent artifact = addArtifact(player1);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(artifact), null, player2.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, foodFight));
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Food Fight");
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    void nonartifactCreatureDoesNotReceiveAbility() {
        addFoodFight(player1);
        Permanent creature = addCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(creature), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    void tappedArtifactCanActivateAndTargetItsController() {
        addFoodFight(player1);
        Permanent artifact = addArtifact(player1);
        artifact.setTapped(true);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateArtifact(artifact, player1.getId());

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    void cannotActivateWithoutTwoMana() {
        addFoodFight(player1);
        Permanent artifact = addArtifact(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(artifact), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        harness.assertNotInGraveyard(player1, "Spellbook");
    }

    @Test
    void cannotTargetNoncreatureArtifact() {
        addFoodFight(player1);
        Permanent artifact = addArtifact(player1);
        Permanent target = addArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(artifact), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    private Permanent addFoodFight(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new FoodFight());
    }

    private Permanent addArtifact(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new Spellbook());
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    private void activateArtifact(Permanent artifact, java.util.UUID targetId) {
        int artifactIndex = gd.playerBattlefields.get(player1.getId()).indexOf(artifact);
        harness.activateAbility(player1, artifactIndex, null, targetId);
        harness.passBothPriorities();
    }
}
