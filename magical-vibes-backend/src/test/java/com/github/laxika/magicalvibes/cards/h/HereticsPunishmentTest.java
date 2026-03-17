package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndDealDamageByHighestManaValueEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HereticsPunishmentTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has activated ability with correct structure")
    void hasCorrectAbility() {
        HereticsPunishment card = new HereticsPunishment();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{3}{R}");
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(MillControllerAndDealDamageByHighestManaValueEffect.class);
    }

    // ===== Activation =====

    @Test
    @DisplayName("Activating ability targeting a player puts it on the stack")
    void activatingTargetingPlayerPutsOnStack() {
        addHereticsPunishment(player1);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Heretic's Punishment");
        assertThat(entry.getTargetPermanentId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumed() {
        addHereticsPunishment(player1);
        addActivationMana(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    // ===== Mill + damage to player =====

    @Test
    @DisplayName("Mills three cards and deals damage equal to highest mana value to target player")
    void millsAndDealsDamageToPlayer() {
        addHereticsPunishment(player1);
        addActivationMana(player1);
        harness.setLife(player2, 20);

        // Set up library with known cards: Shock (MV 1), GrizzlyBears (MV 2), HillGiant (MV 4)
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new Shock(), new GrizzlyBears(), new HillGiant()
        )));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Highest mana value is 4 (HillGiant), so 4 damage to player2
        harness.assertLife(player2, 16);
        // Controller's library should be empty (3 cards milled)
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        // All 3 cards should be in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Deals damage equal to the greatest mana value, not the sum")
    void dealsGreatestManaValueNotSum() {
        addHereticsPunishment(player1);
        addActivationMana(player1);
        harness.setLife(player2, 20);

        // All cards have MV 2 → damage should be 2, not 6
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()
        )));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    // ===== Mill + damage to creature =====

    @Test
    @DisplayName("Deals damage to target creature equal to highest mana value among milled cards")
    void dealsDamageToCreature() {
        addHereticsPunishment(player1);
        addActivationMana(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        // Set up library: HillGiant (MV 4) is highest → 4 damage kills 2/2
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new Forest(), new Shock(), new HillGiant()
        )));

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    // ===== Edge cases =====

    @Test
    @DisplayName("Deals 0 damage when all milled cards have mana value 0")
    void dealsZeroDamageWhenAllLands() {
        addHereticsPunishment(player1);
        addActivationMana(player1);
        harness.setLife(player2, 20);

        // All lands have MV 0
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new Forest(), new Forest(), new Forest()
        )));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Mills fewer than three cards when library has fewer than three cards")
    void millsFewerWhenLibrarySmall() {
        addHereticsPunishment(player1);
        addActivationMana(player1);
        harness.setLife(player2, 20);

        // Only 1 card in library: GrizzlyBears (MV 2)
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new GrizzlyBears())));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Deals no damage when library is empty")
    void dealsNoDamageWhenLibraryEmpty() {
        addHereticsPunishment(player1);
        addActivationMana(player1);
        harness.setLife(player2, 20);

        gd.playerDecks.put(player1.getId(), new ArrayList<>());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can be activated multiple times per turn (no tap cost)")
    void canActivateMultipleTimes() {
        addHereticsPunishment(player1);
        addActivationMana(player1);
        addActivationMana(player1);
        harness.setLife(player2, 20);

        // 6 cards in library for two activations
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new Shock(), new GrizzlyBears(), new HillGiant(),
                new Shock(), new GrizzlyBears(), new HillGiant()
        )));

        // First activation
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Second activation
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Both activations deal 4 damage (HillGiant MV 4 is highest in each set)
        harness.assertLife(player2, 12);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    // ===== Helpers =====

    private void addHereticsPunishment(Player player) {
        harness.addToBattlefield(player, new HereticsPunishment());
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }
}
