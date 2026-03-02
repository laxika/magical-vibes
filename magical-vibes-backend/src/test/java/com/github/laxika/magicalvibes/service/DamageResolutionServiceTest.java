package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.a.ArcTrail;
import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.b.BurnTheImpure;
import com.github.laxika.magicalvibes.cards.c.ConsumeSpirit;
import com.github.laxika.magicalvibes.cards.e.EssenceDrain;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.l.LavabornMuse;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.OrcishArtillery;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SpittingEarth;
import com.github.laxika.magicalvibes.cards.s.SuddenImpact;
import com.github.laxika.magicalvibes.cards.w.WingPuncture;
import com.github.laxika.magicalvibes.cards.b.Blightwidow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DamageResolutionServiceTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    // =========================================================================
    // DealDamageToAnyTargetEffect (via Shock — instant, {R}, 2 damage)
    // =========================================================================

    @Nested
    @DisplayName("resolveDealDamageToAnyTarget")
    class ResolveDealDamageToAnyTarget {

        @Test
        @DisplayName("Deals lethal damage to a creature and destroys it")
        void dealsLethalDamageToCreatureAndDestroysIt() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.addToBattlefield(player2, bears);

            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

            harness.assertInGraveyard(player2, "Grizzly Bears");
            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Deals non-lethal damage to a creature and it survives")
        void dealsNonLethalDamageToCreature() {
            // Serra Angel is a 4/4 — 2 damage from Shock is not lethal
            SerraAngel angel = new SerraAngel();
            harness.addToBattlefield(player2, angel);

            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Serra Angel"));

            harness.assertOnBattlefield(player2, "Serra Angel");
            harness.assertNotInGraveyard(player2, "Serra Angel");
        }

        @Test
        @DisplayName("Deals damage to a player and reduces their life total")
        void dealsDamageToPlayer() {
            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castAndResolveInstant(player1, 0, player2.getId());

            harness.assertLife(player2, 18);
        }

        @Test
        @DisplayName("Fizzles when target creature is no longer on the battlefield")
        void fizzlesWhenTargetGone() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.addToBattlefield(player2, bears);
            UUID bearsPermId = harness.getPermanentId(player2, "Grizzly Bears");

            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, bearsPermId);

            // Remove bears before Shock resolves
            gd.playerBattlefields.get(player2.getId()).clear();

            harness.passBothPriorities();

            assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        }

        @Test
        @DisplayName("Damage is logged in the game log")
        void damageIsLogged() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.addToBattlefield(player2, bears);

            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("Shock") && log.contains("2 damage") && log.contains("Grizzly Bears"));
        }
    }

    // =========================================================================
    // DealDamageToTargetCreatureEffect (via Burn the Impure — instant, {1}{R}, 3 damage to creature)
    // =========================================================================

    @Nested
    @DisplayName("resolveDealDamageToTargetCreature")
    class ResolveDealDamageToTargetCreature {

        @Test
        @DisplayName("Deals 3 damage to a creature and destroys it")
        void deals3DamageToCreature() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.addToBattlefield(player2, bears);

            harness.setHand(player1, List.of(new BurnTheImpure()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

            harness.assertInGraveyard(player2, "Grizzly Bears");
            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        }
    }

    // =========================================================================
    // DealDamageToTargetControllerIfTargetHasKeywordEffect (via Burn the Impure)
    // =========================================================================

    @Nested
    @DisplayName("resolveDealDamageToTargetControllerIfTargetHasKeyword")
    class ResolveDealDamageToTargetControllerIfTargetHasKeyword {

        @Test
        @DisplayName("Deals bonus damage to controller when target creature has infect")
        void dealsBonusDamageWhenTargetHasInfect() {
            // Blightwidow is a 2/4 with reach and infect — survives 3 damage
            harness.addToBattlefield(player2, new Blightwidow());

            harness.setHand(player1, List.of(new BurnTheImpure()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Blightwidow"));

            // 3 damage to 2/4 creature (survives) + 3 damage to controller (infect bonus)
            harness.assertOnBattlefield(player2, "Blightwidow");
            harness.assertLife(player2, 17);
        }

        @Test
        @DisplayName("Does not deal bonus damage when target creature lacks the keyword")
        void noBonusDamageWhenTargetLacksKeyword() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.addToBattlefield(player2, bears);

            harness.setHand(player1, List.of(new BurnTheImpure()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

            // 3 damage kills the creature, but no bonus damage to controller
            harness.assertInGraveyard(player2, "Grizzly Bears");
            harness.assertLife(player2, 20);
        }
    }

    // =========================================================================
    // DealDamageToTargetPlayerEffect (via Lava Axe — sorcery, {4}{R}, 5 damage to player)
    // =========================================================================

    @Nested
    @DisplayName("resolveDealDamageToTargetPlayer")
    class ResolveDealDamageToTargetPlayer {

        @Test
        @DisplayName("Deals 5 damage to target player")
        void deals5DamageToTargetPlayer() {
            harness.setHand(player1, List.of(new LavaAxe()));
            harness.addMana(player1, ManaColor.RED, 5);

            harness.castAndResolveSorcery(player1, 0, player2.getId());

            harness.assertLife(player2, 15);
        }

        @Test
        @DisplayName("Damage is logged in the game log")
        void damageIsLogged() {
            harness.setHand(player1, List.of(new LavaAxe()));
            harness.addMana(player1, ManaColor.RED, 5);

            harness.castAndResolveSorcery(player1, 0, player2.getId());

            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("5 damage"));
        }
    }

    // =========================================================================
    // DealDamageToControllerEffect (via Orcish Artillery — activated ability, deals 2 to any + 3 to self)
    // =========================================================================

    @Nested
    @DisplayName("resolveDealDamageToController")
    class ResolveDealDamageToController {

        @Test
        @DisplayName("Orcish Artillery deals 2 to target creature and 3 to its controller")
        void orcishArtilleryDeals2ToTargetAnd3ToController() {
            OrcishArtillery artillery = new OrcishArtillery();
            harness.addToBattlefield(player1, artillery);
            gd.playerBattlefields.get(player1.getId()).getLast().setSummoningSick(false);

            GrizzlyBears bears = new GrizzlyBears();
            harness.addToBattlefield(player2, bears);

            harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));
            harness.passBothPriorities();

            // 2 damage kills 2/2 Grizzly Bears
            harness.assertInGraveyard(player2, "Grizzly Bears");
            // 3 damage to the controller (player1)
            harness.assertLife(player1, 17);
        }

        @Test
        @DisplayName("Orcish Artillery deals 2 to target player and 3 to its controller")
        void orcishArtilleryDeals2ToPlayerAnd3ToController() {
            OrcishArtillery artillery = new OrcishArtillery();
            harness.addToBattlefield(player1, artillery);
            gd.playerBattlefields.get(player1.getId()).getLast().setSummoningSick(false);

            harness.activateAbility(player1, 0, null, player2.getId());
            harness.passBothPriorities();

            harness.assertLife(player2, 18);
            harness.assertLife(player1, 17);
        }
    }

    // =========================================================================
    // MassDamageEffect (via Pyroclasm — sorcery, {1}{R}, 2 damage to each creature)
    // =========================================================================

    @Nested
    @DisplayName("resolveMassDamage")
    class ResolveMassDamage {

        @Test
        @DisplayName("Pyroclasm kills all 2-toughness creatures on both sides")
        void pyroclasmKills2ToughnessCreatures() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new LlanowarElves());

            harness.setHand(player1, List.of(new Pyroclasm()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castAndResolveSorcery(player1, 0, 0);

            harness.assertInGraveyard(player1, "Grizzly Bears");
            harness.assertInGraveyard(player2, "Llanowar Elves");
        }

        @Test
        @DisplayName("Pyroclasm does not kill creatures with toughness > 2")
        void pyroclasmDoesNotKillHighToughnessCreatures() {
            // Serra Angel is 4/4
            harness.addToBattlefield(player1, new SerraAngel());
            harness.addToBattlefield(player2, new GrizzlyBears());

            harness.setHand(player1, List.of(new Pyroclasm()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castAndResolveSorcery(player1, 0, 0);

            harness.assertOnBattlefield(player1, "Serra Angel");
            harness.assertInGraveyard(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Hurricane deals X damage to flying creatures and all players")
        void hurricaneDealsXDamageToFlyingCreaturesAndPlayers() {
            // Serra Angel has flying (4/4)
            harness.addToBattlefield(player2, new SerraAngel());
            // Grizzly Bears does not have flying — should survive
            harness.addToBattlefield(player1, new GrizzlyBears());

            harness.setHand(player1, List.of(new Hurricane()));
            harness.addMana(player1, ManaColor.GREEN, 5);

            // Cast Hurricane with X=4 to kill Serra Angel (4 damage to 4 toughness)
            harness.castAndResolveSorcery(player1, 0, 4);

            harness.assertInGraveyard(player2, "Serra Angel");
            // Non-flying Grizzly Bears should survive
            harness.assertOnBattlefield(player1, "Grizzly Bears");
            // Both players take 4 damage
            harness.assertLife(player1, 16);
            harness.assertLife(player2, 16);
        }
    }

    // =========================================================================
    // DealXDamageToAnyTargetEffect (via Blaze — sorcery, {X}{R}, X damage to any target)
    // =========================================================================

    @Nested
    @DisplayName("resolveDealXDamageToAnyTarget")
    class ResolveDealXDamageToAnyTarget {

        @Test
        @DisplayName("Blaze for X=3 deals 3 damage to a creature and destroys it")
        void blazeDeals3DamageToCreature() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.addToBattlefield(player2, bears);

            harness.setHand(player1, List.of(new Blaze()));
            harness.addMana(player1, ManaColor.RED, 4);

            harness.castAndResolveSorcery(player1, 0, 3, harness.getPermanentId(player2, "Grizzly Bears"));

            harness.assertInGraveyard(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Blaze for X=5 deals 5 damage to a player")
        void blazeDeals5DamageToPlayer() {
            harness.setHand(player1, List.of(new Blaze()));
            harness.addMana(player1, ManaColor.RED, 6);

            harness.castAndResolveSorcery(player1, 0, 5, player2.getId());

            harness.assertLife(player2, 15);
        }
    }

    // =========================================================================
    // DealDamageToAnyTargetAndGainLifeEffect (via Essence Drain — sorcery, {4}{B}, 3 damage + 3 life)
    // =========================================================================

    @Nested
    @DisplayName("resolveDealDamageToAnyTargetAndGainLife")
    class ResolveDealDamageToAnyTargetAndGainLife {

        @Test
        @DisplayName("Deals 3 damage to target player and controller gains 3 life")
        void deals3DamageAndGains3Life() {
            harness.setHand(player1, List.of(new EssenceDrain()));
            harness.addMana(player1, ManaColor.BLACK, 5);

            harness.castAndResolveSorcery(player1, 0, player2.getId());

            harness.assertLife(player2, 17);
            harness.assertLife(player1, 23);
        }

        @Test
        @DisplayName("Deals 3 damage to creature and controller gains 3 life")
        void deals3DamageToCreatureAndGains3Life() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.addToBattlefield(player2, bears);

            harness.setHand(player1, List.of(new EssenceDrain()));
            harness.addMana(player1, ManaColor.BLACK, 5);

            harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

            harness.assertInGraveyard(player2, "Grizzly Bears");
            harness.assertLife(player1, 23);
        }
    }

    // =========================================================================
    // DealXDamageToAnyTargetAndGainXLifeEffect (via Consume Spirit — sorcery, {X}{1}{B}, X damage + X life)
    // =========================================================================

    @Nested
    @DisplayName("resolveDealXDamageToAnyTargetAndGainXLife")
    class ResolveDealXDamageToAnyTargetAndGainXLife {

        @Test
        @DisplayName("Consume Spirit for X=3 deals 3 damage and gains 3 life")
        void consumeSpiritDeals3AndGains3() {
            harness.setHand(player1, List.of(new ConsumeSpirit()));
            // Consume Spirit costs {X}{1}{B} with X restricted to black mana; X=3 needs 3B + 1 generic + 1B = 4B + 1
            harness.addMana(player1, ManaColor.BLACK, 5);

            harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

            harness.assertLife(player2, 17);
            harness.assertLife(player1, 23);
        }
    }

    // =========================================================================
    // DealDamageToTargetPlayerByHandSizeEffect (via Sudden Impact — instant, {3}{R})
    // =========================================================================

    @Nested
    @DisplayName("resolveDealDamageToTargetPlayerByHandSize")
    class ResolveDealDamageToTargetPlayerByHandSize {

        @Test
        @DisplayName("Deals damage equal to target player's hand size")
        void dealsDamageEqualToHandSize() {
            // Give player2 a hand of 5 cards
            harness.setHand(player2, List.of(
                    new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                    new GrizzlyBears(), new GrizzlyBears()
            ));

            harness.setHand(player1, List.of(new SuddenImpact()));
            harness.addMana(player1, ManaColor.RED, 4);

            harness.castAndResolveInstant(player1, 0, player2.getId());

            harness.assertLife(player2, 15);
        }

        @Test
        @DisplayName("Deals 0 damage when target has an empty hand")
        void dealsZeroDamageWhenEmptyHand() {
            harness.setHand(player2, List.of());

            harness.setHand(player1, List.of(new SuddenImpact()));
            harness.addMana(player1, ManaColor.RED, 4);

            harness.castAndResolveInstant(player1, 0, player2.getId());

            harness.assertLife(player2, 20);
        }
    }

    // =========================================================================
    // DealDamageIfFewCardsInHandEffect (via Lavaborn Muse — opponent upkeep trigger, 3 damage if ≤ 2 cards)
    // =========================================================================

    @Nested
    @DisplayName("resolveDealDamageIfFewCardsInHand")
    class ResolveDealDamageIfFewCardsInHand {

        @Test
        @DisplayName("Deals 3 damage when opponent has 2 cards in hand")
        void deals3DamageWhenOpponentHas2Cards() {
            harness.addToBattlefield(player1, new LavabornMuse());
            harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

            advanceToUpkeep(player2);
            harness.passBothPriorities(); // resolve trigger

            harness.assertLife(player2, 17);
        }

        @Test
        @DisplayName("Deals 3 damage when opponent has 0 cards in hand")
        void deals3DamageWhenOpponentHasEmptyHand() {
            harness.addToBattlefield(player1, new LavabornMuse());
            harness.setHand(player2, List.of());

            advanceToUpkeep(player2);
            harness.passBothPriorities(); // resolve trigger

            harness.assertLife(player2, 17);
        }

        @Test
        @DisplayName("Does nothing when opponent has more than 2 cards in hand")
        void doesNothingWhenOpponentHasMoreThan2Cards() {
            harness.addToBattlefield(player1, new LavabornMuse());
            harness.setHand(player2, List.of(
                    new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()
            ));

            advanceToUpkeep(player2);
            harness.passBothPriorities();

            harness.assertLife(player2, 20);
        }
    }

    // =========================================================================
    // DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect (via Spitting Earth — sorcery, {1}{R})
    // =========================================================================

    @Nested
    @DisplayName("resolveDealDamageToTargetCreatureEqualToControlledSubtypeCount")
    class ResolveDealDamageToTargetCreatureEqualToControlledSubtypeCount {

        @Test
        @DisplayName("Deals damage equal to number of Mountains you control")
        void dealsDamageEqualToMountainCount() {
            // Add 3 Mountains to player1's battlefield
            harness.addToBattlefield(player1, new Mountain());
            harness.addToBattlefield(player1, new Mountain());
            harness.addToBattlefield(player1, new Mountain());

            // Serra Angel is 4/4 — 3 damage is not lethal
            SerraAngel angel = new SerraAngel();
            harness.addToBattlefield(player2, angel);

            harness.setHand(player1, List.of(new SpittingEarth()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Serra Angel"));

            // 3 damage to 4/4 — survives
            harness.assertOnBattlefield(player2, "Serra Angel");
        }

        @Test
        @DisplayName("Kills creature when damage equals toughness")
        void killsCreatureWhenDamageEqualsOrExceedsToughness() {
            harness.addToBattlefield(player1, new Mountain());
            harness.addToBattlefield(player1, new Mountain());

            GrizzlyBears bears = new GrizzlyBears();
            harness.addToBattlefield(player2, bears);

            harness.setHand(player1, List.of(new SpittingEarth()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

            // 2 damage to 2/2 — kills it
            harness.assertInGraveyard(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Deals 0 damage when controller has no Mountains")
        void dealsZeroDamageWithNoMountains() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.addToBattlefield(player2, bears);

            harness.setHand(player1, List.of(new SpittingEarth()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

            // 0 damage — creature survives
            harness.assertOnBattlefield(player2, "Grizzly Bears");
        }
    }

    // =========================================================================
    // DealOrderedDamageToAnyTargetsEffect (via Arc Trail — sorcery, {1}{R}, 2 to first + 1 to second)
    // =========================================================================

    @Nested
    @DisplayName("resolveDealOrderedDamageToAnyTargets")
    class ResolveDealOrderedDamageToAnyTargets {

        @Test
        @DisplayName("Deals 2 damage to first target creature and 1 to second target creature")
        void deals2And1DamageToTwoCreatures() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.addToBattlefield(player2, bears);

            LlanowarElves elves = new LlanowarElves();
            harness.addToBattlefield(player2, elves);

            harness.setHand(player1, List.of(new ArcTrail()));
            harness.addMana(player1, ManaColor.RED, 2);

            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
            UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

            harness.castAndResolveSorcery(player1, 0, List.of(bearsId, elvesId));

            // 2 damage kills 2/2 Grizzly Bears; 1 damage kills 1/1 Llanowar Elves
            harness.assertInGraveyard(player2, "Grizzly Bears");
            harness.assertInGraveyard(player2, "Llanowar Elves");
        }

        @Test
        @DisplayName("Deals 2 damage to a creature and 1 damage to a player")
        void deals2ToCreatureAnd1ToPlayer() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.addToBattlefield(player2, bears);

            harness.setHand(player1, List.of(new ArcTrail()));
            harness.addMana(player1, ManaColor.RED, 2);

            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

            harness.castAndResolveSorcery(player1, 0, List.of(bearsId, player2.getId()));

            harness.assertInGraveyard(player2, "Grizzly Bears");
            harness.assertLife(player2, 19);
        }
    }

    // =========================================================================
    // FirstTargetDealsPowerDamageToSecondTargetEffect (via Wing Puncture — instant, {G})
    // =========================================================================

    @Nested
    @DisplayName("resolveBite")
    class ResolveBite {

        @Test
        @DisplayName("Source creature deals its power as damage to target flyer")
        void sourceDealsItsePowerAsaDamageToFlyer() {
            // GrizzlyBears is 2/2, Serra Angel is 4/4 with flying
            GrizzlyBears bears = new GrizzlyBears();
            harness.addToBattlefield(player1, bears);

            SerraAngel angel = new SerraAngel();
            harness.addToBattlefield(player2, angel);

            harness.setHand(player1, List.of(new WingPuncture()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
            UUID angelId = harness.getPermanentId(player2, "Serra Angel");

            harness.castAndResolveInstant(player1, 0, List.of(bearsId, angelId));

            // 2 power vs 4 toughness — Serra Angel survives
            harness.assertOnBattlefield(player2, "Serra Angel");
        }

        @Test
        @DisplayName("Kills target flyer when source power >= target toughness")
        void killsFlyerWhenPowerIsLethal() {
            // Player1 controls Serra Angel (4/4 flying), player2 controls another Serra Angel (4/4 flying)
            SerraAngel myAngel = new SerraAngel();
            harness.addToBattlefield(player1, myAngel);

            SerraAngel theirAngel = new SerraAngel();
            harness.addToBattlefield(player2, theirAngel);

            harness.setHand(player1, List.of(new WingPuncture()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            UUID myAngelId = harness.getPermanentId(player1, "Serra Angel");
            UUID theirAngelId = harness.getPermanentId(player2, "Serra Angel");

            harness.castAndResolveInstant(player1, 0, List.of(myAngelId, theirAngelId));

            // 4 power vs 4 toughness — lethal
            harness.assertInGraveyard(player2, "Serra Angel");
        }
    }

    // =========================================================================
    // DealDamageToTargetPlayerByHandSizeEffect — via Prodigal Pyromancer activated ability
    // (covered above via Sudden Impact, but let's also test the tap ability pattern)
    // =========================================================================

    @Nested
    @DisplayName("resolveDealDamageViaActivatedAbility")
    class ResolveDealDamageViaActivatedAbility {

        @Test
        @DisplayName("Prodigal Pyromancer's tap ability deals 1 damage to a creature")
        void pyromancerDeals1DamageToCreature() {
            ProdigalPyromancer pyro = new ProdigalPyromancer();
            harness.addToBattlefield(player1, pyro);
            gd.playerBattlefields.get(player1.getId()).getLast().setSummoningSick(false);

            LlanowarElves elves = new LlanowarElves();
            harness.addToBattlefield(player2, elves);

            harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Llanowar Elves"));
            harness.passBothPriorities();

            harness.assertInGraveyard(player2, "Llanowar Elves");
        }

        @Test
        @DisplayName("Prodigal Pyromancer's tap ability deals 1 damage to a player")
        void pyromancerDeals1DamageToPlayer() {
            ProdigalPyromancer pyro = new ProdigalPyromancer();
            harness.addToBattlefield(player1, pyro);
            gd.playerBattlefields.get(player1.getId()).getLast().setSummoningSick(false);

            harness.activateAbility(player1, 0, null, player2.getId());
            harness.passBothPriorities();

            harness.assertLife(player2, 19);
        }
    }
}
