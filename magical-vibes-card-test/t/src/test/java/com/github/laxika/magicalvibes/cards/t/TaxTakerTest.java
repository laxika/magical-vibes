package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RhysticTutor;

import com.github.laxika.magicalvibes.cards.w.WhirlwindDenial;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.cards.d.Divert;
import com.github.laxika.magicalvibes.cards.a.AegisSculptor;
import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.a.ArcumsWhistle;
import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.c.CrystalShard;
import com.github.laxika.magicalvibes.cards.c.CutTheTethers;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.e.Erosion;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsolationCell;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.l.LimDLsHex;
import com.github.laxika.magicalvibes.cards.m.MindWhip;
import com.github.laxika.magicalvibes.cards.o.Oasis;
import com.github.laxika.magicalvibes.cards.r.RishadanCutpurse;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SoulBarrier;
import com.github.laxika.magicalvibes.cards.e.EsperSentinel;
import com.github.laxika.magicalvibes.cards.i.IcefallRegent;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.n.NornsAnnex;
import com.github.laxika.magicalvibes.cards.s.SmotheringTithe;
import com.github.laxika.magicalvibes.cards.u.UltimatePrice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TaxTaker.class, TitheTaker.class, DarkRitual.class, EsperSentinel.class,
        MindStone.class, NornsAnnex.class, SmotheringTithe.class, IcefallRegent.class, UltimatePrice.class,
        AegisSculptor.class, GiantGrowth.class, SoulBarrier.class, GrizzlyBears.class, IsolationCell.class,
        LimDLsHex.class, Erosion.class, Oasis.class, CutTheTethers.class, LanternKami.class, CrystalShard.class,
        AlphaMyr.class, ArcumsWhistle.class, BalduvianBears.class, TitaniaRuggedRumbler.class, Shock.class,
        MindWhip.class, Divert.class, Firebolt.class, LightningBolt.class, WhirlwindDenial.class, TheUnagiOfKyoshiIsland.class,
        RhysticTutor.class})
class TaxTakerTest extends BaseCardTest {

    @Test
    void createsTreasuresWhenAnOpponentPaysAConditionalSpellTax() {
        harness.addToBattlefield(player1, new TaxTaker());
        harness.addToBattlefield(player1, new TitheTaker());
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0);

