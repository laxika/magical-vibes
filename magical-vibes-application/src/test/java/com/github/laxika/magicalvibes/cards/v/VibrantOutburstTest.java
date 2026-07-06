package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import org.junit.jupiter.api.DisplayName;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import org.junit.jupiter.api.Test;

import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.List;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.UUID;

import com.github.laxika.magicalvibes.model.amount.Fixed;
import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VibrantOutburstTest extends BaseCardTest {

    // ===== Damage a creature + tap another creature =====

    @Test
    @DisplayName("Deals 3 damage to a creature and taps a different creature (damage target not tapped)")
    void damagesCreatureAndTapsAnother() {
        harness.addToBattlefield(player2, new AirElemental()); // damage target (survives)
        harness.addToBattlefield(player2, new GrizzlyBears()); // tap target
        harness.setHand(player1, List.of(new VibrantOutburst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID elementalId = harness.getPermanentId(player2, "Air Elemental");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castInstant(player1, 0, List.of(elementalId, bearsId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Air Elemental (4/4) takes 3 damage and survives, and is NOT the tap target
        Permanent elemental = gqs.findPermanentById(gd, elementalId);
        assertThat(elemental.getMarkedDamage()).isEqualTo(3);
        assertThat(elemental.isTapped()).isFalse();
        // Grizzly Bears is tapped
        Permanent bears = gqs.findPermanentById(gd, bearsId);
        assertThat(bears.isTapped()).isTrue();
    }

    // ===== Damage a player + tap a creature =====

    @Test
    @DisplayName("Deals 3 damage to a player and taps a creature")
    void damagesPlayerAndTapsCreature() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new VibrantOutburst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID elementalId = harness.getPermanentId(player2, "Air Elemental");

        harness.castInstant(player1, 0, List.of(player2.getId(), elementalId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        Permanent elemental = gqs.findPermanentById(gd, elementalId);
        assertThat(elemental.isTapped()).isTrue();
    }

    // ===== Optional tap omitted =====

    @Test
    @DisplayName("Resolves with only the damage target (tap is up to one)")
    void resolvesWithOnlyDamageTarget() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new VibrantOutburst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID elementalId = harness.getPermanentId(player2, "Air Elemental");

        harness.castInstant(player1, 0, List.of(elementalId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent elemental = gqs.findPermanentById(gd, elementalId);
        assertThat(elemental.getMarkedDamage()).isEqualTo(3);
        assertThat(elemental.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Vibrant Outburst");
    }

    // ===== Illegal second target =====

    @Test
    @DisplayName("Cannot choose a player as the tap target")
    void cannotTapPlayer() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new VibrantOutburst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID elementalId = harness.getPermanentId(player2, "Air Elemental");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(elementalId, player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
