package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.r.RoyalAssassin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FiendslayerPaladin.class, GrizzlyBears.class, Pacifism.class, RoyalAssassin.class,
        Shock.class, Terror.class, Pyroclasm.class})
class FiendslayerPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's black spell cannot target Fiendslayer Paladin")
    void opponentBlackSpellCannotTarget() {
        harness.addToBattlefield(player2, new FiendslayerPaladin());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, harness.getPermanentId(player2, "Fiendslayer Paladin")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target of black spells");
    }

    @Test
    @DisplayName("An opponent's red spell cannot target Fiendslayer Paladin")
    void opponentRedSpellCannotTarget() {
        harness.addToBattlefield(player2, new FiendslayerPaladin());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, harness.getPermanentId(player2, "Fiendslayer Paladin")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target of red spells");
    }

    @Test
    @DisplayName("The controller's own red spell can target Fiendslayer Paladin")
    void ownRedSpellCanTarget() {
        harness.addToBattlefield(player1, new FiendslayerPaladin());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Fiendslayer Paladin"));

        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(se -> se.getCard().getName().equals("Shock"));
    }

    @Test
    @DisplayName("An opponent's white spell can still target Fiendslayer Paladin")
    void opponentWhiteSpellCanTarget() {
        harness.addToBattlefield(player2, new FiendslayerPaladin());

        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, harness.getPermanentId(player2, "Fiendslayer Paladin"));

        assertThat(gd.stack).anyMatch(se -> se.getCard().getName().equals("Pacifism"));
    }

    @Test
    @DisplayName("An opponent's black activated ability can still target Fiendslayer Paladin")
    void opponentBlackAbilityCanTarget() {
        Permanent paladin = addCreatureReady(player1, new FiendslayerPaladin());
        paladin.tap();

        addCreatureReady(player2, new RoyalAssassin());

        harness.activateAbility(player2, 0, null, paladin.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fiendslayer Paladin");
        harness.assertInGraveyard(player1, "Fiendslayer Paladin");
    }

    @Test
    @DisplayName("The controller's own black spell can destroy Fiendslayer Paladin")
    void ownBlackSpellCanTarget() {
        Permanent paladin = harness.addToBattlefieldAndReturn(player1, new FiendslayerPaladin());
        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, paladin.getId());

        harness.assertNotOnBattlefield(player1, "Fiendslayer Paladin");
        harness.assertInGraveyard(player1, "Fiendslayer Paladin");
    }

    @Test
    @DisplayName("First strike kills a blocker before it deals damage and lifelink gains life")
    void firstStrikeAndLifelinkAgainstBlocker() {
        Permanent paladin = addCreatureReady(player1, new FiendslayerPaladin());
        paladin.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Fiendslayer Paladin");
        assertThat(paladin.getMarkedDamage()).isZero();
        harness.assertLife(player1, 22);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Fiendslayer Paladin");
    }

    @Test
    @DisplayName("An unblocked Paladin deals damage and gains life only in the first strike step")
    void unblockedCombatDamageGainsLifeOnce() {
        Permanent paladin = addCreatureReady(player1, new FiendslayerPaladin());
        paladin.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An opponent's red spell that does not target still deals lethal damage")
    void nontargetedRedDamageIsNotPrevented() {
        harness.addToBattlefield(player2, new FiendslayerPaladin());
        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player2, "Fiendslayer Paladin");
        harness.assertInGraveyard(player2, "Fiendslayer Paladin");
    }
}
