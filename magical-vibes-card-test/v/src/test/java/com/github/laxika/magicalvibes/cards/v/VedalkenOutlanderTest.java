package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.b.BeaconBehemoth;
import com.github.laxika.magicalvibes.cards.c.CanyonMinotaur;
import com.github.laxika.magicalvibes.cards.c.CylianSunsinger;
import com.github.laxika.magicalvibes.cards.f.FieryFall;
import com.github.laxika.magicalvibes.cards.g.GoblinOutlander;
import com.github.laxika.magicalvibes.cards.m.ManiacalRage;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VedalkenOutlander.class, CanyonMinotaur.class, CylianSunsinger.class,
        BeaconBehemoth.class, FieryFall.class, Unsummon.class, GoblinOutlander.class,
        VolcanicFallout.class, ManiacalRage.class})
class VedalkenOutlanderTest extends BaseCardTest {

    @Test
    @DisplayName("Red creature cannot block Vedalken Outlander")
    void redCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new VedalkenOutlander());
        attacker.setAttacking(true);

        addCreatureReady(player2, new CanyonMinotaur());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Green creature can block Vedalken Outlander")
    void greenCreatureCanBlock() {
        Permanent attacker = addCreatureReady(player1, new VedalkenOutlander());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new CylianSunsinger());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Vedalken Outlander takes no combat damage from red creature")
    void takesNoDamageFromRed() {
        Permanent attacker = addCreatureReady(player1, new CanyonMinotaur());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new VedalkenOutlander());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Red creature's 3 damage to the Outlander is prevented (protection from red)
        harness.assertOnBattlefield(player2, "Vedalken Outlander");
    }

    @Test
    @DisplayName("Vedalken Outlander takes normal combat damage from green creature")
    void takesNormalDamageFromGreen() {
        Permanent attacker = addCreatureReady(player1, new BeaconBehemoth());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new VedalkenOutlander());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // 5 damage from green (no protection) kills the Outlander
        harness.assertNotOnBattlefield(player2, "Vedalken Outlander");
        harness.assertInGraveyard(player2, "Vedalken Outlander");
    }

    @Test
    @DisplayName("Cannot be targeted by red instant")
    void cannotBeTargetedByRedInstant() {
        Permanent outlander = addCreatureReady(player2, new VedalkenOutlander());

        // Add valid target so spell is playable
        addCreatureReady(player2, new CylianSunsinger());

        harness.setHand(player1, List.of(new FieryFall()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, outlander.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Can be targeted by blue instant")
    void canBeTargetedByBlueInstant() {
        Permanent outlander = addCreatureReady(player1, new VedalkenOutlander());

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        gs.playCard(gd, player1, 0, 0, outlander.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Unsummon");
    }

    @Test
    @DisplayName("Multicolored red creature cannot block Vedalken Outlander")
    void multicoloredRedCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new VedalkenOutlander());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GoblinOutlander());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection prevents untargeted red spell damage")
    void preventsUntargetedRedDamage() {
        addCreatureReady(player1, new VedalkenOutlander());
        addCreatureReady(player2, new CylianSunsinger());
        harness.setHand(player1, List.of(new VolcanicFallout()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.assertOnBattlefield(player1, "Vedalken Outlander");
        harness.assertInGraveyard(player2, "Cylian Sunsinger");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Cannot be enchanted by a red Aura even from its controller")
    void cannotBeTargetedByOwnRedAura() {
        Permanent outlander = addCreatureReady(player1, new VedalkenOutlander());
        addCreatureReady(player1, new CylianSunsinger());
        harness.setHand(player1, List.of(new ManiacalRage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, outlander.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection does not prevent a blue spell from returning Outlander to hand")
    void blueSpellResolvesNormally() {
        Permanent outlander = addCreatureReady(player2, new VedalkenOutlander());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, outlander.getId());

        harness.assertNotOnBattlefield(player2, "Vedalken Outlander");
        harness.assertInHand(player2, "Vedalken Outlander");
    }

    @Test
    @DisplayName("An attached red Aura is put into its owner's graveyard")
    void attachedRedAuraIsRemoved() {
        Permanent outlander = addCreatureReady(player1, new VedalkenOutlander());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new ManiacalRage());
        aura.setAttachedTo(outlander.getId());
        harness.setHand(player1, List.of(new VolcanicFallout()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.assertNotOnBattlefield(player2, "Maniacal Rage");
        harness.assertInGraveyard(player2, "Maniacal Rage");
        harness.assertOnBattlefield(player1, "Vedalken Outlander");
    }
}
