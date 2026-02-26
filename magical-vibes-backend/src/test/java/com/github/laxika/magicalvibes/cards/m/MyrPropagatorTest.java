package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MyrPropagatorTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has activated ability with tap and mana cost")
    void hasCorrectStructure() {
        MyrPropagator card = new MyrPropagator();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{3}");
        assertThat(card.getActivatedAbilities().getFirst().getEffects())
                .hasSize(1)
                .anyMatch(e -> e instanceof CreateTokenCopyOfSourceEffect);
    }

    // ===== Token creation =====

    @Test
    @DisplayName("Activated ability creates a token copy of Myr Propagator")
    void activatedAbilityCreatesTokenCopy() {
        addPropagatorReady(player1);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Myr Propagator") && p.getCard().isToken())
                .findFirst().orElse(null);
        assertThat(token).isNotNull();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Token copy has the same activated ability as the original")
    void tokenCopyHasSameActivatedAbility() {
        addPropagatorReady(player1);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Myr Propagator") && p.getCard().isToken())
                .findFirst().orElseThrow();

        // Token should also have the activated ability (copiable characteristic per CR 707.2)
        assertThat(token.getCard().getActivatedAbilities()).hasSize(1);
        assertThat(token.getCard().getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(token.getCard().getActivatedAbilities().getFirst().getEffects())
                .hasSize(1)
                .anyMatch(e -> e instanceof CreateTokenCopyOfSourceEffect);
    }

    @Test
    @DisplayName("Multiple activations create multiple tokens")
    void multipleActivationsCreateMultipleTokens() {
        addPropagatorReady(player1);

        // First activation
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Untap source for second activation
        GameData gd = harness.getGameData();
        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Myr Propagator") && !p.getCard().isToken())
                .findFirst().orElseThrow();
        source.untap();

        // Second activation
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Myr Propagator") && p.getCard().isToken())
                .count();
        assertThat(tokenCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Source leaving battlefield before resolution creates no token")
    void sourceLeftBattlefieldNoToken() {
        addPropagatorReady(player1);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);

        // Remove the source before ability resolves
        GameData gd = harness.getGameData();
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Myr Propagator") && !p.getCard().isToken());

        harness.passBothPriorities();

        // No token should be created since source left the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Myr Propagator") && p.getCard().isToken());
    }

    // ===== Helpers =====

    private void addPropagatorReady(com.github.laxika.magicalvibes.model.Player player) {
        harness.addToBattlefield(player, new MyrPropagator());
        GameData gd = harness.getGameData();
        Permanent perm = gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Myr Propagator") && !p.getCard().isToken())
                .findFirst().orElseThrow();
        perm.setSummoningSick(false);
    }
}
