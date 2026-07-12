package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TwiddleTest extends BaseCardTest {

    // ===== Tapping untapped permanents =====

    @Test
    @DisplayName("Taps an untapped creature")
    void tapsUntappedCreature() {
        Permanent target = addReadyCreature(player2);
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThat(target.isTapped()).isFalse();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps an untapped land")
    void tapsUntappedLand() {
        Permanent target = addReadyLand(player2);
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps an untapped artifact")
    void tapsUntappedArtifact() {
        Permanent target = addReadyArtifact(player2);
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    // ===== Untapping tapped permanents =====

    @Test
    @DisplayName("Untaps a tapped land")
    void untapsTappedLand() {
        Permanent target = addReadyLand(player2);
        target.tap();
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThat(target.isTapped()).isTrue();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    // ===== Invalid targets =====

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        addReadyCreature(player1); // valid target so spell is playable
        Permanent enchantment = addReadyEnchantment(player2);
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land");
    }

    // ===== After resolution =====

    @Test
    @DisplayName("Twiddle goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent target = addReadyCreature(player2);
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Twiddle"));
    }

    // ===== Can target own permanents =====

    @Test
    @DisplayName("Can untap own tapped creature")
    void canUntapOwnCreature() {
        Permanent ownCreature = addReadyCreature(player1);
        ownCreature.tap();
        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isFalse();
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyLand(Player player) {
        Forest card = new Forest();
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyArtifact(Player player) {
        AngelsFeather card = new AngelsFeather();
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyEnchantment(Player player) {
        Pacifism card = new Pacifism();
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