        assertThat(countPermanents(player1, "Treasure")).isOne();
    }

    @Test
    void createsTreasuresWhenAnOpponentPaysEsperSentinelTax() {
        harness.addToBattlefield(player1, new TaxTaker());
        harness.addToBattlefield(player1, new EsperSentinel());
        prepareOpponentTurn();

        harness.setHand(player2, List.of(new MindStone()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isOne();
    }

    @Test
    void createsTreasuresEqualToTheCombinedSpellTax() {
        harness.addToBattlefield(player1, new TaxTaker());
        harness.addToBattlefield(player1, new TitheTaker());
        harness.addToBattlefield(player1, new TitheTaker());
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    @Test
    void doesNotCreateTreasuresWhenOpponentDeclinesToPay() {
        harness.addToBattlefield(player1, new TaxTaker());
        harness.addToBattlefield(player1, new EsperSentinel());
        harness.setLibrary(player1, List.of(new DarkRitual()));
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new MindStone()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isZero();
        harness.assertInHand(player1, "Dark Ritual");
    }

    @Test
    void doesNotCreateTreasuresForAnOpponentsTaxSource() {
        harness.addToBattlefield(player1, new TaxTaker());
        harness.addToBattlefield(player2, new EsperSentinel());
        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void createsTwoTreasuresWhenOpponentPaysSmotheringTithe() {
        harness.addToBattlefield(player1, new TaxTaker());
        harness.addToBattlefield(player1, new SmotheringTithe());
        harness.setLibrary(player2, List.of(new DarkRitual()));
        harness.forceActivePlayer(player2);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    @Test
    void createsTreasureWhenOpponentPaysManaToAttackThroughNornsAnnex() {
        harness.addToBattlefield(player1, new TaxTaker());
        harness.addToBattlefield(player1, new NornsAnnex());
        addCreatureReady(player2, new TitheTaker());
        harness.addMana(player2, ManaColor.WHITE, 1);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(countPermanents(player1, "Treasure")).isOne();
    }

    @Test
    void doesNotCreateTreasureWhenOpponentPaysLifeToAttackThroughNornsAnnex() {
        harness.addToBattlefield(player1, new TaxTaker());
        harness.addToBattlefield(player1, new NornsAnnex());
        addCreatureReady(player2, new TitheTaker());
        harness.setLife(player2, 20);

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void createsTreasuresWhenOpponentPaysIcefallRegentsTargetingTax() {
        harness.addToBattlefield(player1, new TaxTaker());
        var regent = harness.addToBattlefieldAndReturn(player1, new IcefallRegent());
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new UltimatePrice()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player2, 0, regent.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Icefall Regent");
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    @Nested
    @DisplayName("Mana an opponent pays as an effect of a permanent you control resolves")
    class ResolutionTimeTaxes {

        @Test
        @DisplayName("Paying a ward's {2} makes two Treasures")
        void wardPayment() {
            harness.addToBattlefield(player1, new TaxTaker());
            Permanent sculptor = harness.addToBattlefieldAndReturn(player1, new AegisSculptor());
            prepareOpponentTurn();
            harness.setHand(player2, List.of(new GiantGrowth()));
            harness.addMana(player2, ManaColor.GREEN, 1);
            harness.addMana(player2, ManaColor.COLORLESS, 2);

            harness.castInstant(player2, 0, sculptor.getId());
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player2, true);
            resolveAllTriggers();

            assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
        }

        @Test
        @CardUsed(RishadanCutpurse.class)
        @DisplayName("Paying Rishadan Cutpurse's {1} makes one Treasure")
        void rishadanCutpursePayment() {
            harness.addToBattlefield(player1, new TaxTaker());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.addMana(player2, ManaColor.COLORLESS, 1);
            harness.castFromHand(player1, new RishadanCutpurse(), "{2}{U}");

            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player2, true);
            resolveAllTriggers();

            harness.assertOnBattlefield(player2, "Grizzly Bears");
            assertThat(countPermanents(player1, "Treasure")).isOne();
        }

        @Test
        @DisplayName("Paying Soul Barrier's {2} makes two Treasures")
        void soulBarrierPayment() {
            harness.addToBattlefield(player1, new TaxTaker());
            harness.addToBattlefield(player1, new SoulBarrier());
            prepareOpponentTurn();
            harness.addMana(player2, ManaColor.COLORLESS, 2);
            harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");

            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player2, true);
            resolveAllTriggers();

            assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
        }

        @Test
        @DisplayName("Paying Isolation Cell's {2} makes two Treasures")
        void isolationCellPayment() {
            harness.addToBattlefield(player1, new TaxTaker());
            harness.addToBattlefield(player1, new IsolationCell());
            prepareOpponentTurn();
            harness.addMana(player2, ManaColor.COLORLESS, 2);
            harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");

            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player2, true);
            resolveAllTriggers();

            assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
        }

        @Test
        @DisplayName("Paying {B} for Lim-Dûl's Hex makes one Treasure, the mana paid")
        void limDulsHexPayment() {
            harness.addToBattlefield(player1, new TaxTaker());
            harness.addToBattlefield(player1, new LimDLsHex());
            giveManaSource(player1);
            giveManaSource(player2);
            advanceToUpkeep(player1);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);
            harness.addMana(player2, ManaColor.BLACK, 1);
            harness.handleMayAbilityChosen(player2, true);
            resolveAllTriggers();

            assertThat(countPermanents(player1, "Treasure")).isOne();
        }

        @Test
        @DisplayName("Paying Erosion's {1} makes one Treasure")
        void erosionPayment() {
            harness.addToBattlefield(player1, new TaxTaker());
            Permanent land = harness.addToBattlefieldAndReturn(player2, new Oasis());
            harness.addToBattlefieldAndReturn(player1, new Erosion()).setAttachedTo(land.getId());
            advanceToUpkeep(player2);
            harness.addMana(player2, ManaColor.COLORLESS, 1);

            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player2, true);
            resolveAllTriggers();

            assertThat(countPermanents(player1, "Treasure")).isOne();
        }

        @Test
        @DisplayName("Paying Cut the Tethers' {3} makes three Treasures")
        void cutTheTethersPayment() {
            harness.addToBattlefield(player1, new TaxTaker());
            harness.addToBattlefield(player2, new LanternKami());
            harness.castFromHand(player1, new CutTheTethers(), "{2}{U}{U}");
            harness.passBothPriorities();

            harness.addMana(player2, ManaColor.COLORLESS, 3);
            harness.handleMayAbilityChosen(player2, true);
            resolveAllTriggers();

            assertThat(countPermanents(player1, "Treasure")).isEqualTo(3);
        }

        @Test
        @DisplayName("Paying Crystal Shard's {1} makes one Treasure")
        void crystalShardPayment() {
            harness.addToBattlefield(player1, new TaxTaker());
            harness.addToBattlefield(player1, new CrystalShard());
            Permanent target = addCreatureReady(player2, new AlphaMyr());
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player2, ManaColor.COLORLESS, 1);

            harness.activateAbility(player1, 1, 1, null, target.getId());
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player2, true);
            resolveAllTriggers();

            assertThat(countPermanents(player1, "Treasure")).isOne();
        }

        @Test
        @DisplayName("Paying Arcum's Whistle's {X} makes X Treasures")
        void arcumsWhistlePayment() {
            harness.addToBattlefield(player1, new TaxTaker());
            addCreatureReady(player1, new ArcumsWhistle());
            harness.addMana(player1, ManaColor.COLORLESS, 3);
            Permanent bears = addCreatureReady(player2, new BalduvianBears());
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
            harness.addMana(player2, ManaColor.COLORLESS, 2);

            harness.activateAbility(player1, 1, 0, null, bears.getId());
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player2, true);
            resolveAllTriggers();

            assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
        }

        @Test
        @DisplayName("Paying Titania, Rugged Rumbler's ward {2} makes two Treasures")
        void titaniaWardPayment() {
            harness.addToBattlefield(player1, new TaxTaker());
            Permanent titania = harness.addToBattlefieldAndReturn(player1, new TitaniaRuggedRumbler());
            prepareOpponentTurn();
            harness.setHand(player2, List.of(new Shock(), new Oasis()));
            harness.addMana(player2, ManaColor.RED, 1);
            harness.addMana(player2, ManaColor.COLORLESS, 2);

            harness.castInstant(player2, 0, titania.getId());
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            // Oracle order: the discard is offered first, then the {2}.
            harness.handleMayAbilityChosen(player2, false);
            harness.handleMayAbilityChosen(player2, true);
            resolveAllTriggers();

            assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
        }

        @Test
        @DisplayName("Paying Mind Whip's {3} makes three Treasures")
        void mindWhipPayment() {
            harness.addToBattlefield(player1, new TaxTaker());
            Permanent creature = addCreatureReady(player2, new BalduvianBears());
            harness.addToBattlefieldAndReturn(player1, new MindWhip()).setAttachedTo(creature.getId());
            advanceToUpkeep(player2);
            harness.addMana(player2, ManaColor.COLORLESS, 3);

            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player2, true);
            resolveAllTriggers();

            assertThat(countPermanents(player1, "Treasure")).isEqualTo(3);
        }

        @Test
        @DisplayName("An opponent paying Rhystic Tutor's {2} makes two Treasures")
        void rhysticTutorPayment() {
            harness.addToBattlefield(player1, new TaxTaker());
            harness.setLibrary(player1, List.of(new GrizzlyBears()));
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.castFromHand(player1, new RhysticTutor(), "{2}{B}");
            harness.passBothPriorities();

            harness.addMana(player2, ManaColor.COLORLESS, 2);
            harness.handleMayAbilityChosen(player1, false);
            harness.handleMayAbilityChosen(player2, true);
            resolveAllTriggers();

            assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
        }
    }

    private void prepareOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Paying {2} to keep a spell's target against Divert makes two Treasures")
    void divertPayment() {
        harness.addToBattlefield(player2, new TaxTaker());
        Firebolt firebolt = new Firebolt();
        harness.setHand(player1, List.of(firebolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new Divert()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, firebolt.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Treasure")).isEqualTo(2);
    }

    @Test
    @DisplayName("Paying Whirlwind Denial's {4} for a spell makes four Treasures")
    void whirlwindDenialPayment() {
        harness.addToBattlefield(player1, new TaxTaker());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        prepareOpponentTurn();
        harness.castInstant(player2, 0, player1.getId());

        harness.castFromHand(player1, new WhirlwindDenial(), "{2}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(4);
    }

    @Test
    @DisplayName("Paying ward waterbend {4} with two creatures and {2} makes two Treasures, the mana paid")
    void waterbendWardPayment() {
        harness.addToBattlefield(player1, new TaxTaker());
        Permanent unagi = harness.addToBattlefieldAndReturn(player1, new TheUnagiOfKyoshiIsland());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, unagi.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        if (gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class) != null) {
            harness.handleMultiplePermanentsChosen(player2, List.of(first.getId(), second.getId()));
        }
        resolveAllTriggers();

        assertThat(first.isTapped() && second.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }
}
