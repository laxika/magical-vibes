package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Soliton.class})
class SolitonTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Soliton puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new Soliton()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Soliton");
    }

    @Test
    @DisplayName("Resolving puts Soliton onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new Soliton()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Soliton");
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Soliton()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Activating ability puts UntapSelf on the stack")
    void activatingAbilityPutsOnStack() {
        addCreatureReady(player1, new Soliton());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Soliton");
    }

    @Test
    @DisplayName("Resolving ability untaps Soliton")
    void resolvingAbilityUntapsSelf() {
        Permanent solitonPerm = addCreatureReady(player1, new Soliton());
        solitonPerm.tap();
        assertThat(solitonPerm.isTapped()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(solitonPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability when already untapped")
    void canActivateWhenAlreadyUntapped() {
        addCreatureReady(player1, new Soliton());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent soliton = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(soliton.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability multiple times if mana allows")
    void canActivateMultipleTimes() {
        Permanent solitonPerm = addCreatureReady(player1, new Soliton());
        solitonPerm.tap();
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(solitonPerm.isTapped()).isFalse();

        // Tap it again manually
        solitonPerm.tap();
        assertThat(solitonPerm.isTapped()).isTrue();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(solitonPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activating ability does NOT tap the permanent")
    void activatingAbilityDoesNotTap() {
        addCreatureReady(player1, new Soliton());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        Permanent soliton = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(soliton.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addCreatureReady(player1, new Soliton());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new Soliton());
        // No mana added

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability resolves without effect if Soliton is removed before resolution")
    void abilityResolvesWithoutEffectIfSourceRemoved() {
        addCreatureReady(player1, new Soliton());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        // Remove Soliton before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Summoning sickness does not prevent the untap ability")
    void canUntapWhileSummoningSick() {
        Permanent soliton = harness.addToBattlefieldAndReturn(player1, new Soliton());
        soliton.setSummoningSick(true);
        soliton.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(soliton.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(soliton.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Colorless mana cannot pay the blue activation cost")
    void cannotActivateWithOnlyColorlessMana() {
        Permanent soliton = addCreatureReady(player1, new Soliton());
        soliton.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(soliton.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the Soliton whose ability was activated is untapped")
    void untapsOnlyItsSource() {
        Permanent source = addCreatureReady(player1, new Soliton());
        Permanent other = addCreatureReady(player1, new Soliton());
        Permanent opposing = addCreatureReady(player2, new Soliton());
        source.tap();
        other.tap();
        opposing.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(source.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(source.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
        assertThat(opposing.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An old activation cannot untap the source after it leaves and returns")
    void doesNotUntapReturnedSource() {
        Permanent source = addCreatureReady(player1, new Soliton());
        source.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(source);
        Permanent returned = harness.addToBattlefieldAndReturn(player1, source.getCard());
        returned.tap();
        harness.passBothPriorities();

        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Unblocked Soliton deals 3 damage to defending player")
    void dealsThreeDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent atkPerm = addCreatureReady(player1, new Soliton());
        atkPerm.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

}
