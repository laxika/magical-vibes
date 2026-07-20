package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OraclesVaultTest extends BaseCardTest {

    private SpellCastingService spellCastingService() {
        return GameTestEngineContext.get().getBean(SpellCastingService.class);
    }

    private Permanent addReadyVault(Player player) {
        Permanent vault = new Permanent(new OraclesVault());
        vault.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(vault);
        return vault;
    }

    private int vaultIndex(Player player, Permanent vault) {
        return gd.playerBattlefields.get(player.getId()).indexOf(vault);
    }

    private Card putSpellOnTop(Player player, String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{4}{R}{R}");
        card.setColor(CardColor.RED);
        gd.playerDecks.get(player.getId()).addFirst(card);
        return card;
    }

    @Test
    @DisplayName("First ability exiles the top card with normal play permission and adds a brick counter")
    void firstAbilityExilesWithPlayPermissionAndBrickCounter() {
        Permanent vault = addReadyVault(player1);
        Card top = putSpellOnTop(player1, "Exiled Spell");
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
        putSpellOnTop(player1, "Exiled Spell");

        assertThatThrownBy(() -> harness.activateAbility(player1, vaultIndex(player1, vault), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("brick counters");
    }

    @Test
    @DisplayName("Second ability exiles the top card with a free-play permission")
    void secondAbilityGrantsFreePlayPermission() {
        Permanent vault = addReadyVault(player1);
        vault.setCounterCount(CounterType.BRICK, 3);
        Card top = putSpellOnTop(player1, "Exiled Spell");

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
        Card top = putSpellOnTop(player1, "Free Spell");

        harness.activateAbility(player1, vaultIndex(player1, vault), 1, null, null);
        harness.passBothPriorities();

        // Player has no mana at all — the play must still succeed for free.
        gd.playerManaPools.get(player1.getId()).clear();
        spellCastingService().playCardFromExile(gd, player1, top.getId(), 0, null);

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
        Card top = putSpellOnTop(player1, "Exiled Spell");

        harness.activateAbility(player1, vaultIndex(player1, vault), 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(top.getId());

        GameTestEngineContext.get().getBean(com.github.laxika.magicalvibes.service.turn.TurnCleanupService.class)
                .applyCleanupResets(gd);

        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(top.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
    }
}
