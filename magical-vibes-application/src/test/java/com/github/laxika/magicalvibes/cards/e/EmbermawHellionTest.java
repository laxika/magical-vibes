package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.ConsumeSpirit;
import com.github.laxika.magicalvibes.cards.f.FurnaceOfRath;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmbermawHellion.class, Shock.class, ConsumeSpirit.class, SerraAngel.class, RagingGoblin.class,
        TurnToFrog.class, ProdigalPyromancer.class, FurnaceOfRath.class})
class EmbermawHellionTest extends BaseCardTest {

    @Test
    @DisplayName("Red spell you control deals 1 extra damage to a player")
    void redSpellDealsExtraDamageToPlayer() {
        harness.addToBattlefield(player1, new EmbermawHellion());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Shock deals 2 + 1 = 3
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Red spell you control deals 1 extra damage to a creature")
    void redSpellDealsExtraDamageToCreature() {
        harness.addToBattlefield(player1, new EmbermawHellion());
        harness.addToBattlefield(player2, new SerraAngel()); // 4/4
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Serra Angel"));
        harness.passBothPriorities();
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Serra Angel"));
        harness.passBothPriorities();

        // Two boosted Shocks deal 3 + 3 = 6 to a 4/4
        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Nonred spell you control gets no bonus")
    void nonRedSpellGetsNoBonus() {
        harness.addToBattlefield(player1, new EmbermawHellion());
        harness.setHand(player1, List.of(new ConsumeSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Another red creature you control deals 1 extra combat damage")
    void anotherRedCreatureDealsExtraCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EmbermawHellion());
        addCreatureReady(player1, new RagingGoblin()); // 1/1

        declareAttackers(player1, List.of(1));

        // Raging Goblin deals 1 + 1 = 2
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Embermaw Hellion does not boost its own combat damage")
    void doesNotBoostItsOwnCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EmbermawHellion()); // 4/5

        declareAttackers(player1, List.of(0));

        // "another red source" — the Hellion's own 4 damage is unchanged
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Two Embermaw Hellions each boost the other's combat damage")
    void twoHellionsBoostEachOther() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EmbermawHellion());
        addCreatureReady(player1, new EmbermawHellion());

        declareAttackers(player1, List.of(0));

        // The attacking Hellion deals 4 + 1 = 5 (boosted only by the other one)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Opponent's red source is not boosted")
    void opponentsRedSourceNotBoosted() {
        harness.addToBattlefield(player1, new EmbermawHellion());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Multiple Hellions each increase damage from another red spell")
    void multipleHellionsBoostSpellDamage() {
        harness.addToBattlefield(player1, new EmbermawHellion());
        harness.addToBattlefield(player1, new EmbermawHellion());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("The bonus also applies to damage dealt to your own permanents")
    void boostsDamageToOwnCreature() {
        harness.addToBattlefield(player1, new EmbermawHellion());
        var angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, angel.getId());
        harness.passBothPriorities();

        assertThat(angel.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Serra Angel");
    }

    @Test
    @DisplayName("Another red permanent's activated ability deals extra damage")
    void boostsActivatedAbilityDamage() {
        harness.addToBattlefield(player1, new EmbermawHellion());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A Hellion that has lost all abilities no longer increases damage")
    void losingAbilitiesDisablesBonus() {
        var hellion = harness.addToBattlefieldAndReturn(player1, new EmbermawHellion());
        harness.setHand(player1, List.of(new TurnToFrog(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, hellion.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A red creature changed to blue no longer gets extra combat damage")
    void changedColorSourceGetsNoCombatBonus() {
        harness.addToBattlefield(player1, new EmbermawHellion());
        var goblin = addCreatureReady(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, goblin.getId());
        harness.passBothPriorities();
        declareAttackers(player1, List.of(1));

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("An ability uses its source's color when damage is dealt")
    void changedColorSourceGetsNoAbilityBonus() {
        harness.addToBattlefield(player1, new EmbermawHellion());
        var pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.castInstant(player1, 0, pyromancer.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Embermaw Hellion tramples over a blocker without boosting itself")
    void tramplesWithoutSelfBonus() {
        addCreatureReady(player1, new EmbermawHellion());
        var blocker = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 1, player2.getId(), 3));

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Raging Goblin");
        harness.assertOnBattlefield(player1, "Embermaw Hellion");
    }

    @Test
    @DisplayName("The damaged player chooses the order of Hellion and Furnace replacements")
    void damagedPlayerChoosesReplacementOrder() {
        harness.addToBattlefield(player1, new EmbermawHellion());
        harness.addToBattlefield(player1, new FurnaceOfRath());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // The two legal orders deal 5 or 6 damage; the recipient must choose first.
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.assertLife(player2, 20);
    }
}
