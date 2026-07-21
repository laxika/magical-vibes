package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OverwhelmingSplendorTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Resolving Overwhelming Splendor attaches it to the target player")
    void resolvingAttachesToPlayer() {
        harness.setHand(player1, List.of(new OverwhelmingSplendor()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Overwhelming Splendor")
                        && p.isAttached()
                        && p.getAttachedTo().equals(player2.getId()));
    }

    // ===== Creatures the enchanted player controls: base 1/1, no abilities =====

    @Test
    @DisplayName("Enchanted player's creatures become base 1/1 and lose all abilities")
    void enchantedPlayerCreaturesAreNeutered() {
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        placeCurseOnPlayer(player1, player2);

        // 4/4 flyer -> base 1/1 with no flying
        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Curse controller's own creatures are unaffected")
    void controllerCreaturesUnaffected() {
        Permanent airElemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        placeCurseOnPlayer(player1, player2);

        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Neuter effect wears off when the curse leaves the battlefield")
    void neuterRemovedWhenCurseLeaves() {
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent curse = placeCurseOnPlayer(player1, player2);

        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(curse);

        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.FLYING)).isTrue();
    }

    // ===== Enchanted player can't activate non-mana, non-loyalty abilities =====

    @Test
    @DisplayName("Enchanted player can't activate a non-mana activated ability")
    void enchantedPlayerCantActivateNonManaAbility() {
        harness.addToBattlefield(player2, new JayemdaeTome());
        placeCurseOnPlayer(player1, player2);
        harness.addMana(player2, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Overwhelming Splendor");
    }

    @Test
    @DisplayName("Enchanted player can still tap a land for mana")
    void enchantedPlayerCanStillTapForMana() {
        harness.addToBattlefield(player2, new Plains());
        placeCurseOnPlayer(player1, player2);

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A player who is not enchanted can activate their abilities normally")
    void nonEnchantedPlayerUnaffected() {
        // Curse enchants player2, but its controller player1 is not restricted.
        harness.addToBattlefield(player1, new JayemdaeTome());
        placeCurseOnPlayer(player1, player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    // ===== Helpers =====

    private Permanent placeCurseOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent cursePerm = new Permanent(new OverwhelmingSplendor());
        cursePerm.setAttachedTo(enchantedPlayer.getId());
        gd.playerBattlefields.get(controller.getId()).add(cursePerm);
        return cursePerm;
    }
}
