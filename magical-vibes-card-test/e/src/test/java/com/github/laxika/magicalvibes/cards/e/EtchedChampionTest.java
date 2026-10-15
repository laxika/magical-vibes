package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GalvanicBlast;
import com.github.laxika.magicalvibes.cards.a.Arrest;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EtchedChampion.class, LeoninScimitar.class, GrizzlyBears.class, GalvanicBlast.class, Arrest.class})
class EtchedChampionTest extends BaseCardTest {


    @Test
    @DisplayName("With metalcraft, Etched Champion has protection from all colors")
    void hasProtectionWithMetalcraft() {
        harness.addToBattlefield(player1, new EtchedChampion());
        // Etched Champion itself is an artifact; add two more for metalcraft
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent champion = findPermanent(player1, "Etched Champion");

        assertThat(gqs.hasProtectionFrom(gd, champion, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, champion, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, champion, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, champion, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, champion, CardColor.GREEN)).isTrue();
    }


    @Test
    @DisplayName("Without metalcraft, Etched Champion has no protection")
    void noProtectionWithoutMetalcraft() {
        harness.addToBattlefield(player1, new EtchedChampion());
        // Only one artifact (itself), not enough for metalcraft

        Permanent champion = findPermanent(player1, "Etched Champion");

        assertThat(gqs.hasProtectionFrom(gd, champion, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, champion, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("With only two artifacts, Etched Champion has no protection")
    void noProtectionWithTwoArtifacts() {
        harness.addToBattlefield(player1, new EtchedChampion());
        harness.addToBattlefield(player1, new LeoninScimitar());
        // Two artifacts, still not enough

        Permanent champion = findPermanent(player1, "Etched Champion");

        assertThat(gqs.hasProtectionFrom(gd, champion, CardColor.BLACK)).isFalse();
    }


    @Test
    @DisplayName("With metalcraft, colored creature cannot block Etched Champion")
    void coloredCreatureCannotBlockWithMetalcraft() {
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent champion = addCreatureReady(player1, new EtchedChampion());
        champion.setAttacking(true);

        int championIdx = gd.playerBattlefields.get(player1.getId()).indexOf(champion);

        // Green blocker
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, championIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }


    @Test
    @DisplayName("Without metalcraft, colored creature can block Etched Champion")
    void coloredCreatureCanBlockWithoutMetalcraft() {
        // Only one artifact (itself), no metalcraft
        Permanent champion = addCreatureReady(player1, new EtchedChampion());
        champion.setAttacking(true);

        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }


    @Test
    @DisplayName("Protection is lost when artifact count drops below three")
    void protectionLostWhenArtifactRemoved() {
        harness.addToBattlefield(player1, new EtchedChampion());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent champion = findPermanent(player1, "Etched Champion");

        // With 3 artifacts, has protection
        assertThat(gqs.hasProtectionFrom(gd, champion, CardColor.RED)).isTrue();

        // Remove one artifact
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Leonin Scimitar"));

        // Now only 1 artifact, no protection
        assertThat(gqs.hasProtectionFrom(gd, champion, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Opposing artifacts do not enable metalcraft")
    void opposingArtifactsDoNotCount() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new EtchedChampion());
        harness.addToBattlefield(player2, new EtchedChampion());
        harness.addToBattlefield(player2, new EtchedChampion());

        for (CardColor color : CardColor.values()) {
            assertThat(gqs.hasProtectionFrom(gd, champion, color)).isFalse();
        }
    }

    @Test
    @DisplayName("Protection changes immediately at the three-artifact threshold")
    void protectionUpdatesAtThreshold() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new EtchedChampion());
        harness.addToBattlefield(player1, new EtchedChampion());
        assertThat(gqs.hasProtectionFrom(gd, champion, CardColor.RED)).isFalse();

        Permanent third = harness.addToBattlefieldAndReturn(player1, new EtchedChampion());
        assertThat(gqs.hasProtectionFrom(gd, champion, CardColor.RED)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(third);
        assertThat(gqs.hasProtectionFrom(gd, champion, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Colorless creatures can block despite metalcraft")
    void colorlessCreatureCanBlock() {
        Permanent champion = addCreatureReady(player1, new EtchedChampion());
        champion.setAttacking(true);
        harness.addToBattlefield(player1, new EtchedChampion());
        harness.addToBattlefield(player1, new EtchedChampion());
        Permanent blocker = addCreatureReady(player2, new EtchedChampion());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargets()).contains(0);
    }

    @Test
    @DisplayName("Protection prevents damage from a colored attacker")
    void preventsColoredCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent champion = addCreatureReady(player2, new EtchedChampion());
        harness.addToBattlefield(player2, new EtchedChampion());
        harness.addToBattlefield(player2, new EtchedChampion());
        champion.setBlocking(true);
        champion.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        assertThat(champion.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(champion);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Protection does not prevent damage from a colorless attacker")
    void allowsColorlessCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new EtchedChampion());
        attacker.setAttacking(true);
        Permanent champion = addCreatureReady(player2, new EtchedChampion());
        harness.addToBattlefield(player2, new EtchedChampion());
        harness.addToBattlefield(player2, new EtchedChampion());
        champion.setBlocking(true);
        champion.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(champion);
        harness.assertInGraveyard(player2, "Etched Champion");
    }

    @Test
    @DisplayName("Colored spells cannot target a champion with metalcraft")
    void rejectsColoredSpellTarget() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new EtchedChampion());
        harness.addToBattlefield(player1, new EtchedChampion());
        harness.addToBattlefield(player1, new EtchedChampion());
        harness.setHand(player2, List.of(new GalvanicBlast()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, champion.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A colored spell loses its target when metalcraft is gained before resolution")
    void gainingMetalcraftInvalidatesSpellTarget() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new EtchedChampion());
        harness.addToBattlefield(player1, new EtchedChampion());
        harness.setHand(player2, List.of(new GalvanicBlast()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, champion.getId());

        harness.addToBattlefield(player1, new EtchedChampion());
        harness.passBothPriorities();

        assertThat(champion.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(champion);
        harness.assertInGraveyard(player2, "Galvanic Blast");
    }

    @Test
    @DisplayName("Gaining metalcraft removes a colored Aura already attached")
    void gainingMetalcraftRemovesColoredAura() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new EtchedChampion());
        harness.addToBattlefield(player1, new EtchedChampion());
        harness.setHand(player2, List.of(new Arrest()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0, champion.getId());
        harness.passBothPriorities();
        assertThat(findPermanent(player2, "Arrest").getAttachedTo()).isEqualTo(champion.getId());

        harness.addToBattlefield(player1, new EtchedChampion());
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player2, "Arrest");
        harness.assertInGraveyard(player2, "Arrest");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(champion);
    }

    @Test
    @DisplayName("Colorless Equipment can attach while metalcraft is active")
    void allowsColorlessEquipment() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new EtchedChampion());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, champion.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(champion.getId());
        assertThat(gqs.hasProtectionFrom(gd, champion, CardColor.RED)).isTrue();
    }
}
