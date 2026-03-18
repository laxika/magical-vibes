package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NephaliaDrownyardTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Nephalia Drownyard has two activated abilities")
    void hasTwoActivatedAbilities() {
        NephaliaDrownyard card = new NephaliaDrownyard();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        // First ability: {T}: Add {C}.
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(AwardManaEffect.class);

        // Second ability: {1}{U}{B}, {T}: Target player mills three cards.
        assertThat(card.getActivatedAbilities().get(1).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(1).getManaCost()).isEqualTo("{1}{U}{B}");
        assertThat(card.getActivatedAbilities().get(1).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(1).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(1).getEffects().getFirst())
                .isInstanceOf(MillTargetPlayerEffect.class);
        MillTargetPlayerEffect effect = (MillTargetPlayerEffect) card.getActivatedAbilities().get(1).getEffects().getFirst();
        assertThat(effect.count()).isEqualTo(3);
    }

    // ===== Mana ability =====

    @Test
    @DisplayName("Tapping for mana adds colorless mana")
    void tappingForManaAddsColorless() {
        Permanent drownyard = addReadyDrownyard(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(drownyard.isTapped()).isTrue();
    }

    // ===== Mill ability =====

    @Test
    @DisplayName("Activating mill ability puts it on the stack")
    void activatingMillAbilityPutsOnStack() {
        addReadyDrownyard(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Nephalia Drownyard");
        assertThat(entry.getTargetPermanentId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Activating mill ability taps Nephalia Drownyard")
    void activatingMillAbilityTapsDrownyard() {
        Permanent drownyard = addReadyDrownyard(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());

        assertThat(drownyard.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana is consumed when activating mill ability")
    void manaIsConsumedOnMillAbility() {
        addReadyDrownyard(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Mills three cards from target player's library")
    void millsThreeCards() {
        addReadyDrownyard(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        List<Card> deck = harness.getGameData().playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Can target yourself to mill")
    void canTargetSelf() {
        addReadyDrownyard(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Mills only remaining cards when library has fewer than three")
    void millsOnlyRemainingWhenLibrarySmall() {
        addReadyDrownyard(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        List<Card> deck = harness.getGameData().playerDecks.get(player2.getId());
        while (deck.size() > 2) {
            deck.removeFirst();
        }

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Mills nothing when library is empty")
    void millsNothingWhenLibraryEmpty() {
        addReadyDrownyard(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.getGameData().playerDecks.get(player2.getId()).clear();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate mill ability without enough mana")
    void cannotActivateMillWithoutMana() {
        addReadyDrownyard(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate mill ability when already tapped")
    void cannotActivateMillWhenTapped() {
        Permanent drownyard = addReadyDrownyard(player1);
        drownyard.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Helpers =====

    private Permanent addReadyDrownyard(Player player) {
        NephaliaDrownyard card = new NephaliaDrownyard();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
