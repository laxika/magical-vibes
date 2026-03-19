package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerAndBoostSelfByManaValueEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MindshriekerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has activated ability with correct structure")
    void hasCorrectAbility() {
        Mindshrieker card = new Mindshrieker();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{2}");
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(MillTargetPlayerAndBoostSelfByManaValueEffect.class);
    }

    // ===== Activation =====

    @Test
    @DisplayName("Activating ability targeting a player puts it on the stack")
    void activatingTargetingPlayerPutsOnStack() {
        addMindshrieker(player1);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Mindshrieker");
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumed() {
        addMindshrieker(player1);
        addActivationMana(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    // ===== Mill + boost =====

    @Test
    @DisplayName("Mills one card and boosts by milled card's mana value")
    void millsOneCardAndBoosts() {
        Permanent mindshrieker = addMindshrieker(player1);
        addActivationMana(player1);

        // Set up opponent library with a known card: HillGiant (MV 4)
        gd.playerDecks.put(player2.getId(), new ArrayList<>(List.of(new HillGiant())));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Mindshrieker should get +4/+4 (base 1/1 + modifier 4/4)
        assertThat(mindshrieker.getPowerModifier()).isEqualTo(4);
        assertThat(mindshrieker.getToughnessModifier()).isEqualTo(4);
        // Opponent's library should be empty
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        // Milled card should be in graveyard
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Boost is zero when milled card has mana value 0")
    void boostIsZeroForLand() {
        Permanent mindshrieker = addMindshrieker(player1);
        addActivationMana(player1);

        // Forest has MV 0
        gd.playerDecks.put(player2.getId(), new ArrayList<>(List.of(new Forest())));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(mindshrieker.getPowerModifier()).isEqualTo(0);
        assertThat(mindshrieker.getToughnessModifier()).isEqualTo(0);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Boost equals exact mana value of milled card")
    void boostEqualsExactManaValue() {
        Permanent mindshrieker = addMindshrieker(player1);
        addActivationMana(player1);

        // GrizzlyBears has MV 2
        gd.playerDecks.put(player2.getId(), new ArrayList<>(List.of(new GrizzlyBears())));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(mindshrieker.getPowerModifier()).isEqualTo(2);
        assertThat(mindshrieker.getToughnessModifier()).isEqualTo(2);
    }

    // ===== Can target self =====

    @Test
    @DisplayName("Can target yourself to mill your own library")
    void canTargetSelf() {
        Permanent mindshrieker = addMindshrieker(player1);
        addActivationMana(player1);

        // Set up own library with Shock (MV 1)
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new Shock())));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(mindshrieker.getPowerModifier()).isEqualTo(1);
        assertThat(mindshrieker.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    // ===== Multiple activations =====

    @Test
    @DisplayName("Multiple activations stack boosts")
    void multipleActivationsStackBoosts() {
        Permanent mindshrieker = addMindshrieker(player1);
        addActivationMana(player1);
        addActivationMana(player1);

        // GrizzlyBears (MV 2) + Shock (MV 1)
        gd.playerDecks.put(player2.getId(), new ArrayList<>(List.of(
                new GrizzlyBears(), new Shock()
        )));

        // First activation — mills GrizzlyBears (MV 2)
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Second activation — mills Shock (MV 1)
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Total boost: +2/+2 + +1/+1 = +3/+3
        assertThat(mindshrieker.getPowerModifier()).isEqualTo(3);
        assertThat(mindshrieker.getToughnessModifier()).isEqualTo(3);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    // ===== Edge cases =====

    @Test
    @DisplayName("Does nothing when target player's library is empty")
    void doesNothingWhenLibraryEmpty() {
        Permanent mindshrieker = addMindshrieker(player1);
        addActivationMana(player1);

        gd.playerDecks.put(player2.getId(), new ArrayList<>());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(mindshrieker.getPowerModifier()).isEqualTo(0);
        assertThat(mindshrieker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Mills only one card even when library has many")
    void millsOnlyOneCard() {
        addMindshrieker(player1);
        addActivationMana(player1);

        gd.playerDecks.put(player2.getId(), new ArrayList<>(List.of(
                new Shock(), new GrizzlyBears(), new HillGiant()
        )));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Only 1 card milled, 2 remain
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    // ===== Helpers =====

    private Permanent addMindshrieker(Player player) {
        Mindshrieker card = new Mindshrieker();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
