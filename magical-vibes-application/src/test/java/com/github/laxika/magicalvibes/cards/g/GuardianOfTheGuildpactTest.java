package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AzoriusFirstWing;
import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.b.BlessingOfTheNephilim;
import com.github.laxika.magicalvibes.cards.c.CacklingFlames;
import com.github.laxika.magicalvibes.cards.d.Drekavac;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.s.SealOfFire;
import com.github.laxika.magicalvibes.cards.w.WreckingBall;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuardianOfTheGuildpact.class, AzoriusFirstWing.class, AzoriusSignet.class,
        BlessingOfTheNephilim.class, CacklingFlames.class, Drekavac.class, SealOfFire.class,
        WreckingBall.class, Humility.class})
class GuardianOfTheGuildpactTest extends BaseCardTest {

    @Test
    @DisplayName("Has protection from monocolored sources but not multicolored sources")
    void hasProtectionFromMonocoloredSources() {
        Permanent guardian = addCreatureReady(player1, new GuardianOfTheGuildpact());
        Permanent monocoloredSource = addCreatureReady(player2, new Drekavac());
        Permanent multicoloredSource = addCreatureReady(player2, new AzoriusFirstWing());
        Permanent colorlessSource = harness.addToBattlefieldAndReturn(player2, new AzoriusSignet());

        assertThat(gqs.hasProtectionFromSource(gd, guardian, monocoloredSource)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, guardian, multicoloredSource)).isFalse();
        assertThat(gqs.hasProtectionFromSource(gd, guardian, colorlessSource)).isFalse();
    }

    @Test
    @DisplayName("A monocolored spell cannot target Guardian of the Guildpact")
    void monocoloredSpellCannotTarget() {
        Permanent guardian = addCreatureReady(player2, new GuardianOfTheGuildpact());
        harness.setHand(player1, List.of(new CacklingFlames()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, guardian.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An ability from a monocolored source cannot target Guardian of the Guildpact")
    void monocoloredAbilityCannotTarget() {
        Permanent guardian = addCreatureReady(player2, new GuardianOfTheGuildpact());
        harness.addToBattlefield(player1, new SealOfFire());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, guardian.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A multicolored spell can target Guardian of the Guildpact")
    void multicoloredSpellCanTarget() {
        Permanent guardian = addCreatureReady(player2, new GuardianOfTheGuildpact());
        harness.setHand(player1, List.of(new WreckingBall()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, guardian.getId());

        harness.assertInGraveyard(player2, "Guardian of the Guildpact");
    }

    @Test
    @DisplayName("A monocolored creature cannot block Guardian of the Guildpact")
    void monocoloredCreatureCannotBlock() {
        addCreatureReady(player1, new GuardianOfTheGuildpact());
        addCreatureReady(player2, new Drekavac());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Combat damage from a monocolored creature is prevented")
    void monocoloredCombatDamageIsPrevented() {
        Permanent attacker = addCreatureReady(player1, new Drekavac());
        Permanent guardian = addCreatureReady(player2, new GuardianOfTheGuildpact());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
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
        harness.setHand(player1, List.of(new BlessingOfTheNephilim()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, guardian.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A multicolored creature can block Guardian and deal combat damage to it")
    void multicoloredCreatureCanBlockAndDealDamage() {
        Permanent guardian = addCreatureReady(player1, new GuardianOfTheGuildpact());
        addCreatureReady(player2, new AzoriusFirstWing());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> {
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
            harness.passUntil(player1, TurnStep.COMBAT_DAMAGE);
        });

        assertThat(guardian.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Azorius First-Wing");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(guardian);
    }

    @Test
    @DisplayName("An illegally attached monocolored Aura is put into its owner's graveyard")
    void monocoloredAuraIsRemovedByStateBasedActions() {
        Permanent guardian = addCreatureReady(player1, new GuardianOfTheGuildpact());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BlessingOfTheNephilim());
        aura.setAttachedTo(guardian.getId());

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Blessing of the Nephilim");
        harness.assertNotOnBattlefield(player1, "Blessing of the Nephilim");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(guardian);
    }

    @Test
    @DisplayName("Losing all abilities allows monocolored spells to target Guardian")
    void losingAllAbilitiesAllowsMonocoloredTargeting() {
        Permanent guardian = addCreatureReady(player2, new GuardianOfTheGuildpact());
        harness.addToBattlefield(player1, new Humility());
        harness.setHand(player1, List.of(new CacklingFlames()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, guardian.getId());

        harness.assertInGraveyard(player2, "Guardian of the Guildpact");
    }

    @Test
    @DisplayName("Losing all abilities allows monocolored creatures to deal damage to Guardian")
    void losingAllAbilitiesAllowsMonocoloredCombatDamage() {
        addCreatureReady(player1, new Drekavac());
        addCreatureReady(player2, new GuardianOfTheGuildpact());
        harness.addToBattlefield(player1, new Humility());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Drekavac");
        harness.assertInGraveyard(player2, "Guardian of the Guildpact");
    }
}
