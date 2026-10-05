package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.h.HieroglyphicIllumination;
import com.github.laxika.magicalvibes.cards.s.ShelteredThicket;
import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OraclesVault.class, HieroglyphicIllumination.class, ShelteredThicket.class, Colossapede.class})
class OraclesVaultTest extends BaseCardTest {

    private Permanent addReadyVault(Player player) {
        Permanent vault = harness.addToBattlefieldAndReturn(player, new OraclesVault());
        vault.setSummoningSick(false);
        return vault;
    }

    private int vaultIndex(Player player, Permanent vault) {
        return gd.playerBattlefields.get(player.getId()).indexOf(vault);
    }

    private Card putSpellOnTop(Player player) {
        Card card = new HieroglyphicIllumination();
        gd.playerDecks.get(player.getId()).addFirst(card);
        return card;
    }

    @Test
    @DisplayName("First ability exiles the top card with normal play permission and adds a brick counter")
    void firstAbilityExilesWithPlayPermissionAndBrickCounter() {
        Permanent vault = addReadyVault(player1);
        Card top = putSpellOnTop(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, vaultIndex(player1, vault), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());
        // First ability is a normal-cost play, not a free one.
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(top.getId());
        assertThat(vault.getCounterCount(CounterType.BRICK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability can't be activated with fewer than three brick counters")
    void secondAbilityRequiresThreeBrickCounters() {
        Permanent vault = addReadyVault(player1);
        vault.setCounterCount(CounterType.BRICK, 2);
        putSpellOnTop(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, vaultIndex(player1, vault), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("brick counters");
    }

    @Test
    @DisplayName("Second ability exiles the top card with a free-play permission")
    void secondAbilityGrantsFreePlayPermission() {
        Permanent vault = addReadyVault(player1);
        vault.setCounterCount(CounterType.BRICK, 3);
        Card top = putSpellOnTop(player1);

        harness.activateAbility(player1, vaultIndex(player1, vault), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(top.getId());
    }

    @Test
    @DisplayName("A card exiled by the second ability can be cast from exile without paying its mana cost")
    void freePlayCastsExiledCardWithoutPayingMana() {
        Permanent vault = addReadyVault(player1);
        vault.setCounterCount(CounterType.BRICK, 3);
        Card top = putSpellOnTop(player1);

        harness.activateAbility(player1, vaultIndex(player1, vault), 1, null, null);
        harness.passBothPriorities();

        // Player has no mana at all — the play must still succeed for free.
        gd.playerManaPools.get(player1.getId()).clear();
        harness.castFromExile(player1, top.getId());

        assertThat(gd.stack).anyMatch(e -> e.getCard().getId().equals(top.getId())
                && e.getEntryType() == StackEntryType.INSTANT_SPELL);
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(top.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Free-play permission is cleared during end-of-turn cleanup")
    void freePlayPermissionExpiresAtEndOfTurn() {
        Permanent vault = addReadyVault(player1);
        vault.setCounterCount(CounterType.BRICK, 3);
        Card top = putSpellOnTop(player1);

        harness.activateAbility(player1, vaultIndex(player1, vault), 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(top.getId());

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(com.github.laxika.magicalvibes.service.turn.TurnCleanupService.class)
                        .applyCleanupResets(gd));

        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(top.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
    }

    @Test
    void firstAbilityStillAddsBrickCounterWithEmptyLibrary() {
        Permanent vault = addReadyVault(player1);
        harness.setLibrary(player1, java.util.List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, vaultIndex(player1, vault), 0, null, null);
        assertThat(vault.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(vault.getCounterCount(CounterType.BRICK)).isEqualTo(1);
    }

    @Test
    void firstAbilityRequiresNormalManaCostToCastExiledSpell() {
        Permanent vault = addReadyVault(player1);
        Card top = new HieroglyphicIllumination();
        gd.playerDecks.get(player1.getId()).addFirst(top);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, vaultIndex(player1, vault), 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, top.getId());

        assertThat(gd.stack).anyMatch(e -> e.getCard().getId().equals(top.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void secondAbilityCanPlayLandButDoesNotGrantExtraLandPlay() {
        Permanent vault = addReadyVault(player1);
        vault.setCounterCount(CounterType.BRICK, 3);
        Card first = new ShelteredThicket();
        Card second = new ShelteredThicket();
        harness.setLibrary(player1, java.util.List.of(first, second));

        harness.activateAbility(player1, vaultIndex(player1, vault), 1, null, null);
        harness.passBothPriorities();
        harness.castFromExile(player1, first.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(first.getId()) && p.isTapped());
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);

        vault.untap();
        harness.activateAbility(player1, vaultIndex(player1, vault), 1, null, null);
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(second);
        assertThat(vault.getCounterCount(CounterType.BRICK)).isEqualTo(3);
    }

    @Test
    void firstAbilityResolvesAfterVaultLeavesBattlefield() {
        Permanent vault = addReadyVault(player1);
        Card top = new HieroglyphicIllumination();
        gd.playerDecks.get(player1.getId()).addFirst(top);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, vaultIndex(player1, vault), 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(vault);
        gd.playerGraveyards.get(player1.getId()).add(vault.getCard());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, top.getId());
        assertThat(gd.stack).anyMatch(e -> e.getCard().getId().equals(top.getId()));
        assertThat(vault.getCounterCount(CounterType.BRICK)).isZero();
    }

    @Test
    void secondAbilityDoesNotRecheckBrickCountersOnResolution() {
        Permanent vault = addReadyVault(player1);
        vault.setCounterCount(CounterType.BRICK, 3);
        Card top = new HieroglyphicIllumination();
        gd.playerDecks.get(player1.getId()).addFirst(top);
        harness.activateAbility(player1, vaultIndex(player1, vault), 1, null, null);
        vault.setCounterCount(CounterType.BRICK, 0);
        harness.passBothPriorities();

        harness.castFromExile(player1, top.getId());
        assertThat(gd.stack).anyMatch(e -> e.getCard().getId().equals(top.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void freePlayPermissionDoesNotOverrideCreatureTiming() {
        Permanent vault = addReadyVault(player1);
        vault.setCounterCount(CounterType.BRICK, 3);
        Card top = new Colossapede();
        gd.playerDecks.get(player1.getId()).addFirst(top);
        harness.forceStep(TurnStep.UPKEEP);
        harness.activateAbility(player1, vaultIndex(player1, vault), 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, top.getId());
        assertThat(gd.stack).anyMatch(e -> e.getCard().getId().equals(top.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void normalPlayPermissionExpiresAndLeavesCardInExile() {
        Permanent vault = addReadyVault(player1);
        Card top = new HieroglyphicIllumination();
        gd.playerDecks.get(player1.getId()).addFirst(top);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, vaultIndex(player1, vault), 0, null, null);
        harness.passBothPriorities();

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(com.github.laxika.magicalvibes.service.turn.TurnCleanupService.class)
                        .applyCleanupResets(gd));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }
}
