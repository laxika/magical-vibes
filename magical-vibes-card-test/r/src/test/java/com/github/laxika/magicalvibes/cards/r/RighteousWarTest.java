package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarkPrivilege;
import com.github.laxika.magicalvibes.cards.f.FallenAskari;
import com.github.laxika.magicalvibes.cards.l.LongbowArcher;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.s.SunClasp;
import com.github.laxika.magicalvibes.cards.w.Warthog;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RighteousWar.class, DarkPrivilege.class, SunClasp.class, LongbowArcher.class,
        FallenAskari.class, Warthog.class, Opalescence.class})
class RighteousWarTest extends BaseCardTest {

    private void addRighteousWar() {
        harness.addToBattlefield(player1, new RighteousWar());
    }

    @Test
    @DisplayName("White creature you control can't be targeted by a black Aura")
    void whiteCreatureProtectedFromBlack() {
        addRighteousWar();
        Permanent white = addCreatureReady(player1, new LongbowArcher());

        harness.setHand(player1, List.of(new DarkPrivilege()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, white.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Black creature you control can't be targeted by a white Aura")
    void blackCreatureProtectedFromWhite() {
        addRighteousWar();
        Permanent black = addCreatureReady(player1, new FallenAskari());

        harness.setHand(player1, List.of(new SunClasp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, black.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("White creature remains targetable by a white Aura")
    void whiteCreatureNotProtectedFromWhite() {
        addRighteousWar();
        Permanent white = addCreatureReady(player1, new LongbowArcher());

        harness.setHand(player1, List.of(new SunClasp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, white.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Green creature you control is not protected")
    void greenCreatureNotProtected() {
        addRighteousWar();
        Permanent green = addCreatureReady(player1, new Warthog());

        harness.setHand(player1, List.of(new DarkPrivilege()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, green.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Opponent's white creature is not protected")
    void opponentWhiteCreatureNotProtected() {
        addRighteousWar();
        Permanent oppWhite = addCreatureReady(player2, new LongbowArcher());

        harness.setHand(player1, List.of(new DarkPrivilege()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, oppWhite.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Opponent's black creature is not protected")
    void opponentBlackCreatureNotProtected() {
        addRighteousWar();
        Permanent oppBlack = addCreatureReady(player2, new FallenAskari());

        harness.setHand(player1, List.of(new SunClasp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, oppBlack.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Protection ends when Righteous War leaves the battlefield")
    void protectionEndsWhenRighteousWarLeaves() {
        Permanent war = harness.addToBattlefieldAndReturn(player1, new RighteousWar());
        Permanent white = addCreatureReady(player1, new LongbowArcher());
        gd.playerBattlefields.get(player1.getId()).remove(war);

        harness.setHand(player1, List.of(new DarkPrivilege()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, white.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Animated Righteous War grants protection to itself")
    void animatedRighteousWarGrantsProtectionToItself() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent war = harness.addToBattlefieldAndReturn(player1, new RighteousWar());

        assertThat(gqs.isCreature(gd, war)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, war, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, war, CardColor.WHITE)).isTrue();
    }

    @Test
    @DisplayName("Righteous War does not grant protection to itself while it is not a creature")
    void noncreatureRighteousWarHasNoProtection() {
        Permanent war = harness.addToBattlefieldAndReturn(player1, new RighteousWar());

        assertThat(gqs.hasProtectionFrom(gd, war, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, war, CardColor.WHITE)).isFalse();
    }

    @Test
    @DisplayName("Black creature remains targetable by a black Aura")
    void blackCreatureNotProtectedFromBlack() {
        addRighteousWar();
        Permanent black = addCreatureReady(player1, new FallenAskari());
        harness.setHand(player1, List.of(new DarkPrivilege()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, black.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dark Privilege");
    }

    @Test
    @DisplayName("An existing black Aura falls off a white creature when Righteous War resolves")
    void existingBlackAuraFallsOffWhiteCreature() {
        Permanent white = addCreatureReady(player1, new LongbowArcher());
        harness.setHand(player1, List.of(new DarkPrivilege()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, white.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Dark Privilege");

        harness.castFromHand(player1, new RighteousWar(), "{1}{W}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Longbow Archer");
        harness.assertNotOnBattlefield(player1, "Dark Privilege");
        harness.assertInGraveyard(player1, "Dark Privilege");
    }

    @Test
    @DisplayName("An existing white Aura falls off a black creature when Righteous War resolves")
    void existingWhiteAuraFallsOffBlackCreature() {
        Permanent black = addCreatureReady(player1, new FallenAskari());
        harness.setHand(player1, List.of(new SunClasp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, black.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sun Clasp");

        harness.castFromHand(player1, new RighteousWar(), "{1}{W}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fallen Askari");
        harness.assertNotOnBattlefield(player1, "Sun Clasp");
        harness.assertInGraveyard(player1, "Sun Clasp");
    }
}
