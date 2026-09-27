package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BloodRites;
import com.github.laxika.magicalvibes.cards.f.Frostwielder;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reciprocate.class, WanderingOnes.class, Frostwielder.class, BloodRites.class})
class ReciprocateTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature that dealt damage to you this turn")
    void exilesCreatureThatDealtDamageToYou() {
        Permanent creature = addCreatureReady(player2, new WanderingOnes());
        gd.combatDamageToPlayersThisTurn
                .computeIfAbsent(creature.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(player1.getId());

        castReciprocate(player1, creature.getId());

        harness.assertNotOnBattlefield(player2, "Wandering Ones");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Wandering Ones"));
    }

    @Test
    @DisplayName("Cannot target a creature that dealt damage only to another player")
    void cannotTargetCreatureThatDealtDamageToAnotherPlayer() {
        Permanent creature = addCreatureReady(player2, new WanderingOnes());
        gd.combatDamageToPlayersThisTurn
                .computeIfAbsent(creature.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(player2.getId());

        assertThatThrownBy(() -> castReciprocate(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature that dealt no damage this turn")
    void cannotTargetCreatureThatDealtNoDamage() {
        Permanent creature = addCreatureReady(player2, new WanderingOnes());

        assertThatThrownBy(() -> castReciprocate(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiles a creature that dealt noncombat damage to you this turn")
    void exilesCreatureThatDealtNoncombatDamageToYou() {
        Permanent frostwielder = addCreatureReady(player2, new Frostwielder());

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 19);

        castReciprocate(player1, frostwielder.getId());

        harness.assertNotOnBattlefield(player2, "Frostwielder");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Frostwielder"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent that dealt damage to you this turn")
    void cannotTargetNoncreaturePermanent() {
        Permanent bloodRites = harness.addToBattlefieldAndReturn(player2, new BloodRites());
        harness.addToBattlefield(player2, new WanderingOnes());
        harness.addMana(player2, ManaColor.RED, 2);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 18);

        assertThatThrownBy(() -> castReciprocate(player1, bloodRites.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castReciprocate(com.github.laxika.magicalvibes.model.Player caster, UUID targetId) {
        harness.setHand(caster, List.of(new Reciprocate()));
        harness.addMana(caster, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(caster, 0, targetId);
    }
}
