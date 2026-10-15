package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DireWolfProwler;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkullportMerchant.class, DireWolfProwler.class, SpareDagger.class})
class SkullportMerchantTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure token when it enters the battlefield")
    void createsTreasureWhenEntering() {
        harness.setHand(player1, List.of(new SkullportMerchant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Sacrifices another creature and draws a card")
    void sacrificesAnotherCreatureAndDraws() {
        harness.setHand(player1, List.of());
        Permanent merchant = addCreatureReady(player1, new SkullportMerchant());
        addCreatureReady(player1, new DireWolfProwler());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(merchant);
        harness.assertInGraveyard(player1, "Dire Wolf Prowler");
    }

    @Test
    @DisplayName("Can sacrifice its Treasure token and draw a card")
    void sacrificesTreasureAndDraws() {
        harness.setHand(player1, List.of(new SkullportMerchant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

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
        harness.addToBattlefield(player1, new SpareDagger());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature or a Treasure");
    }

    @Test
    @DisplayName("Cannot sacrifice itself when it is not a Treasure")
    void cannotSacrificeItselfAsACreature() {
        Permanent merchant = harness.addToBattlefieldAndReturn(player1, new SkullportMerchant());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature or a Treasure");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(merchant);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        harness.addToBattlefield(player1, new SkullportMerchant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new DireWolfProwler());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature or a Treasure");

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingCreature);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Merchant can activate; sacrifice precedes drawing")
    void paysSacrificeBeforeDrawingWithoutTapping() {
        harness.setHand(player1, List.of());
        Permanent merchant = harness.addToBattlefieldAndReturn(player1, new SkullportMerchant());
        merchant.setSummoningSick(true);
        merchant.tap();
        harness.addToBattlefield(player1, new DireWolfProwler());
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertInGraveyard(player1, "Dire Wolf Prowler");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(merchant);
        assertThat(merchant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("May sacrifice itself through the Treasure alternative when it is a Treasure")
    void canSacrificeItselfWhenAlsoATreasure() {
        harness.setHand(player1, List.of());
        Permanent merchant = harness.addToBattlefieldAndReturn(player1, new SkullportMerchant());
        merchant.getGrantedCardTypes().add(CardType.ARTIFACT);
        merchant.getGrantedSubtypes().add(CardSubtype.TREASURE);
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertInGraveyard(player1, "Skullport Merchant");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Skullport Merchant");
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
