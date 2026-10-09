package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CommandingPresence.class, NyxbornCourser.class})
class CommandingPresenceTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2 and first strike")
    void boostsEnchantedCreatureAndGrantsFirstStrike() {
        Permanent creature = addEnchantedCreature();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Creates a Soldier token when the enchanted creature deals combat damage to a player")
    void createsSoldierOnCombatDamage() {
        Permanent creature = addEnchantedCreature();
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(findPermanents(player1, "Human Soldier")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Soldier token when the enchanted creature deals no combat damage to a player")
    void doesNotCreateSoldierWhenBlocked() {
        Permanent creature = addEnchantedCreature();
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new NyxbornCourser());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(findPermanents(player1, "Human Soldier")).isEmpty();
    }

    private Permanent addEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new NyxbornCourser());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CommandingPresence());
        aura.setAttachedTo(creature.getId());
        return creature;
    }

    @Test
    @DisplayName("Combat damage creates a Human Soldier token")
    void createsHumanSoldier() {
        Permanent creature = addEnchantedCreature();
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(token.getCard().getSubtypes())
                            .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
                    assertThat(token.isTapped()).isFalse();
                });
    }

    @Test
    @DisplayName("The enchanted creature's controller creates the token when the Aura has a different controller")
    void enchantedCreatureControllerCreatesToken() {
        Permanent creature = addCreatureReady(player2, new NyxbornCourser());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CommandingPresence());
        aura.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }
}
