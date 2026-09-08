package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkullportMerchant.class, GrizzlyBears.class, Spellbook.class})
class SkullportMerchantTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure token when it enters the battlefield")
    void createsTreasureWhenEntering() {
        harness.setHand(player1, List.of(new SkullportMerchant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Sacrifices another creature and draws a card")
    void sacrificesAnotherCreatureAndDraws() {
        harness.setHand(player1, List.of());
        Permanent merchant = addCreatureReady(player1, new SkullportMerchant());
        addCreatureReady(player1, new GrizzlyBears());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(merchant);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can sacrifice its Treasure token and draw a card")
    void sacrificesTreasureAndDraws() {
        harness.setHand(player1, List.of(new SkullportMerchant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent merchant = findPermanent(player1, "Skullport Merchant");
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        addAbilityMana();
        int merchantIndex = gd.playerBattlefields.get(player1.getId()).indexOf(merchant);

        harness.activateAbility(player1, merchantIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(merchant);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Cannot sacrifice a non-Treasure artifact")
    void cannotSacrificeNonTreasureArtifact() {
        addCreatureReady(player1, new SkullportMerchant());
        harness.addToBattlefield(player1, new Spellbook());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature or a Treasure");
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
