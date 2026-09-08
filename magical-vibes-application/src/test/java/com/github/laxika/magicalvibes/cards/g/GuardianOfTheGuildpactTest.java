package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.m.Mortify;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuardianOfTheGuildpact.class, HillGiant.class, HolyStrength.class, Mortify.class, Shock.class,
        WoollyThoctar.class})
class GuardianOfTheGuildpactTest extends BaseCardTest {

    @Test
    @DisplayName("Has protection from monocolored sources but not multicolored sources")
    void hasProtectionFromMonocoloredSources() {
        Permanent guardian = addCreatureReady(player1, new GuardianOfTheGuildpact());
        Permanent monocoloredSource = addCreatureReady(player2, new HillGiant());
        Permanent multicoloredSource = addCreatureReady(player2, new WoollyThoctar());

        assertThat(gqs.hasProtectionFromSource(gd, guardian, monocoloredSource)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, guardian, multicoloredSource)).isFalse();
    }

    @Test
    @DisplayName("A monocolored spell cannot target Guardian of the Guildpact")
    void monocoloredSpellCannotTarget() {
        Permanent guardian = addCreatureReady(player2, new GuardianOfTheGuildpact());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, guardian.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A multicolored spell can target Guardian of the Guildpact")
    void multicoloredSpellCanTarget() {
        Permanent guardian = addCreatureReady(player2, new GuardianOfTheGuildpact());
        harness.setHand(player1, List.of(new Mortify()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, guardian.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Guardian of the Guildpact");
    }

    @Test
    @DisplayName("A monocolored creature cannot block Guardian of the Guildpact")
    void monocoloredCreatureCannotBlock() {
        Permanent guardian = addCreatureReady(player1, new GuardianOfTheGuildpact());
        guardian.setSummoningSick(false);
        addCreatureReady(player2, new HillGiant());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Combat damage from a monocolored creature is prevented")
    void monocoloredCombatDamageIsPrevented() {
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        Permanent guardian = addCreatureReady(player2, new GuardianOfTheGuildpact());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(guardian.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(guardian);
    }

    @Test
    @DisplayName("A monocolored Aura cannot enchant Guardian of the Guildpact")
    void monocoloredAuraCannotEnchant() {
        Permanent guardian = addCreatureReady(player2, new GuardianOfTheGuildpact());
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, guardian.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
