package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HurloonMinotaur;
import com.github.laxika.magicalvibes.cards.m.Manabarbs;
import com.github.laxika.magicalvibes.cards.m.MonssGoblinRaiders;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({
        IvoryGuardians.class,
        GrizzlyBears.class,
        HurloonMinotaur.class,
        Incinerate.class,
        Manabarbs.class,
        MonssGoblinRaiders.class
})
class IvoryGuardiansTest extends BaseCardTest {

    @Test
    @DisplayName("Base 3/3 when no opponent controls a nontoken red permanent")
    void baseWithoutRedPermanent() {
        Permanent guardians = harness.addToBattlefieldAndReturn(player1, new IvoryGuardians());
        assertThat(gqs.getEffectivePower(gd, guardians)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, guardians)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+1 (4/4) when an opponent controls a nontoken red permanent")
    void boostWhenOpponentControlsRedPermanent() {
        Permanent guardians = harness.addToBattlefieldAndReturn(player1, new IvoryGuardians());
        harness.addToBattlefield(player2, new MonssGoblinRaiders());

        assertThat(gqs.getEffectivePower(gd, guardians)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, guardians)).isEqualTo(4);
    }

    @Test
    @DisplayName("No boost when the opponent's red permanent is a token")
    void noBoostWhenRedPermanentIsToken() {
        Permanent guardians = harness.addToBattlefieldAndReturn(player1, new IvoryGuardians());
        Card token = new MonssGoblinRaiders();
        token.setToken(true);
        harness.addToBattlefield(player2, token);

        assertThat(gqs.getEffectivePower(gd, guardians)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, guardians)).isEqualTo(3);
    }

    @Test
    @DisplayName("No boost when the opponent's nontoken permanent is not red")
    void noBoostWhenPermanentNotRed() {
        Permanent guardians = harness.addToBattlefieldAndReturn(player1, new IvoryGuardians());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, guardians)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, guardians)).isEqualTo(3);
    }

    @Test
    @DisplayName("The controller's own red permanent does not grant the boost")
    void noBoostFromOwnRedPermanent() {
        Permanent guardians = harness.addToBattlefieldAndReturn(player1, new IvoryGuardians());
        harness.addToBattlefield(player1, new MonssGoblinRaiders());

        assertThat(gqs.getEffectivePower(gd, guardians)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, guardians)).isEqualTo(3);
    }

    @Test
    @DisplayName("A nontoken red enchantment grants the boost")
    void boostWhenOpponentControlsRedEnchantment() {
        Permanent guardians = harness.addToBattlefieldAndReturn(player1, new IvoryGuardians());
        harness.addToBattlefield(player2, new Manabarbs());

        assertThat(gqs.getEffectivePower(gd, guardians)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, guardians)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost disappears when the qualifying red permanent leaves")
    void boostDisappearsWhenRedPermanentLeaves() {
        Permanent guardians = harness.addToBattlefieldAndReturn(player1, new IvoryGuardians());
        Permanent redPermanent = harness.addToBattlefieldAndReturn(player2, new MonssGoblinRaiders());

        assertThat(gqs.getEffectivePower(gd, guardians)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, guardians)).isEqualTo(4);

        gd.playerBattlefields.get(player2.getId()).remove(redPermanent);

        assertThat(gqs.getEffectivePower(gd, guardians)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, guardians)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost applies to all creatures named Ivory Guardians")
    void boostsAllNamedIvoryGuardians() {
        Permanent ownGuardians = addCreatureReady(player1, new IvoryGuardians());
        Permanent opposingGuardians = addCreatureReady(player2, new IvoryGuardians());
        harness.addToBattlefield(player2, new MonssGoblinRaiders());

        assertThat(gqs.getEffectivePower(gd, ownGuardians)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownGuardians)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingGuardians)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposingGuardians)).isEqualTo(4);
    }

    @Test
    @DisplayName("Each active Ivory Guardians ability contributes its own boost")
    void boostsStackWhenBothPlayersControlRedPermanents() {
        Permanent ownGuardians = addCreatureReady(player1, new IvoryGuardians());
        Permanent opposingGuardians = addCreatureReady(player2, new IvoryGuardians());
        harness.addToBattlefield(player1, new MonssGoblinRaiders());
        harness.addToBattlefield(player2, new MonssGoblinRaiders());

        assertThat(gqs.getEffectivePower(gd, ownGuardians)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownGuardians)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, opposingGuardians)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opposingGuardians)).isEqualTo(5);
    }

    @Test
    @DisplayName("Red creature cannot block Ivory Guardians (protection from red)")
    void redCreatureCannotBlock() {
        addCreatureReady(player1, new IvoryGuardians());
        addCreatureReady(player2, new MonssGoblinRaiders());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from red prevents combat damage from red creatures")
    void redCombatDamageIsPrevented() {
        Permanent guardians = addCreatureReady(player1, new IvoryGuardians());
        addCreatureReady(player2, new HurloonMinotaur());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(guardians);
        assertThat(guardians.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A red spell cannot target Ivory Guardians")
    void redSpellCannotTargetGuardians() {
        Permanent guardians = addCreatureReady(player1, new IvoryGuardians());
        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, guardians.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }
}
