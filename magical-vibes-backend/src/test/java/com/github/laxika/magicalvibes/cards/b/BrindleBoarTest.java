package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrindleBoarTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Brindle Boar has correct activated ability structure")
    void hasCorrectAbilityStructure() {
        BrindleBoar card = new BrindleBoar();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(GainLifeEffect.class);

        GainLifeEffect gainLife = (GainLifeEffect) ability.getEffects().get(1);
        assertThat(gainLife.amount()).isEqualTo(4);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Brindle Boar puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new BrindleBoar()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Brindle Boar");
    }

    @Test
    @DisplayName("Resolving Brindle Boar puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new BrindleBoar()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Brindle Boar"));
    }

    // ===== Sacrifice ability =====

    @Test
    @DisplayName("Sacrificing Brindle Boar puts ability on stack and moves it to graveyard")
    void sacrificingPutsAbilityOnStackAndMovesToGraveyard() {
        addBrindleBoarReady(player1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();

        // Brindle Boar should be sacrificed (moved to graveyard as a cost)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Brindle Boar"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Brindle Boar"));

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Brindle Boar");
    }

    @Test
    @DisplayName("Resolving the ability gains 4 life")
    void resolvingAbilityGains4Life() {
        addBrindleBoarReady(player1);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 4);
    }

    @Test
    @DisplayName("Ability has no mana cost — can activate without mana")
    void canActivateWithoutMana() {
        addBrindleBoarReady(player1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Ability does not require tapping")
    void abilityDoesNotRequireTap() {
        Permanent boar = addBrindleBoarReady(player1);
        boar.tap();

        // Should still be able to activate even when tapped
        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
    }

    // ===== Helper methods =====

    private Permanent addBrindleBoarReady(Player player) {
        BrindleBoar card = new BrindleBoar();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
