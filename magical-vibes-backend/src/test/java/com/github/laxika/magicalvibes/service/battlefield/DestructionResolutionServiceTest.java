package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.d.DivineOffering;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HoardSmelterDragon;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LordOfThePit;
import com.github.laxika.magicalvibes.cards.m.MeltTerrain;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PlagueWind;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.cards.t.TurnToSlag;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DestructionResolutionServiceTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    private static Card indestructibleCreature() {
        Card card = new Card();
        card.setName("Indestructible Golem");
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setColor(null);
        card.setPower(2);
        card.setToughness(2);
        card.setKeywords(Set.of(Keyword.INDESTRUCTIBLE));
        return card;
    }

    // =========================================================================
    // DestroyAllPermanentsEffect (via Wrath of God — sorcery, {2}{W}{W}, all creatures, can't be regenerated)
    // =========================================================================

    @Nested
    @DisplayName("resolveDestroyAllPermanents")
    class ResolveDestroyAllPermanents {

        @Test
        @DisplayName("Wrath of God destroys all creatures on both sides")
        void destroysAllCreaturesOnBothSides() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new SerraAngel());

            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.castAndResolveSorcery(player1, 0, 0);

            harness.assertInGraveyard(player1, "Grizzly Bears");
            harness.assertInGraveyard(player2, "Serra Angel");
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            harness.assertNotOnBattlefield(player2, "Serra Angel");
        }

        @Test
        @DisplayName("Wrath of God does not destroy non-creature permanents")
        void doesNotDestroyNonCreatures() {
            harness.addToBattlefield(player1, new Spellbook());
            harness.addToBattlefield(player1, new GrizzlyBears());

            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.castAndResolveSorcery(player1, 0, 0);

            harness.assertOnBattlefield(player1, "Spellbook");
            harness.assertInGraveyard(player1, "Grizzly Bears");
        }

        @Test
        @DisplayName("Indestructible creatures survive Wrath of God")
        void indestructibleCreaturesSurvive() {
            harness.addToBattlefield(player2, indestructibleCreature());
            harness.addToBattlefield(player2, new GrizzlyBears());

            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.castAndResolveSorcery(player1, 0, 0);

            harness.assertOnBattlefield(player2, "Indestructible Golem");
            harness.assertInGraveyard(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Indestructible status is logged")
        void indestructibleIsLogged() {
            harness.addToBattlefield(player2, indestructibleCreature());

            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.castAndResolveSorcery(player1, 0, 0);

            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("Indestructible Golem") && log.contains("indestructible"));
        }

        @Test
        @DisplayName("Plague Wind only destroys opponents' creatures")
        void plagueWindOnlyDestroysOpponentsCreatures() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new SerraAngel());
            harness.addToBattlefield(player2, new LlanowarElves());

            harness.setHand(player1, List.of(new PlagueWind()));
            harness.addMana(player1, ManaColor.BLACK, 9);

            harness.castAndResolveSorcery(player1, 0, 0);

            // Player1's creatures survive
            harness.assertOnBattlefield(player1, "Grizzly Bears");
            // Player2's creatures are destroyed
            harness.assertInGraveyard(player2, "Serra Angel");
            harness.assertInGraveyard(player2, "Llanowar Elves");
        }

        @Test
        @DisplayName("Destruction is logged for each destroyed creature")
        void destructionIsLogged() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new LlanowarElves());

            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.castAndResolveSorcery(player1, 0, 0);

            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("Grizzly Bears") && log.contains("destroyed"));
            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("Llanowar Elves") && log.contains("destroyed"));
        }
    }

    // =========================================================================
    // DestroyTargetPermanentEffect (via Terror — instant, {1}{B}, destroy nonartifact nonblack creature)
    // =========================================================================

    @Nested
    @DisplayName("resolveDestroyTargetPermanent")
    class ResolveDestroyTargetPermanent {

        @Test
        @DisplayName("Terror destroys target creature")
        void destroysTargetCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());

            harness.setHand(player1, List.of(new Terror()));
            harness.addMana(player1, ManaColor.BLACK, 2);

            harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

            harness.assertInGraveyard(player2, "Grizzly Bears");
            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Shatter destroys target artifact")
        void destroysTargetArtifact() {
            harness.addToBattlefield(player2, new Spellbook());

            harness.setHand(player1, List.of(new Shatter()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Spellbook"));

            harness.assertInGraveyard(player2, "Spellbook");
            harness.assertNotOnBattlefield(player2, "Spellbook");
        }

        @Test
        @DisplayName("Fizzles when target is removed before resolution")
        void fizzlesWhenTargetRemoved() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

            harness.setHand(player1, List.of(new Terror()));
            harness.addMana(player1, ManaColor.BLACK, 2);

            harness.castInstant(player1, 0, bearsId);

            // Remove target before resolution
            gd.playerBattlefields.get(player2.getId()).clear();

            harness.passBothPriorities();

            assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        }

        @Test
        @DisplayName("Indestructible creature survives targeted destruction")
        void indestructibleSurvivesTargetedDestruction() {
            harness.addToBattlefield(player2, indestructibleCreature());

            harness.setHand(player1, List.of(new Terror()));
            harness.addMana(player1, ManaColor.BLACK, 2);

            harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Indestructible Golem"));

            harness.assertOnBattlefield(player2, "Indestructible Golem");
            harness.assertNotInGraveyard(player2, "Indestructible Golem");
        }

        @Test
        @DisplayName("Destruction is logged")
        void destructionIsLogged() {
            harness.addToBattlefield(player2, new GrizzlyBears());

            harness.setHand(player1, List.of(new Terror()));
            harness.addMana(player1, ManaColor.BLACK, 2);

            harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("Grizzly Bears") && log.contains("destroyed"));
        }
    }

    // =========================================================================
    // DestroyEquipmentAttachedToTargetCreatureEffect (via Turn to Slag — sorcery, {3}{R}{R})
    // =========================================================================

    @Nested
    @DisplayName("resolveDestroyEquipmentAttachedToTargetCreature")
    class ResolveDestroyEquipmentAttachedToTargetCreature {

        @Test
        @DisplayName("Destroys equipment attached to target creature")
        void destroysAttachedEquipment() {
            // Add creature and equipment attached to it
            harness.addToBattlefield(player2, new SerraAngel());
            UUID angelId = harness.getPermanentId(player2, "Serra Angel");

            harness.addToBattlefield(player2, new LeoninScimitar());
            Permanent scimitar = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Leonin Scimitar"))
                    .findFirst().orElseThrow();
            scimitar.setAttachedTo(angelId);

            harness.setHand(player1, List.of(new TurnToSlag()));
            harness.addMana(player1, ManaColor.RED, 5);

            harness.castAndResolveSorcery(player1, 0, angelId);

            harness.assertInGraveyard(player2, "Leonin Scimitar");
            harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        }

        @Test
        @DisplayName("No equipment to destroy resolves without error")
        void noEquipmentResolvesCleanly() {
            harness.addToBattlefield(player2, new SerraAngel());
            UUID angelId = harness.getPermanentId(player2, "Serra Angel");

            harness.setHand(player1, List.of(new TurnToSlag()));
            harness.addMana(player1, ManaColor.RED, 5);

            // Should resolve without error — the 5 damage kills Serra Angel (4/4)
            harness.castAndResolveSorcery(player1, 0, angelId);

            harness.assertInGraveyard(player2, "Serra Angel");
        }
    }

    // =========================================================================
    // DestroyTargetLandAndDamageControllerEffect (via Melt Terrain — sorcery, {2}{R}{R}, 2 damage)
    // =========================================================================

    @Nested
    @DisplayName("resolveDestroyTargetLandAndDamageController")
    class ResolveDestroyTargetLandAndDamageController {

        @Test
        @DisplayName("Destroys target land and deals 2 damage to its controller")
        void destroysLandAndDealsDamage() {
            harness.addToBattlefield(player2, new Mountain());

            harness.setHand(player1, List.of(new MeltTerrain()));
            harness.addMana(player1, ManaColor.RED, 4);

            harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Mountain"));

            harness.assertInGraveyard(player2, "Mountain");
            harness.assertNotOnBattlefield(player2, "Mountain");
            harness.assertLife(player2, 18);
        }

        @Test
        @DisplayName("Fizzles when target land is removed before resolution")
        void fizzlesWhenTargetRemoved() {
            harness.addToBattlefield(player2, new Mountain());
            UUID mountainId = harness.getPermanentId(player2, "Mountain");

            harness.setHand(player1, List.of(new MeltTerrain()));
            harness.addMana(player1, ManaColor.RED, 4);

            harness.castSorcery(player1, 0, mountainId);

            // Remove land before resolution
            gd.playerBattlefields.get(player2.getId()).clear();

            harness.passBothPriorities();

            // Player2 life should remain unchanged since the spell fizzled
            harness.assertLife(player2, 20);
        }

        @Test
        @DisplayName("Destruction and damage are logged")
        void destructionAndDamageAreLogged() {
            harness.addToBattlefield(player2, new Mountain());

            harness.setHand(player1, List.of(new MeltTerrain()));
            harness.addMana(player1, ManaColor.RED, 4);

            harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Mountain"));

            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("Mountain") && log.contains("destroyed"));
            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("damage") && log.contains("Melt Terrain"));
        }
    }

    // =========================================================================
    // SacrificeCreatureEffect (via Cruel Edict — sorcery, {1}{B})
    // =========================================================================

    @Nested
    @DisplayName("resolveSacrificeCreature")
    class ResolveSacrificeCreature {

        @Test
        @DisplayName("Opponent with one creature sacrifices it automatically")
        void autoSacrificesOnlyCreature() {
            Permanent creature = new Permanent(new GrizzlyBears());
            gd.playerBattlefields.get(player2.getId()).add(creature);

            harness.setHand(player1, List.of(new CruelEdict()));
            harness.addMana(player1, ManaColor.BLACK, 2);

            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            harness.assertInGraveyard(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Opponent with multiple creatures is prompted to choose")
        void promptsChoiceWithMultipleCreatures() {
            gd.playerBattlefields.get(player2.getId()).add(new Permanent(new GrizzlyBears()));
            gd.playerBattlefields.get(player2.getId()).add(new Permanent(new GiantSpider()));

            harness.setHand(player1, List.of(new CruelEdict()));
            harness.addMana(player1, ManaColor.BLACK, 2);

            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
            assertThat(gd.interaction.permanentChoice().playerId()).isEqualTo(player2.getId());
            assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);
        }

        @Test
        @DisplayName("No effect when opponent has no creatures")
        void noEffectWithNoCreatures() {
            harness.setHand(player1, List.of(new CruelEdict()));
            harness.addMana(player1, ManaColor.BLACK, 2);

            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.gameLog).anyMatch(log -> log.contains("no creatures to sacrifice"));
        }

        @Test
        @DisplayName("Sacrifice is logged")
        void sacrificeIsLogged() {
            gd.playerBattlefields.get(player2.getId()).add(new Permanent(new GrizzlyBears()));

            harness.setHand(player1, List.of(new CruelEdict()));
            harness.addMana(player1, ManaColor.BLACK, 2);

            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("sacrifices") && log.contains("Grizzly Bears"));
        }
    }

    // =========================================================================
    // SacrificeOtherCreatureOrDamageEffect (via Lord of the Pit — upkeep trigger, 7 damage)
    // =========================================================================

    @Nested
    @DisplayName("resolveSacrificeOtherCreatureOrDamage")
    class ResolveSacrificeOtherCreatureOrDamage {

        @Test
        @DisplayName("Deals 7 damage to controller when no other creatures are present")
        void dealsDamageWhenNoOtherCreatures() {
            harness.addToBattlefield(player1, new LordOfThePit());

            advanceToUpkeep(player1);
            harness.passBothPriorities(); // resolve trigger

            harness.assertLife(player1, 13);
        }

        @Test
        @DisplayName("Sacrifices the only other creature automatically")
        void autoSacrificesOnlyOtherCreature() {
            harness.addToBattlefield(player1, new LordOfThePit());
            harness.addToBattlefield(player1, new LlanowarElves());

            advanceToUpkeep(player1);
            harness.passBothPriorities(); // resolve trigger

            harness.assertInGraveyard(player1, "Llanowar Elves");
            harness.assertNotOnBattlefield(player1, "Llanowar Elves");
            // No damage dealt when sacrifice succeeds
            harness.assertLife(player1, 20);
        }

        @Test
        @DisplayName("Prompts choice when multiple other creatures exist")
        void promptsChoiceWithMultipleOtherCreatures() {
            harness.addToBattlefield(player1, new LordOfThePit());
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player1, new LlanowarElves());

            advanceToUpkeep(player1);
            harness.passBothPriorities(); // resolve trigger

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
            assertThat(gd.interaction.permanentChoice().playerId()).isEqualTo(player1.getId());
            assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);
        }

        @Test
        @DisplayName("Damage is logged when no creatures to sacrifice")
        void damageIsLogged() {
            harness.addToBattlefield(player1, new LordOfThePit());

            advanceToUpkeep(player1);
            harness.passBothPriorities();

            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("Lord of the Pit") && log.contains("7 damage"));
        }
    }

    // =========================================================================
    // DestroyTargetPermanentAndBoostSelfByManaValueEffect (via Hoard-Smelter Dragon — {3}{R} ability)
    // =========================================================================

    @Nested
    @DisplayName("resolveDestroyTargetArtifactAndBoostSelfByManaValue")
    class ResolveDestroyTargetArtifactAndBoostSelfByManaValue {

        @Test
        @DisplayName("Destroys target artifact and boosts self by its mana value")
        void destroysArtifactAndBoostsSelf() {
            HoardSmelterDragon dragonCard = new HoardSmelterDragon();
            harness.addToBattlefield(player1, dragonCard);
            Permanent dragon = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Hoard-Smelter Dragon"))
                    .findFirst().orElseThrow();
            dragon.setSummoningSick(false);

            // Spellbook has mana value 0, so use Leonin Scimitar (mana value 1)
            harness.addToBattlefield(player2, new LeoninScimitar());
            UUID scimitarId = harness.getPermanentId(player2, "Leonin Scimitar");

            harness.addMana(player1, ManaColor.RED, 4);

            harness.activateAbility(player1, 0, null, scimitarId);
            harness.passBothPriorities();

            harness.assertInGraveyard(player2, "Leonin Scimitar");

            // Dragon gets +1/+0 from Leonin Scimitar's mana value of 1
            assertThat(dragon.getPowerModifier()).isEqualTo(1);
        }

        @Test
        @DisplayName("Boost is zero when artifact has mana value 0")
        void noBoostForZeroManaValue() {
            HoardSmelterDragon dragonCard = new HoardSmelterDragon();
            harness.addToBattlefield(player1, dragonCard);
            Permanent dragon = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Hoard-Smelter Dragon"))
                    .findFirst().orElseThrow();
            dragon.setSummoningSick(false);

            // Spellbook has mana value 0
            harness.addToBattlefield(player2, new Spellbook());
            UUID spellbookId = harness.getPermanentId(player2, "Spellbook");

            harness.addMana(player1, ManaColor.RED, 4);

            harness.activateAbility(player1, 0, null, spellbookId);
            harness.passBothPriorities();

            harness.assertInGraveyard(player2, "Spellbook");
            assertThat(dragon.getPowerModifier()).isEqualTo(0);
        }

        @Test
        @DisplayName("Destruction and boost are logged")
        void destructionAndBoostAreLogged() {
            HoardSmelterDragon dragonCard = new HoardSmelterDragon();
            harness.addToBattlefield(player1, dragonCard);
            Permanent dragon = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Hoard-Smelter Dragon"))
                    .findFirst().orElseThrow();
            dragon.setSummoningSick(false);

            harness.addToBattlefield(player2, new LeoninScimitar());
            UUID scimitarId = harness.getPermanentId(player2, "Leonin Scimitar");

            harness.addMana(player1, ManaColor.RED, 4);

            harness.activateAbility(player1, 0, null, scimitarId);
            harness.passBothPriorities();

            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("Leonin Scimitar") && log.contains("destroyed"));
            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("Hoard-Smelter Dragon") && log.contains("+1/+0"));
        }
    }

    // =========================================================================
    // DestroyTargetPermanentAndGainLifeEqualToManaValueEffect (via Divine Offering — instant, {1}{W})
    // =========================================================================

    @Nested
    @DisplayName("resolveDestroyTargetPermanentAndGainLifeEqualToManaValue")
    class ResolveDestroyTargetPermanentAndGainLifeEqualToManaValue {

        @Test
        @DisplayName("Destroys artifact and gains life equal to its mana value")
        void destroysArtifactAndGainsLife() {
            // Leonin Scimitar has mana value 1
            harness.addToBattlefield(player2, new LeoninScimitar());

            harness.setHand(player1, List.of(new DivineOffering()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Leonin Scimitar"));

            harness.assertInGraveyard(player2, "Leonin Scimitar");
            harness.assertLife(player1, 21); // 20 + 1 (mana value of Leonin Scimitar)
        }

        @Test
        @DisplayName("Gains no life from zero mana value artifact")
        void gainsNoLifeFromZeroManaValue() {
            // Spellbook has mana value 0
            harness.addToBattlefield(player2, new Spellbook());

            harness.setHand(player1, List.of(new DivineOffering()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Spellbook"));

            harness.assertInGraveyard(player2, "Spellbook");
            harness.assertLife(player1, 20); // no life gain from 0 mana value
        }

        @Test
        @DisplayName("Fizzles when target is removed before resolution")
        void fizzlesWhenTargetRemoved() {
            harness.addToBattlefield(player2, new Spellbook());
            UUID spellbookId = harness.getPermanentId(player2, "Spellbook");

            harness.setHand(player1, List.of(new DivineOffering()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            harness.castInstant(player1, 0, spellbookId);

            // Remove target before resolution
            gd.playerBattlefields.get(player2.getId()).clear();

            harness.passBothPriorities();

            harness.assertLife(player1, 20);
            assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        }

        @Test
        @DisplayName("Life gain is logged")
        void lifeGainIsLogged() {
            harness.addToBattlefield(player2, new LeoninScimitar());

            harness.setHand(player1, List.of(new DivineOffering()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Leonin Scimitar"));

            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("gains") && log.contains("1 life"));
        }
    }

    // =========================================================================
    // DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect (via Flesh Allergy — sorcery, {2}{B}{B})
    // =========================================================================

    @Nested
    @DisplayName("resolveDestroyTargetAndControllerLosesLifePerCreatureDeaths")
    class ResolveDestroyTargetAndControllerLosesLifePerCreatureDeaths {

        @Test
        @DisplayName("Counts creature deaths including the sacrificed and destroyed creatures")
        void countsAllCreatureDeathsThisTurn() {
            Permanent sacrifice = new Permanent(new LlanowarElves());
            gd.playerBattlefields.get(player1.getId()).add(sacrifice);

            Permanent target = new Permanent(new GrizzlyBears());
            gd.playerBattlefields.get(player2.getId()).add(target);

            harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.f.FleshAllergy()));
            harness.addMana(player1, ManaColor.BLACK, 4);

            harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
            harness.passBothPriorities();

            harness.assertInGraveyard(player2, "Grizzly Bears");
            // 2 creature deaths: sacrificed + destroyed → 2 life loss
            harness.assertLife(player2, 18);
        }

        @Test
        @DisplayName("Includes earlier deaths from the same turn")
        void includesEarlierDeaths() {
            Permanent sacrifice = new Permanent(new LlanowarElves());
            gd.playerBattlefields.get(player1.getId()).add(sacrifice);

            Permanent target = new Permanent(new GrizzlyBears());
            gd.playerBattlefields.get(player2.getId()).add(target);

            // Simulate a creature that died earlier
            gd.creatureDeathCountThisTurn.merge(player1.getId(), 2, Integer::sum);

            harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.f.FleshAllergy()));
            harness.addMana(player1, ManaColor.BLACK, 4);

            harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
            harness.passBothPriorities();

            // 4 creature deaths: 2 earlier + sacrificed + destroyed → 4 life loss
            harness.assertLife(player2, 16);
        }
    }

    // =========================================================================
    // EachOpponentSacrificesCreatureEffect (via Grave Pact — enchantment, triggered on ally creature death)
    // =========================================================================

    @Nested
    @DisplayName("resolveEachOpponentSacrificesCreature")
    class ResolveEachOpponentSacrificesCreature {

        @Test
        @DisplayName("Opponent with one creature sacrifices it when your creature dies")
        void opponentSacrificesWhenYourCreatureDies() {
            harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.g.GravePact());
            Permanent myCreature = new Permanent(new LlanowarElves());
            gd.playerBattlefields.get(player1.getId()).add(myCreature);

            Permanent opponentCreature = new Permanent(new GrizzlyBears());
            gd.playerBattlefields.get(player2.getId()).add(opponentCreature);

            // Kill player1's creature to trigger Grave Pact
            harness.setHand(player2, List.of(new Terror()));
            harness.addMana(player2, ManaColor.BLACK, 2);
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.castInstant(player2, 0, myCreature.getId());
            harness.passBothPriorities(); // resolve Terror
            harness.passBothPriorities(); // resolve Grave Pact trigger

            harness.assertInGraveyard(player1, "Llanowar Elves");
            harness.assertInGraveyard(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("No effect when opponent has no creatures")
        void noEffectWhenOpponentHasNoCreatures() {
            harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.g.GravePact());
            Permanent myCreature = new Permanent(new LlanowarElves());
            gd.playerBattlefields.get(player1.getId()).add(myCreature);

            // Player2 has no creatures

            harness.setHand(player2, List.of(new Terror()));
            harness.addMana(player2, ManaColor.BLACK, 2);
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.castInstant(player2, 0, myCreature.getId());
            harness.passBothPriorities(); // resolve Terror
            harness.passBothPriorities(); // resolve Grave Pact trigger

            harness.assertInGraveyard(player1, "Llanowar Elves");
            assertThat(gd.gameLog).anyMatch(log -> log.contains("no creatures to sacrifice"));
        }
    }
}
