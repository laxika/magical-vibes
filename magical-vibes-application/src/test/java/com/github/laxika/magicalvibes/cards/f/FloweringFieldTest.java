package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CoastalHornclaw;
import com.github.laxika.magicalvibes.cards.d.DivingGriffin;
import com.github.laxika.magicalvibes.cards.p.PygmyRazorback;
import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FloweringField.class, RhysticCave.class, CoastalHornclaw.class, DivingGriffin.class,
        PygmyRazorback.class})
class FloweringFieldTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land can tap to prevent the next damage to a player")
    void enchantedLandPreventsNextDamageToPlayer() {
        Permanent land = setUpEnchantedLand();
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new CoastalHornclaw());

        activatePreventionAbility(land, player2.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Enchanted land can tap to prevent the next damage to a creature")
    void enchantedLandPreventsNextDamageToCreature() {
        Permanent land = setUpEnchantedLand();
        Permanent attacker = addCreatureReady(player1, new PygmyRazorback());
        Permanent blocker = addCreatureReady(player2, new DivingGriffin());

        activatePreventionAbility(land, blocker.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Flowering Field can enchant a land")
    void canEnchantLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        harness.setHand(player1, List.of(new FloweringField()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof FloweringField
                        && land.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Granted prevention ability cannot target a land")
    void preventionAbilityCannotTargetLand() {
        Permanent land = setUpEnchantedLand();
        Permanent otherLand = harness.addToBattlefieldAndReturn(player2, new RhysticCave());

        assertThatThrownBy(() -> activatePreventionAbility(land, otherLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature, planeswalker, battle, or player");
    }

    @Test
    @DisplayName("Granted ability disappears when Flowering Field leaves the battlefield")
    void grantedAbilityDisappearsWhenAuraLeaves() {
        Permanent land = setUpEnchantedLand();
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof FloweringField);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(land), 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid ability index");
    }

    @Test
    @DisplayName("Flowering Field cannot enchant a creature")
    void cannotEnchantCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DivingGriffin());
        harness.setHand(player1, List.of(new FloweringField()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    private Permanent setUpEnchantedLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FloweringField());
        aura.setAttachedTo(land.getId());
        return land;
    }

    private void activatePreventionAbility(Permanent land, UUID targetId) {
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(land), 1, null, targetId);
    }
}
