package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AddManaPerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CabalStrongholdTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Cabal Stronghold has two activated abilities")
    void hasCorrectAbilities() {
        CabalStronghold card = new CabalStronghold();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        ActivatedAbility colorless = card.getActivatedAbilities().get(0);
        assertThat(colorless.isRequiresTap()).isTrue();
        assertThat(colorless.getManaCost()).isNull();
        assertThat(colorless.getEffects()).hasSize(1);
        assertThat(colorless.getEffects().getFirst()).isInstanceOf(AwardManaEffect.class);

        ActivatedAbility swampMana = card.getActivatedAbilities().get(1);
        assertThat(swampMana.isRequiresTap()).isTrue();
        assertThat(swampMana.getManaCost()).isEqualTo("{3}");
        assertThat(swampMana.getEffects()).hasSize(1);
        assertThat(swampMana.getEffects().getFirst()).isInstanceOf(AddManaPerControlledPermanentEffect.class);
    }

    // ===== First ability: {T}: Add {C} =====

    @Test
    @DisplayName("First ability taps for colorless mana")
    void firstAbilityAddsColorless() {
        harness.addToBattlefield(player1, new CabalStronghold());

        Permanent stronghold = gd.playerBattlefields.get(player1.getId()).getFirst();
        stronghold.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    // ===== Second ability: {3}, {T}: Add {B} for each basic Swamp you control =====

    @Test
    @DisplayName("Second ability adds B for each basic Swamp controlled")
    void secondAbilityAddsBlackPerBasicSwamp() {
        harness.addToBattlefield(player1, new CabalStronghold());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());

        Permanent stronghold = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Cabal Stronghold"))
                .findFirst().orElseThrow();
        stronghold.setSummoningSick(false);

        int strongholdIdx = gd.playerBattlefields.get(player1.getId()).indexOf(stronghold);

        // Pay {3} mana cost
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, strongholdIdx, 1, null, null);

        // 3 basic Swamps = 3 black mana
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
    }

    @Test
    @DisplayName("Second ability with no Swamps adds zero black mana")
    void secondAbilityWithNoSwampsAddsNothing() {
        harness.addToBattlefield(player1, new CabalStronghold());

        Permanent stronghold = gd.playerBattlefields.get(player1.getId()).getFirst();
        stronghold.setSummoningSick(false);

        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }

    @Test
    @DisplayName("Second ability does not count non-basic lands with Swamp subtype")
    void secondAbilityDoesNotCountNonBasicSwamps() {
        harness.addToBattlefield(player1, new CabalStronghold());

        // Add a basic Swamp
        harness.addToBattlefield(player1, new Swamp());

        // Add a non-basic Swamp (simulate by stripping BASIC supertype)
        Swamp nonBasicSwamp = new Swamp();
        nonBasicSwamp.setSupertypes(Set.of());
        harness.addToBattlefield(player1, nonBasicSwamp);

        Permanent stronghold = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Cabal Stronghold"))
                .findFirst().orElseThrow();
        stronghold.setSummoningSick(false);

        int strongholdIdx = gd.playerBattlefields.get(player1.getId()).indexOf(stronghold);

        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, strongholdIdx, 1, null, null);

        // Only 1 basic Swamp counted, non-basic Swamp ignored
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability does not count opponent's basic Swamps")
    void secondAbilityDoesNotCountOpponentSwamps() {
        harness.addToBattlefield(player1, new CabalStronghold());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        Permanent stronghold = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Cabal Stronghold"))
                .findFirst().orElseThrow();
        stronghold.setSummoningSick(false);

        int strongholdIdx = gd.playerBattlefields.get(player1.getId()).indexOf(stronghold);

        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, strongholdIdx, 1, null, null);

        // Only 1 Swamp owned by player1, opponent's 2 Swamps not counted
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }
}
