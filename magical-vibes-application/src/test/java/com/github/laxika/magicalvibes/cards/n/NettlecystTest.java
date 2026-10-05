package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LiquimetalTorque;
import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
import com.github.laxika.magicalvibes.cards.s.SealOfCleansing;
import com.github.laxika.magicalvibes.cards.s.SealOfRemoval;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Nettlecyst.class, FountainOfYouth.class, PhyrexianArena.class, GrizzlyBears.class,
        LiquimetalTorque.class, OrnithopterOfParadise.class, SealOfCleansing.class, SealOfRemoval.class})
class NettlecystTest extends BaseCardTest {

    @Test
    @DisplayName("Living weapon creates a Germ and attaches Nettlecyst to it")
    void livingWeaponCreatesAndEquipsGerm() {
        castNettlecyst();

        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        Permanent nettlecyst = findPermanent(player1, "Nettlecyst");
        assertThat(germ.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(1);
        assertThat(nettlecyst.getAttachedTo()).isEqualTo(germ.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each artifact and enchantment controlled")
    void countsArtifactsAndEnchantments() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new PhyrexianArena());
        castNettlecyst();

        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip can move Nettlecyst to another creature")
    void equipsAnotherCreature() {
        castNettlecyst();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent nettlecyst = findPermanent(player1, "Nettlecyst");
        int nettlecystIndex = gd.playerBattlefields.get(player1.getId()).indexOf(nettlecyst);
        harness.activateAbility(player1, nettlecystIndex, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(nettlecyst.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Opposing artifacts and enchantments do not increase the bonus")
    void ignoresOpponentsPermanents() {
        harness.addToBattlefield(player2, new OrnithopterOfParadise());
        harness.addToBattlefield(player2, new SealOfRemoval());
        castNettlecyst();

        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(1);
    }

    @Test
    @DisplayName("The bonus decreases when an enchantment is sacrificed and an artifact leaves")
    void updatesCountAsPermanentsLeave() {
        harness.addToBattlefield(player1, new SealOfRemoval());
        harness.addToBattlefield(player1, new OrnithopterOfParadise());
        castNettlecyst();
        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(3);

        Permanent seal = findPermanent(player1, "Seal of Removal");
        Permanent thopter = findPermanent(player1, "Ornithopter of Paradise");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(seal),
                null, thopter.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(1);
        assertThat(countPermanents(player1, "Seal of Removal")).isZero();
        assertThat(countPermanents(player1, "Ornithopter of Paradise")).isZero();
    }

    @Test
    @DisplayName("A permanent that is both an artifact and an enchantment counts only once")
    void countsArtifactEnchantmentOnlyOnce() {
        harness.addToBattlefield(player1, new LiquimetalTorque());
        harness.addToBattlefield(player1, new SealOfRemoval());
        castNettlecyst();
        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        Permanent torque = findPermanent(player1, "Liquimetal Torque");
        Permanent seal = findPermanent(player1, "Seal of Removal");
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(torque),
                1, null, seal.getId());
        resolveAllTriggers();

        assertThat(gqs.isArtifact(gd, seal)).isTrue();
        assertThat(gqs.isEnchantment(gd, seal)).isTrue();
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature becoming an artifact increases Nettlecyst's bonus immediately")
    void countsNewlyGrantedArtifactType() {
        harness.addToBattlefield(player1, new LiquimetalTorque());
        castNettlecyst();
        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        Permanent torque = findPermanent(player1, "Liquimetal Torque");
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(torque),
                1, null, germ.getId());
        resolveAllTriggers();

        assertThat(gqs.isArtifact(gd, germ)).isTrue();
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(3);
    }

    @Test
    @DisplayName("Moving Nettlecyst leaves the unboosted Germ to die")
    void germDiesWhenEquipmentMoves() {
        Permanent thopter = addCreatureReady(player1, new OrnithopterOfParadise());
        castNettlecyst();
        Permanent nettlecyst = findPermanent(player1, "Nettlecyst");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(nettlecyst),
                null, thopter.getId());
        resolveAllTriggers();

        assertThat(nettlecyst.getAttachedTo()).isEqualTo(thopter.getId());
        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(4);
        assertThat(countPermanents(player1, "Phyrexian Germ")).isZero();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent opposingCreature = addCreatureReady(player2, new OrnithopterOfParadise());
        castNettlecyst();
        Permanent nettlecyst = findPermanent(player1, "Nettlecyst");
        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(nettlecyst),
                null, opposingCreature.getId())).isInstanceOf(IllegalStateException.class);

        assertThat(nettlecyst.getAttachedTo()).isEqualTo(germ.getId());
    }

    @Test
    @DisplayName("Equip requires payment of two mana")
    void cannotEquipWithOnlyOneMana() {
        Permanent thopter = addCreatureReady(player1, new OrnithopterOfParadise());
        castNettlecyst();
        Permanent nettlecyst = findPermanent(player1, "Nettlecyst");
        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(nettlecyst),
                null, thopter.getId())).isInstanceOf(IllegalStateException.class);

        assertThat(nettlecyst.getAttachedTo()).isEqualTo(germ.getId());
    }

    @Test
    @DisplayName("Equip cannot be activated during an opponent's turn")
    void cannotEquipDuringOpponentsTurn() {
        Permanent thopter = addCreatureReady(player1, new OrnithopterOfParadise());
        castNettlecyst();
        Permanent nettlecyst = findPermanent(player1, "Nettlecyst");
        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(nettlecyst),
                null, thopter.getId())).isInstanceOf(IllegalStateException.class);

        assertThat(nettlecyst.getAttachedTo()).isEqualTo(germ.getId());
    }

    @Test
    @DisplayName("Living weapon still creates a Germ if Nettlecyst is destroyed in response")
    void livingWeaponResolvesWithoutEquipment() {
        harness.addToBattlefield(player2, new SealOfCleansing());
        harness.castFromHand(player1, new Nettlecyst(), "{3}");
        harness.passBothPriorities();
        Permanent nettlecyst = findPermanent(player1, "Nettlecyst");
        assertThat(countPermanents(player1, "Phyrexian Germ")).isZero();

        harness.activateAbility(player2, 0, null, nettlecyst.getId());
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Nettlecyst")).isZero();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Phyrexian Germ")).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("Phyrexian Germ creature token enters the battlefield")).isTrue();
    }

    private void castNettlecyst() {
        harness.castFromHand(player1, new Nettlecyst(), "{3}");
        resolveAllTriggers();
    }
}
