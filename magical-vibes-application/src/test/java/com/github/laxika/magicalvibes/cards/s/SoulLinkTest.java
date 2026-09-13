package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RazorfinHunter;
import com.github.laxika.magicalvibes.cards.y.YavimayaCoast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulLink.class, RazorfinHunter.class, YavimayaCoast.class})
class SoulLinkTest extends BaseCardTest {

    @Test
    @DisplayName("You gain life equal to combat damage dealt by the enchanted creature")
    void gainsLifeFromCombatDamageDealtByEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new RazorfinHunter());
        castSoulLink(creature);

        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("You gain life equal to noncombat damage dealt by the enchanted creature")
    void gainsLifeFromNoncombatDamageDealtByEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new RazorfinHunter());
        castSoulLink(creature);

        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(creature),
                null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("You gain life equal to damage dealt to the enchanted creature")
    void gainsLifeFromDamageDealtToEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new RazorfinHunter());
        Permanent damageSource = addCreatureReady(player1, new RazorfinHunter());
        castSoulLink(creature);

        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(damageSource),
                null, creature.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Both abilities trigger when the enchanted creature deals damage to itself")
    void bothAbilitiesTriggerWhenEnchantedCreatureDealsDamageToItself() {
        Permanent creature = addCreatureReady(player1, new RazorfinHunter());
        castSoulLink(creature);

        harness.setLife(player1, 10);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(creature),
                null, creature.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        harness.addToBattlefield(player2, new RazorfinHunter());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new YavimayaCoast());
        harness.setHand(player1, List.of(new SoulLink()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castSoulLink(Permanent creature) {
        harness.setHand(player1, List.of(new SoulLink()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
