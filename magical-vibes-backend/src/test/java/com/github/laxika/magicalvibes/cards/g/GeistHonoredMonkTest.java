package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledCreatureCountEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GeistHonoredMonkTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Geist-Honored Monk has correct effects")
    void hasCorrectEffects() {
        GeistHonoredMonk card = new GeistHonoredMonk();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(PowerToughnessEqualToControlledCreatureCountEffect.class);

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(CreateTokenEffect.class);
        CreateTokenEffect tokenEffect =
                (CreateTokenEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(tokenEffect.amount()).isEqualTo(2);
        assertThat(tokenEffect.tokenName()).isEqualTo("Spirit");
        assertThat(tokenEffect.keywords()).containsExactly(Keyword.FLYING);
    }

    // ===== ETB: creates two Spirit tokens with flying =====

    @Test
    @DisplayName("ETB creates two 1/1 white Spirit tokens with flying")
    void etbCreatesTwoSpiritTokens() {
        castAndResolveMonk();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(3); // Monk + 2 tokens
        assertThat(countSpiritTokens(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Spirit tokens have flying")
    void spiritTokensHaveFlying() {
        castAndResolveMonk();

        Permanent token = findSpiritToken(player1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    // ===== P/T equals number of creatures you control =====

    @Test
    @DisplayName("Geist-Honored Monk is 3/3 after ETB resolves (itself + 2 Spirit tokens)")
    void ptAfterEtb() {
        castAndResolveMonk();

        Permanent monk = findPermanent(player1, "Geist-Honored Monk");
        // Monk + 2 Spirit tokens = 3 creatures
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(3);
    }

    @Test
    @DisplayName("P/T increases when more creatures enter")
    void ptIncreasesWithMoreCreatures() {
        castAndResolveMonk();
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent monk = findPermanent(player1, "Geist-Honored Monk");
        // Monk + 2 Spirits + Bears = 4
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(4);
    }

    @Test
    @DisplayName("P/T decreases when creatures leave")
    void ptDecreasesWhenCreaturesLeave() {
        castAndResolveMonk();

        Permanent monk = findPermanent(player1, "Geist-Honored Monk");
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(3);

        // Remove the Spirit tokens
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Spirit"));

        // Now only the Monk itself
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not count opponent's creatures")
    void doesNotCountOpponentCreatures() {
        castAndResolveMonk();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent monk = findPermanent(player1, "Geist-Honored Monk");
        // Only counts own creatures: Monk + 2 Spirits = 3
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(3);
    }

    // ===== Helpers =====

    private void castAndResolveMonk() {
        harness.setHand(player1, List.of(new GeistHonoredMonk()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger
    }

    private int countSpiritTokens(Player player) {
        return (int) gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Spirit"))
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.SPIRIT))
                .count();
    }

    private Permanent findSpiritToken(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Spirit"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No Spirit token found"));
    }

    private Permanent findPermanent(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst().orElseThrow();
    }
}
