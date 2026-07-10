package com.github.laxika.magicalvibes.layers;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.b.BloodMoon;
import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.d.DauntlessDourbark;
import com.github.laxika.magicalvibes.cards.d.DeepFreeze;
import com.github.laxika.magicalvibes.cards.d.Diminish;
import com.github.laxika.magicalvibes.cards.d.Dub;
import com.github.laxika.magicalvibes.cards.e.ElvishChampion;
import com.github.laxika.magicalvibes.cards.e.EvilPresence;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.Glimmerpost;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GoblinKing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImperiousPerfect;
import com.github.laxika.magicalvibes.cards.i.InBolassClutches;
import com.github.laxika.magicalvibes.cards.i.Incite;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MagebaneArmor;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.cards.m.Maro;
import com.github.laxika.magicalvibes.cards.m.MerfolkTrickster;
import com.github.laxika.magicalvibes.cards.m.MindBend;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.Nightmare;
import com.github.laxika.magicalvibes.cards.n.NimDeathmantle;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.SeasClaim;
import com.github.laxika.magicalvibes.cards.s.SowerOfTemptation;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.Threaten;
import com.github.laxika.magicalvibes.cards.t.TurtleshellChangeling;
import com.github.laxika.magicalvibes.cards.t.TwistedImage;
import com.github.laxika.magicalvibes.cards.v.VoiceOfAll;
import com.github.laxika.magicalvibes.cards.w.WingsOfVelisVel;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Target specification for the CR 613 layer system ("seven layers") migration.
 *
 * <p>Each nested class covers one layer (or layer-7 sublayer) with scenarios whose outcome
 * depends on layer ordering, timestamps (CR 613.7), or dependency (CR 613.8) — behavior the
 * current single-pass accumulator in {@code computeStaticBonus} cannot generally express.
 * Many of these tests are EXPECTED TO BE RED until the layered refactor lands; they define
 * the rules-correct end state, not the current engine behavior.
 *
 * <p>Timestamp conventions used by the setups: a permanent's timestamp is the order in which
 * it was added to a battlefield; an Aura/Equipment attached directly gets the timestamp of
 * its own battlefield insertion; one-shot continuous effects (Giant Growth, Diminish, ...)
 * get the timestamp of their resolution. Setups always create sources in the order the
 * scenario's timestamps require.
 */
class SevenLayerTest extends BaseCardTest {

    // ===== shared setup helpers =====

    private Permanent addPermanent(Player player, Card card) {
        card.setOwnerId(player.getId());
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = addPermanent(player, card);
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent attach(Player controller, Card attachment, Permanent target) {
        attachment.setOwnerId(controller.getId());
        Permanent perm = new Permanent(attachment);
        perm.setAttachedTo(target.getId());
        gd.playerBattlefields.get(controller.getId()).add(perm);
        return perm;
    }

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private int power(Permanent perm) {
        return gqs.getEffectivePower(gd, perm);
    }

    private int toughness(Permanent perm) {
        return gqs.getEffectiveToughness(gd, perm);
    }

    private boolean hasKeyword(Permanent perm, Keyword keyword) {
        return gqs.hasKeyword(gd, perm, keyword);
    }

    private GameQueryService.StaticBonus bonus(Permanent perm) {
        return gqs.computeStaticBonus(gd, perm);
    }

    private boolean controls(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .anyMatch(p -> p.getId().equals(perm.getId()));
    }

    private void destroy(Permanent perm) {
        perm.setMarkedDamage(Math.max(1, gqs.getEffectiveToughness(gd, perm)));
        harness.runStateBasedActions();
    }

    private int tapLandAndCount(Player player, int landIndex, ManaColor color) {
        gs.tapPermanent(gd, player, landIndex);
        return gd.playerManaPools.get(player.getId()).get(color);
    }

    private List<Card> cards(int count) {
        return java.util.stream.Stream.generate(GrizzlyBears::new).limit(count).map(Card.class::cast).toList();
    }

    // ===== spell-casting helpers =====

    private void castAndResolveInstantOn(Player player, Card card, UUID targetId,
                                         ManaColor color, int colored, int generic) {
        harness.setHand(player, List.of(card));
        harness.addMana(player, color, colored);
        if (generic > 0) {
            harness.addMana(player, ManaColor.COLORLESS, generic);
        }
        harness.castAndResolveInstant(player, 0, targetId);
    }

    private void castGiantGrowth(Player player, Permanent target) {
        castAndResolveInstantOn(player, new GiantGrowth(), target.getId(), ManaColor.GREEN, 1, 0);
    }

    private void castDiminish(Player player, Permanent target) {
        castAndResolveInstantOn(player, new Diminish(), target.getId(), ManaColor.BLUE, 1, 0);
    }

    private void castTwistedImage(Player player, Permanent target) {
        castAndResolveInstantOn(player, new TwistedImage(), target.getId(), ManaColor.BLUE, 1, 0);
    }

    private void castWingsOfVelisVel(Player player, Permanent target) {
        castAndResolveInstantOn(player, new WingsOfVelisVel(), target.getId(), ManaColor.BLUE, 2, 0);
    }

    private void castThreaten(Player player, Permanent target) {
        harness.forceActivePlayer(player);
        harness.setHand(player, List.of(new Threaten()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.castSorcery(player, 0, target.getId());
        harness.passBothPriorities();
    }

    private void castIncite(Player player, Permanent target) {
        harness.forceActivePlayer(player);
        harness.setHand(player, List.of(new Incite()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.castSorcery(player, 0, target.getId());
        harness.passBothPriorities();
    }

    private void castMindBend(Player player, UUID targetId, String fromWord, String toWord) {
        harness.setHand(player, List.of(new MindBend()));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.castInstant(player, 0, targetId);
        harness.passBothPriorities();
        harness.handleListChoice(player, fromWord);
        harness.handleListChoice(player, toWord);
    }

    private Permanent resolveClone(Player player, UUID copyTargetId) {
        harness.setHand(player, List.of(new Clone()));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player, true);
        harness.handlePermanentChosen(player, copyTargetId);
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Clone"))
                .reduce((first, second) -> second)
                .orElseThrow();
    }

    private Permanent castSowerOfTemptation(Player player, Permanent target) {
        harness.forceActivePlayer(player);
        harness.setHand(player, List.of(new SowerOfTemptation()));
        harness.addMana(player, ManaColor.BLUE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.castCreature(player, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player, "Sower of Temptation");
    }

    private void castMerfolkTrickster(Player player, Permanent target) {
        harness.setHand(player, List.of(new MerfolkTrickster()));
        harness.addMana(player, ManaColor.BLUE, 2);
        harness.castCreature(player, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void castInBolassClutches(Player player, Permanent target) {
        harness.forceActivePlayer(player);
        harness.setHand(player, List.of(new InBolassClutches()));
        harness.addMana(player, ManaColor.BLUE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player, 0, target.getId());
        harness.passBothPriorities();
    }

    // =====================================================================================
    // Layer 1 — copy effects (CR 613.2a, 707.2: copiable values)
    // =====================================================================================

    @Nested
    @DisplayName("Layer 1: copy effects")
    class Layer1Copy {

        @Test
        @DisplayName("Copy uses printed values, not values modified by another permanent's static boost")
        void copyUsesPrintedValuesNotStaticBoosts() {
            Permanent bears = addReady(player2, new GrizzlyBears());
            addPermanent(player2, new GloriousAnthem());
            assertThat(power(bears)).isEqualTo(3); // sanity: anthem applies to the original

            Permanent clone = resolveClone(player1, bears.getId());

            // CR 707.2: the anthem is not part of the copiable values, and player1 has no anthem.
            assertThat(power(clone)).isEqualTo(2);
            assertThat(toughness(clone)).isEqualTo(2);
        }

        @Test
        @DisplayName("Copy does not include +1/+1 counters on the copied creature")
        void copyIgnoresCounters() {
            Permanent bears = addReady(player2, new GrizzlyBears());
            bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

            Permanent clone = resolveClone(player1, bears.getId());

            assertThat(power(clone)).isEqualTo(2);
            assertThat(toughness(clone)).isEqualTo(2);
        }

        @Test
        @DisplayName("Copy does not include a resolved one-shot pump (Giant Growth)")
        void copyIgnoresOneShotPump() {
            Permanent bears = addReady(player2, new GrizzlyBears());
            castGiantGrowth(player2, bears);
            assertThat(power(bears)).isEqualTo(5); // sanity

            Permanent clone = resolveClone(player1, bears.getId());

            assertThat(power(clone)).isEqualTo(2);
            assertThat(toughness(clone)).isEqualTo(2);
        }

        @Test
        @DisplayName("Copy does not include Aura-granted P/T, keywords, or subtypes (Dub)")
        void copyIgnoresAuraGrants() {
            Permanent bears = addReady(player2, new GrizzlyBears());
            attach(player2, new Dub(), bears);
            assertThat(power(bears)).isEqualTo(4); // sanity

            Permanent clone = resolveClone(player1, bears.getId());

            assertThat(power(clone)).isEqualTo(2);
            assertThat(hasKeyword(clone, Keyword.FIRST_STRIKE)).isFalse();
            assertThat(clone.getCard().getSubtypes()).doesNotContain(CardSubtype.KNIGHT);
            assertThat(bonus(clone).grantedSubtypes()).doesNotContain(CardSubtype.KNIGHT);
        }

        @Test
        @DisplayName("Copy of a Lignified creature is the unmodified original, not a 0/4 Treefolk")
        void copyIgnoresLignifyOverride() {
            Permanent elemental = addReady(player2, new AirElemental());
            attach(player2, new Lignify(), elemental);
            assertThat(power(elemental)).isEqualTo(0); // sanity

            Permanent clone = resolveClone(player1, elemental.getId());

            assertThat(power(clone)).isEqualTo(4);
            assertThat(toughness(clone)).isEqualTo(4);
            assertThat(hasKeyword(clone, Keyword.FLYING)).isTrue();
            assertThat(clone.getCard().getSubtypes()).doesNotContain(CardSubtype.TREEFOLK);
        }

        @Test
        @DisplayName("Copy does not include a P/T switch on the copied creature")
        void copyIgnoresSwitch() {
            Permanent merfolk = addReady(player2, new CoralMerfolk());
            castTwistedImage(player2, merfolk);
            assertThat(power(merfolk)).isEqualTo(1); // sanity: 2/1 switched to 1/2

            Permanent clone = resolveClone(player1, merfolk.getId());

            assertThat(power(clone)).isEqualTo(2);
            assertThat(toughness(clone)).isEqualTo(1);
        }

        @Test
        @DisplayName("Copy does not include text changes (CR 707.2)")
        void copyIgnoresTextChanges() {
            Permanent paladin = addReady(player1, new PaladinEnVec());
            addPermanent(player2, new Swamp());
            Permanent nightmare = addReady(player2, new Nightmare()); // black probe source
            castMindBend(player1, paladin.getId(), "BLACK", "GREEN");

            Permanent clone = resolveClone(player1, paladin.getId());

            // The clone has the original printed text: protection from black (and red).
            assertThat(gqs.hasProtectionFromSource(gd, clone, nightmare)).isTrue();
        }

        @Test
        @DisplayName("A copied characteristic-defining ability evaluates in the copy's own context")
        void copiedCdaEvaluatesInNewContext() {
            Permanent maro = addReady(player2, new Maro());

            // Keep an extra card in hand so the entering copy is not a 0/0 killed by SBAs.
            harness.setHand(player1, List.of(new Clone(), new GrizzlyBears()));
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 3);
            harness.castCreature(player1, 0);
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.handlePermanentChosen(player1, maro.getId());
            Permanent clone = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getOriginalCard().getName().equals("Clone"))
                    .findFirst().orElseThrow();

            harness.setHand(player1, cards(1));
            harness.setHand(player2, cards(4));

            assertThat(power(maro)).isEqualTo(4);
            assertThat(power(clone)).isEqualTo(1);
        }

        @Test
        @DisplayName("Copy of a copy uses the first copy's copiable values")
        void copyOfCopyUsesCopiableValues() {
            Permanent bears = addReady(player2, new GrizzlyBears());
            Permanent firstClone = resolveClone(player1, bears.getId());

            Permanent secondClone = resolveClone(player1, firstClone.getId());

            assertThat(secondClone.getCard().getName()).isEqualTo("Grizzly Bears");
            assertThat(power(secondClone)).isEqualTo(2);
            assertThat(toughness(secondClone)).isEqualTo(2);
        }

        @Test
        @DisplayName("After copying, the copy is subject to static effects in later layers")
        void copyIsSubjectToLaterLayersAfterCopying() {
            addReady(player1, new ElvishChampion());
            Permanent elves = addReady(player2, new LlanowarElves());

            Permanent clone = resolveClone(player1, elves.getId());

            // The clone is a Llanowar Elves, so the (all-Elves) Champion boost applies to it.
            assertThat(power(clone)).isEqualTo(2);
            assertThat(hasKeyword(clone, Keyword.FORESTWALK)).isTrue();
        }
    }

    // =====================================================================================
    // Layer 2 — control-changing effects (CR 613.2b, timestamps CR 613.7)
    // =====================================================================================

    @Nested
    @DisplayName("Layer 2: control-changing effects")
    class Layer2Control {

        @Test
        @DisplayName("A later control effect (Threaten) overrides an earlier one (Sower of Temptation)")
        void laterControlEffectWins() {
            Permanent bears = addReady(player2, new GrizzlyBears());
            castSowerOfTemptation(player1, bears);
            assertThat(controls(player1, bears)).isTrue(); // sanity

            castThreaten(player2, bears);

            assertThat(controls(player2, bears)).isTrue();
        }

        @Test
        @DisplayName("When the later control effect expires, control falls to the still-active earlier effect")
        void expiredControlEffectFallsBackToEarlierEffect() {
            Permanent bears = addReady(player2, new GrizzlyBears());
            Permanent sower = castSowerOfTemptation(player1, bears);
            castThreaten(player2, bears);

            endTurn();

            // Threaten's duration ended, Sower's effect is still active: player1 controls the
            // bear again — control does NOT revert to the owner while Sower remains.
            assertThat(controls(player1, sower)).isTrue(); // sanity: sower survived
            assertThat(controls(player1, bears)).isTrue();
            assertThat(controls(player2, bears)).isFalse();
        }

        @Test
        @DisplayName("Removing the earlier control effect leaves the later one in charge until it expires")
        void removedControlEffectLeavesLaterOneActive() {
            Permanent bears = addReady(player2, new GrizzlyBears());
            Permanent sower = castSowerOfTemptation(player1, bears);
            castThreaten(player1, bears); // player1 stacks a second, later control effect

            destroy(sower);

            // Sower's effect ended, but player1's Threaten is still active this turn.
            assertThat(controls(player1, bears)).isTrue();

            endTurn();

            // Now both effects are gone: control reverts to the owner.
            assertThat(controls(player2, bears)).isTrue();
        }

        @Test
        @DisplayName("Of two temporary steal effects, the later timestamp wins")
        void latestOfTwoTemporaryStealsWins() {
            Permanent bears = addReady(player2, new GrizzlyBears());
            castThreaten(player1, bears);
            assertThat(controls(player1, bears)).isTrue(); // sanity

            castThreaten(player2, bears);

            assertThat(controls(player2, bears)).isTrue();
        }

        @Test
        @DisplayName("Control change does not change ownership: stolen creature dies to owner's graveyard")
        void controlChangeDoesNotChangeOwnership() {
            Permanent bears = addReady(player2, new GrizzlyBears());
            castThreaten(player1, bears);

            destroy(bears);

            harness.assertInGraveyard(player2, "Grizzly Bears");
            harness.assertNotInGraveyard(player1, "Grizzly Bears");
        }

        @Test
        @DisplayName("'Creatures you control' statics apply to a creature stolen afterwards")
        void anthemAppliesToStolenCreature() {
            addPermanent(player1, new GloriousAnthem());
            Permanent bears = addReady(player2, new GrizzlyBears());
            assertThat(power(bears)).isEqualTo(2); // sanity

            castThreaten(player1, bears);

            assertThat(power(bears)).isEqualTo(3);
        }

        @Test
        @DisplayName("Controller-scoped lord boost stops applying once the creature is stolen")
        void controllerScopedLordStopsApplyingWhenStolen() {
            addReady(player2, new ImperiousPerfect());
            Permanent elves = addReady(player2, new LlanowarElves());
            assertThat(power(elves)).isEqualTo(2); // sanity: "other Elves you control get +1/+1"

            castThreaten(player1, elves);

            assertThat(power(elves)).isEqualTo(1);
        }

        @Test
        @DisplayName("Aura-based control (In Bolas's Clutches) is continuous and survives end of turn")
        void auraControlIsContinuous() {
            Permanent bears = addReady(player2, new GrizzlyBears());
            castInBolassClutches(player1, bears);
            assertThat(controls(player1, bears)).isTrue();

            endTurn();

            assertThat(controls(player1, bears)).isTrue();
        }

        @Test
        @DisplayName("Temporary steal overrides Aura control until cleanup, then reverts to the Aura")
        void temporaryStealOverridesAuraControlUntilCleanup() {
            Permanent bears = addReady(player2, new GrizzlyBears());
            castInBolassClutches(player1, bears);

            castThreaten(player2, bears);
            assertThat(controls(player2, bears)).isTrue();

            endTurn();

            // Threaten ended; the Clutches control effect is still active → player1, not owner.
            assertThat(controls(player1, bears)).isTrue();
        }

        @Test
        @DisplayName("An attached Aura keeps applying to a creature after it changes controller")
        void attachedAuraKeepsApplyingAfterControlChange() {
            Permanent bears = addReady(player2, new GrizzlyBears());
            attach(player2, new Dub(), bears);

            castThreaten(player1, bears);

            assertThat(controls(player1, bears)).isTrue();
            assertThat(power(bears)).isEqualTo(4);
            assertThat(hasKeyword(bears, Keyword.FIRST_STRIKE)).isTrue();
        }
    }

    // =====================================================================================
    // Layer 3 — text-changing effects (CR 613.2c, 612)
    // =====================================================================================

    @Nested
    @DisplayName("Layer 3: text-changing effects")
    class Layer3Text {

        @Test
        @DisplayName("Changing a protection color word changes what the creature is protected from")
        void textChangeRewritesProtectionColor() {
            Permanent paladin = addReady(player1, new PaladinEnVec()); // pro black, pro red
            addPermanent(player2, new Swamp());
            Permanent nightmare = addReady(player2, new Nightmare()); // black source
            Permanent elemental = addReady(player2, new AirElemental()); // blue source

            castMindBend(player1, paladin.getId(), "BLACK", "BLUE");

            assertThat(gqs.hasProtectionFromSource(gd, paladin, elemental)).isTrue();
            assertThat(gqs.hasProtectionFromSource(gd, paladin, nightmare)).isFalse();
        }

        @Test
        @DisplayName("Changing a landwalk word on a lord changes the walk ability it grants")
        void textChangeRewritesGrantedLandwalk() {
            Permanent king = addReady(player1, new GoblinKing());
            Permanent goblin = addReady(player1, new RagingGoblin());
            assertThat(hasKeyword(goblin, Keyword.MOUNTAINWALK)).isTrue(); // sanity

            castMindBend(player1, king.getId(), "MOUNTAIN", "ISLAND");

            assertThat(hasKeyword(goblin, Keyword.ISLANDWALK)).isTrue();
            assertThat(hasKeyword(goblin, Keyword.MOUNTAINWALK)).isFalse();
        }

        @Test
        @DisplayName("Changing the land type word on an Aura changes the type it grants in layer 4")
        void textChangeOnAuraChangesGrantedLandType() {
            Permanent forest = addPermanent(player1, new Forest());
            attach(player1, new SeasClaim(), forest);
            castMindBend(player1, gd.playerBattlefields.get(player1.getId()).get(1).getId(),
                    "ISLAND", "MOUNTAIN");

            int red = tapLandAndCount(player1, 0, ManaColor.RED);

            assertThat(red).isEqualTo(1);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
        }

        @Test
        @DisplayName("Changing Mountain to Island on Blood Moon makes nonbasics tap for blue")
        void textChangeOnBloodMoon() {
            Permanent glimmerpost = addPermanent(player1, new Glimmerpost());
            Permanent moon = addPermanent(player1, new BloodMoon());
            castMindBend(player1, moon.getId(), "MOUNTAIN", "ISLAND");

            int blue = tapLandAndCount(player1, 0, ManaColor.BLUE);

            assertThat(glimmerpost.isTapped()).isTrue();
            assertThat(blue).isEqualTo(1);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
        }

        @Test
        @DisplayName("A text change does not change the object's actual color")
        void textChangeDoesNotChangeColor() {
            Permanent bears = addReady(player1, new GrizzlyBears());

            castMindBend(player1, bears.getId(), "GREEN", "WHITE");

            assertThat(bears.getEffectiveColor()).isEqualTo(CardColor.GREEN);
        }

        @Test
        @DisplayName("Text changes are not part of the copiable values")
        void textChangeIsNotCopiable() {
            Permanent paladin = addReady(player1, new PaladinEnVec());
            addPermanent(player2, new Swamp());
            Permanent nightmare = addReady(player2, new Nightmare());
            castMindBend(player1, paladin.getId(), "BLACK", "GREEN");

            Permanent clone = resolveClone(player1, paladin.getId());

            assertThat(gqs.hasProtectionFromSource(gd, clone, nightmare)).isTrue();
        }

        @Test
        @DisplayName("A text change with no duration persists across turns")
        void textChangePersistsAcrossTurns() {
            Permanent king = addReady(player1, new GoblinKing());
            Permanent goblin = addReady(player1, new RagingGoblin());
            castMindBend(player1, king.getId(), "MOUNTAIN", "ISLAND");

            endTurn();

            assertThat(king.getTextReplacements()).hasSize(1);
            assertThat(hasKeyword(goblin, Keyword.ISLANDWALK)).isTrue();
        }

        @Test
        @DisplayName("Changing Swamp to Forest on Evil Presence makes the enchanted land tap for green")
        void textChangeOnEvilPresence() {
            Permanent mountain = addPermanent(player1, new Mountain());
            attach(player1, new EvilPresence(), mountain);
            castMindBend(player1, gd.playerBattlefields.get(player1.getId()).get(1).getId(),
                    "SWAMP", "FOREST");

            int green = tapLandAndCount(player1, 0, ManaColor.GREEN);

            assertThat(green).isEqualTo(1);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
        }

        @Test
        @DisplayName("Sequential text changes compose in timestamp order")
        void sequentialTextChangesCompose() {
            Permanent king = addReady(player1, new GoblinKing());
            Permanent goblin = addReady(player1, new RagingGoblin());

            castMindBend(player1, king.getId(), "MOUNTAIN", "ISLAND");
            castMindBend(player1, king.getId(), "ISLAND", "FOREST");

            assertThat(hasKeyword(goblin, Keyword.FORESTWALK)).isTrue();
            assertThat(hasKeyword(goblin, Keyword.ISLANDWALK)).isFalse();
            assertThat(hasKeyword(goblin, Keyword.MOUNTAINWALK)).isFalse();
        }

        @Test
        @DisplayName("A text change updates a matching color chosen as the permanent entered")
        void textChangeUpdatesChosenColor() {
            Permanent voice = addReady(player2, new VoiceOfAll());
            voice.setChosenColor(CardColor.BLACK);

            castMindBend(player1, voice.getId(), "BLACK", "RED");

            assertThat(voice.getChosenColor()).isEqualTo(CardColor.RED);
        }
    }

    // =====================================================================================
    // Layer 4 — type-changing effects (CR 613.2d, timestamps CR 613.7, dependency CR 613.8)
    // =====================================================================================

    @Nested
    @DisplayName("Layer 4: type-changing effects")
    class Layer4Type {

        @Test
        @DisplayName("Of two land-type-setting Auras, the later timestamp wins (Sea's Claim then Evil Presence)")
        void laterLandTypeOverrideWins() {
            Permanent forest = addPermanent(player1, new Forest());
            attach(player1, new SeasClaim(), forest);
            attach(player1, new EvilPresence(), forest);

            int black = tapLandAndCount(player1, 0, ManaColor.BLACK);

            assertThat(black).isEqualTo(1);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
        }

        @Test
        @DisplayName("Of two land-type-setting Auras, the later timestamp wins (Evil Presence then Sea's Claim)")
        void laterLandTypeOverrideWinsReversedOrder() {
            Permanent forest = addPermanent(player1, new Forest());
            attach(player1, new EvilPresence(), forest);
            attach(player1, new SeasClaim(), forest);

            int blue = tapLandAndCount(player1, 0, ManaColor.BLUE);

            assertThat(blue).isEqualTo(1);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
        }

        @Test
        @DisplayName("Blood Moon entering after Sea's Claim wins by timestamp")
        void bloodMoonAfterAuraWins() {
            Permanent glimmerpost = addPermanent(player1, new Glimmerpost());
            attach(player1, new SeasClaim(), glimmerpost);
            addPermanent(player1, new BloodMoon());

            int red = tapLandAndCount(player1, 0, ManaColor.RED);

            assertThat(red).isEqualTo(1);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
        }

        @Test
        @DisplayName("Sea's Claim attached after Blood Moon wins by timestamp")
        void auraAfterBloodMoonWins() {
            addPermanent(player1, new BloodMoon());
            Permanent glimmerpost = addPermanent(player1, new Glimmerpost());
            attach(player1, new SeasClaim(), glimmerpost);

            int blue = tapLandAndCount(player1, 1, ManaColor.BLUE);

            assertThat(blue).isEqualTo(1);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
        }

        @Test
        @DisplayName("A creature-type override in layer 4 removes lord boosts that keyed off the old type")
        void subtypeOverrideRemovesLordBoost() {
            addReady(player1, new ElvishChampion());
            Permanent elves = addReady(player1, new LlanowarElves());
            assertThat(power(elves)).isEqualTo(2); // sanity: Elf lord applies

            attach(player2, new Lignify(), elves);

            // Lignify: the creature is a Treefolk (replacing its types) with base 0/4 — the
            // Elf-scoped boost and forestwalk no longer apply.
            assertThat(power(elves)).isEqualTo(0);
            assertThat(toughness(elves)).isEqualTo(4);
            assertThat(hasKeyword(elves, Keyword.FORESTWALK)).isFalse();
        }

        @Test
        @DisplayName("An additive subtype grant keeps the creature's existing types (Dub)")
        void additiveSubtypeGrantKeepsExistingTypes() {
            Permanent bears = addReady(player1, new GrizzlyBears());

            attach(player1, new Dub(), bears);

            assertThat(bonus(bears).grantedSubtypes()).contains(CardSubtype.KNIGHT);
            assertThat(bonus(bears).subtypeOverriding()).isFalse();
        }

        @Test
        @DisplayName("A layer 4 chosen-subtype grant feeds later-layer lord effects")
        void chosenSubtypeGrantFeedsLaterLayers() {
            Permanent xenograft = addPermanent(player1, new Xenograft());
            xenograft.setChosenSubtype(CardSubtype.GOBLIN);
            addReady(player1, new GoblinKing());
            Permanent bears = addReady(player1, new GrizzlyBears());

            // The bear is a Goblin in layer 4, so Goblin King's layer 6/7c effects apply.
            assertThat(power(bears)).isEqualTo(3);
            assertThat(hasKeyword(bears, Keyword.MOUNTAINWALK)).isTrue();
        }

        @Test
        @DisplayName("March of the Machines animates a noncreature artifact in layer 4 with MV P/T")
        void marchAnimatesArtifact() {
            addPermanent(player1, new MarchOfTheMachines());
            Permanent deathmantle = addPermanent(player1, new NimDeathmantle());

            assertThat(gqs.isCreature(gd, deathmantle)).isTrue();
            // Nim Deathmantle costs {2} (Scryfall oracle), so the animation's base P/T is 2/2.
            assertThat(power(deathmantle)).isEqualTo(2);
            assertThat(toughness(deathmantle)).isEqualTo(2);
        }

        @Test
        @DisplayName("A zero-mana-value artifact animated by March of the Machines dies to SBAs")
        void animatedZeroCostArtifactDiesToSba() {
            addPermanent(player1, new MarchOfTheMachines());
            addPermanent(player1, new FountainOfYouth());

            harness.runStateBasedActions();

            harness.assertInGraveyard(player1, "Fountain of Youth");
            harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        }

        @Test
        @DisplayName("A later 7b setter beats the animation's MV-based P/T on an animated artifact")
        void laterSetterBeatsAnimationBasePT() {
            addPermanent(player1, new MarchOfTheMachines());
            Permanent deathmantle = addPermanent(player1, new NimDeathmantle());
            attach(player1, new Lignify(), deathmantle);

            assertThat(power(deathmantle)).isEqualTo(0);
            assertThat(toughness(deathmantle)).isEqualTo(4);
            assertThat(gqs.isArtifact(gd, deathmantle)).isTrue();
            assertThat(gqs.isCreature(gd, deathmantle)).isTrue();
        }
    }

    // =====================================================================================
    // Layer 5 — color-changing effects (CR 613.2e, timestamps CR 613.7)
    // =====================================================================================

    @Nested
    @DisplayName("Layer 5: color-changing effects")
    class Layer5Color {

        @Test
        @DisplayName("'Becomes red' overrides the creature's natural color")
        void becomesRedOverridesNaturalColor() {
            Permanent paladin = addReady(player2, new PaladinEnVec()); // pro black, pro red
            Permanent bears = addReady(player1, new GrizzlyBears());
            assertThat(gqs.hasProtectionFromSource(gd, paladin, bears)).isFalse(); // green source

            castIncite(player1, bears);

            assertThat(gqs.hasProtectionFromSource(gd, paladin, bears)).isTrue();
        }

        @Test
        @DisplayName("Of two color setters, the later timestamp wins (Deep Freeze then Incite)")
        void laterColorSetterWins() {
            Permanent paladin = addReady(player2, new PaladinEnVec());
            Permanent bears = addReady(player1, new GrizzlyBears());
            attach(player1, new DeepFreeze(), bears); // blue

            castIncite(player1, bears); // red, later timestamp

            assertThat(gqs.hasProtectionFromSource(gd, paladin, bears)).isTrue();
        }

        @Test
        @DisplayName("When the later color setter expires, the earlier one applies again")
        void expiredColorSetterRevertsToEarlier() {
            Permanent paladin = addReady(player2, new PaladinEnVec());
            Permanent bears = addReady(player1, new GrizzlyBears());
            attach(player1, new DeepFreeze(), bears);
            castIncite(player1, bears);

            endTurn();

            // Incite's "becomes red" ended; Deep Freeze's blue applies again.
            assertThat(gqs.hasProtectionFromSource(gd, paladin, bears)).isFalse();
            assertThat(bonus(bears).grantedColors()).contains(CardColor.BLUE);
        }

        @Test
        @DisplayName("Of two color-setting attachments, the later attach timestamp wins")
        void laterAttachmentColorOverridesEarlier() {
            Permanent paladin = addReady(player2, new PaladinEnVec());
            Permanent bears = addReady(player1, new GrizzlyBears());
            attach(player1, new DeepFreeze(), bears); // blue
            attach(player1, new NimDeathmantle(), bears); // black, later

            assertThat(gqs.hasProtectionFromSource(gd, paladin, bears)).isTrue(); // black
            assertThat(bonus(bears).grantedColors()).containsExactly(CardColor.BLACK);
        }

        @Test
        @DisplayName("A color setter replaces the natural color rather than adding to it")
        void colorSetterReplacesNaturalColor() {
            Permanent bears = addReady(player1, new GrizzlyBears());

            attach(player1, new DeepFreeze(), bears);

            assertThat(bonus(bears).grantedColors()).containsExactly(CardColor.BLUE);
            assertThat(bonus(bears).colorOverriding()).isTrue();
        }

        @Test
        @DisplayName("Color changes are not part of the copiable values")
        void colorChangeIsNotCopiable() {
            Permanent paladin = addReady(player2, new PaladinEnVec());
            Permanent bears = addReady(player2, new GrizzlyBears());
            castIncite(player1, bears);
            assertThat(gqs.hasProtectionFromSource(gd, paladin, bears)).isTrue(); // sanity

            Permanent clone = resolveClone(player1, bears.getId());

            assertThat(gqs.hasProtectionFromSource(gd, paladin, clone)).isFalse(); // green copy
        }

        @Test
        @DisplayName("An artifact animated in layer 4 can be given a color in layer 5")
        void animatedArtifactCanBeGivenColor() {
            addPermanent(player1, new MarchOfTheMachines());
            Permanent deathmantle = addPermanent(player1, new NimDeathmantle());

            attach(player1, new DeepFreeze(), deathmantle);

            assertThat(gqs.isCreature(gd, deathmantle)).isTrue();
            assertThat(bonus(deathmantle).grantedColors()).contains(CardColor.BLUE);
        }

        @Test
        @DisplayName("Color setters apply to tokens like any other permanent")
        void colorSetterAppliesToToken() {
            Permanent paladin = addReady(player2, new PaladinEnVec());
            Permanent perfect = addReady(player1, new ImperiousPerfect());
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(perfect), null, null);
            harness.passBothPriorities();
            Permanent token = findPermanent(player1, "Elf Warrior");

            castIncite(player1, token);

            assertThat(gqs.hasProtectionFromSource(gd, paladin, token)).isTrue();
        }

        @Test
        @DisplayName("'Becomes red until end of turn' expires at cleanup")
        void temporaryColorSetterExpires() {
            Permanent paladin = addReady(player2, new PaladinEnVec());
            Permanent bears = addReady(player1, new GrizzlyBears());
            castIncite(player1, bears);

            endTurn();

            assertThat(gqs.hasProtectionFromSource(gd, paladin, bears)).isFalse();
            assertThat(bears.getEffectiveColor()).isEqualTo(CardColor.GREEN);
        }

        @Test
        @DisplayName("A color change does not disturb subtype-keyed effects in other layers")
        void colorChangeDoesNotAffectOtherLayers() {
            Permanent paladin = addReady(player2, new PaladinEnVec());
            addReady(player1, new ElvishChampion());
            Permanent elves = addReady(player1, new LlanowarElves());

            castIncite(player1, elves);

            // Still an Elf: the lord boost (layer 7c) and forestwalk (layer 6) survive the
            // layer 5 color change, which happens "before" them in layer order.
            assertThat(power(elves)).isEqualTo(2);
            assertThat(hasKeyword(elves, Keyword.FORESTWALK)).isTrue();
            assertThat(gqs.hasProtectionFromSource(gd, paladin, elves)).isTrue(); // and it is red
        }
    }

    // =====================================================================================
    // Layer 6 — ability-adding/removing effects (CR 613.2f, timestamps CR 613.7)
    // =====================================================================================

    @Nested
    @DisplayName("Layer 6: ability adding and removing")
    class Layer6Abilities {

        @Test
        @DisplayName("A keyword granted after 'loses all abilities' applies (later timestamp)")
        void keywordGrantAfterLoseAllApplies() {
            Permanent elemental = addReady(player2, new AirElemental());
            attach(player1, new DeepFreeze(), elemental);
            assertThat(hasKeyword(elemental, Keyword.FLYING)).isFalse(); // sanity

            castWingsOfVelisVel(player2, elemental);

            // Wings of Velis Vel has a later timestamp than Deep Freeze: the flying grant
            // is applied after the ability removal and therefore sticks (Humility ordering).
            assertThat(hasKeyword(elemental, Keyword.FLYING)).isTrue();
        }

        @Test
        @DisplayName("A keyword granted before 'loses all abilities' is removed (earlier timestamp)")
        void keywordGrantBeforeLoseAllIsRemoved() {
            Permanent bears = addReady(player1, new GrizzlyBears());
            attach(player1, new Dub(), bears);
            attach(player2, new DeepFreeze(), bears);

            assertThat(hasKeyword(bears, Keyword.FIRST_STRIKE)).isFalse();
        }

        @Test
        @DisplayName("An Aura granting a keyword after a lose-all Aura applies (later attach timestamp)")
        void auraGrantAfterLoseAllAuraApplies() {
            Permanent bears = addReady(player1, new GrizzlyBears());
            attach(player2, new DeepFreeze(), bears);
            attach(player1, new Dub(), bears);

            assertThat(hasKeyword(bears, Keyword.FIRST_STRIKE)).isTrue();
        }

        @Test
        @DisplayName("A keyword granted after a one-shot 'loses all abilities until end of turn' applies")
        void keywordGrantAfterOneShotLoseAllApplies() {
            Permanent elemental = addReady(player2, new AirElemental());
            castMerfolkTrickster(player1, elemental);
            assertThat(hasKeyword(elemental, Keyword.FLYING)).isFalse(); // sanity

            castWingsOfVelisVel(player2, elemental);

            assertThat(hasKeyword(elemental, Keyword.FLYING)).isTrue();
        }

        @Test
        @DisplayName("Losing all abilities removes a P/T-defining CDA: the creature becomes 0/0 and dies")
        void loseAllRemovesPTDefiningCda() {
            harness.setHand(player1, cards(3));
            Permanent maro = addReady(player1, new Maro());
            assertThat(power(maro)).isEqualTo(3); // sanity

            castMerfolkTrickster(player2, maro);
            harness.runStateBasedActions();

            harness.assertInGraveyard(player1, "Maro");
        }

        @Test
        @DisplayName("A lord's keyword grant from before the lose-all effect is removed")
        void lordGrantBeforeLoseAllIsRemoved() {
            addReady(player1, new GoblinKing());
            Permanent goblin = addReady(player1, new RagingGoblin());
            attach(player2, new DeepFreeze(), goblin);

            assertThat(hasKeyword(goblin, Keyword.MOUNTAINWALK)).isFalse();
            // The king's +1/+1 (layer 7c) still applies on top of Deep Freeze's 0/4 (layer 7b).
            assertThat(power(goblin)).isEqualTo(1);
            assertThat(toughness(goblin)).isEqualTo(5);
        }

        @Test
        @DisplayName("A lord entering after the lose-all effect grants its keyword (later timestamp)")
        void lordGrantAfterLoseAllApplies() {
            Permanent goblin = addReady(player1, new RagingGoblin());
            attach(player2, new DeepFreeze(), goblin);
            addReady(player1, new GoblinKing());

            assertThat(hasKeyword(goblin, Keyword.MOUNTAINWALK)).isTrue();
            assertThat(power(goblin)).isEqualTo(1);
            assertThat(toughness(goblin)).isEqualTo(5);
        }

        @Test
        @DisplayName("A keyword granted after a keyword-removal effect applies (later timestamp)")
        void grantAfterRemovalApplies() {
            Permanent elemental = addReady(player1, new AirElemental());
            attach(player1, new MagebaneArmor(), elemental); // removes flying
            assertThat(hasKeyword(elemental, Keyword.FLYING)).isFalse(); // sanity

            castWingsOfVelisVel(player1, elemental);

            assertThat(hasKeyword(elemental, Keyword.FLYING)).isTrue();
        }

        @Test
        @DisplayName("A keyword-removal effect with the later timestamp removes an earlier grant")
        void removalAfterGrantWins() {
            Permanent bears = addReady(player1, new GrizzlyBears());
            castWingsOfVelisVel(player1, bears); // grants flying
            attach(player1, new MagebaneArmor(), bears); // later: loses flying

            assertThat(hasKeyword(bears, Keyword.FLYING)).isFalse();
        }

        @Test
        @DisplayName("One-shot lose-all expires at cleanup and the printed abilities return")
        void oneShotLoseAllExpires() {
            Permanent elemental = addReady(player2, new AirElemental());
            castMerfolkTrickster(player1, elemental);
            assertThat(hasKeyword(elemental, Keyword.FLYING)).isFalse();

            endTurn();

            assertThat(hasKeyword(elemental, Keyword.FLYING)).isTrue();
        }
    }

    // =====================================================================================
    // Layer 7a — characteristic-defining P/T (CR 613.4a)
    // =====================================================================================

    @Nested
    @DisplayName("Layer 7a: characteristic-defining P/T")
    class Layer7aCda {

        @Test
        @DisplayName("A */* CDA tracks its controller's hand size continuously")
        void cdaTracksControllerHandSize() {
            Permanent maro = addReady(player1, new Maro());

            harness.setHand(player1, cards(3));
            assertThat(power(maro)).isEqualTo(3);

            harness.setHand(player1, cards(5));
            assertThat(power(maro)).isEqualTo(5);
        }

        @Test
        @DisplayName("A one-shot pump (7c) applies on top of the CDA value (7a)")
        void cdaPlusOneShotPump() {
            Permanent maro = addReady(player1, new Maro());
            castGiantGrowth(player1, maro);
            harness.setHand(player1, cards(3));

            assertThat(power(maro)).isEqualTo(6);
            assertThat(toughness(maro)).isEqualTo(6);
        }

        @Test
        @DisplayName("A 7b setter overrides the CDA regardless of the CDA changing afterwards")
        void basePTSetterOverridesCda() {
            Permanent maro = addReady(player1, new Maro());
            castDiminish(player1, maro);
            harness.setHand(player1, cards(5));

            // 7b applies after 7a in layer order: Maro is 1/1 no matter the hand size.
            assertThat(power(maro)).isEqualTo(1);
            assertThat(toughness(maro)).isEqualTo(1);
        }

        @Test
        @DisplayName("A static boost (7c) applies on top of the CDA value (7a)")
        void cdaPlusStaticBoost() {
            addPermanent(player1, new Swamp());
            addPermanent(player1, new Swamp());
            addPermanent(player1, new Swamp());
            Permanent nightmare = addReady(player1, new Nightmare());
            addPermanent(player1, new GloriousAnthem());

            assertThat(power(nightmare)).isEqualTo(4);
            assertThat(toughness(nightmare)).isEqualTo(4);
        }

        @Test
        @DisplayName("The CDA recomputes when the counted game state changes")
        void cdaRecomputesWhenStateChanges() {
            addPermanent(player1, new Swamp());
            addPermanent(player1, new Swamp());
            Permanent thirdSwamp = addPermanent(player1, new Swamp());
            Permanent nightmare = addReady(player1, new Nightmare());
            assertThat(power(nightmare)).isEqualTo(3);

            gd.playerBattlefields.get(player1.getId()).remove(thirdSwamp);

            assertThat(power(nightmare)).isEqualTo(2);
        }

        @Test
        @DisplayName("A CDA creature at 0 toughness dies to state-based actions")
        void cdaZeroTriggersSba() {
            Permanent maro = addReady(player1, new Maro());
            harness.setHand(player1, List.of());
            assertThat(toughness(maro)).isEqualTo(0);

            harness.runStateBasedActions();

            harness.assertInGraveyard(player1, "Maro");
        }

        @Test
        @DisplayName("A copied CDA counts the copy controller's resources")
        void copiedCdaCountsNewControllersResources() {
            addPermanent(player2, new Swamp());
            addPermanent(player2, new Swamp());
            addPermanent(player2, new Swamp());
            Permanent nightmare = addReady(player2, new Nightmare());
            addPermanent(player1, new Swamp());

            Permanent clone = resolveClone(player1, nightmare.getId());

            assertThat(power(nightmare)).isEqualTo(3);
            assertThat(power(clone)).isEqualTo(1);
        }

        @Test
        @DisplayName("Counters (7c) apply on top of the CDA value (7a)")
        void cdaPlusCounters() {
            Permanent maro = addReady(player1, new Maro());
            maro.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
            harness.setHand(player1, cards(3));

            assertThat(power(maro)).isEqualTo(4);
            assertThat(toughness(maro)).isEqualTo(4);
        }

        @Test
        @DisplayName("The CDA sees creature types granted in layer 4 (Lignify makes a Treefolk)")
        void cdaSeesLayer4TypeChanges() {
            addPermanent(player1, new Forest());
            Permanent dourbark = addReady(player1, new DauntlessDourbark());
            // 1 Forest + itself (a Treefolk) = 2/2.
            assertThat(power(dourbark)).isEqualTo(2);

            Permanent bears = addReady(player1, new GrizzlyBears());
            attach(player1, new Lignify(), bears);

            // The bear is now a Treefolk in layer 4, and Lignify itself is a Kindred
            // Enchantment — Treefolk permanent (CR 205.3f), so the 7a count is 1 Forest
            // + 3 Treefolk (Dourbark, the bear, the Lignify aura) = 4/4.
            assertThat(power(dourbark)).isEqualTo(4);
        }

        @Test
        @DisplayName("The CDA sees land types set in layer 4 (Evil Presence makes a Swamp)")
        void cdaSeesLandTypeOverrides() {
            addPermanent(player1, new Swamp());
            Permanent nightmare = addReady(player1, new Nightmare());
            assertThat(power(nightmare)).isEqualTo(1);

            Permanent mountain = addPermanent(player1, new Mountain());
            attach(player1, new EvilPresence(), mountain);

            assertThat(power(nightmare)).isEqualTo(2);
        }
    }

    // =====================================================================================
    // Layer 7b — P/T-setting effects (CR 613.4b, timestamps CR 613.7)
    // =====================================================================================

    @Nested
    @DisplayName("Layer 7b: P/T setting")
    class Layer7bSetPT {

        @Test
        @DisplayName("A later one-shot setter beats an earlier Aura setter (Lignify then Diminish)")
        void spellSetterAfterAuraSetterWins() {
            Permanent elemental = addReady(player1, new AirElemental());
            attach(player2, new Lignify(), elemental); // base 0/4

            castDiminish(player2, elemental); // base 1/1, later timestamp

            assertThat(power(elemental)).isEqualTo(1);
            assertThat(toughness(elemental)).isEqualTo(1);
        }

        @Test
        @DisplayName("A later Aura setter beats an earlier one-shot setter (Diminish then Lignify)")
        void auraSetterAfterSpellSetterWins() {
            Permanent elemental = addReady(player1, new AirElemental());
            castDiminish(player2, elemental); // base 1/1

            attach(player2, new Lignify(), elemental); // base 0/4, later timestamp

            assertThat(power(elemental)).isEqualTo(0);
            assertThat(toughness(elemental)).isEqualTo(4);
        }

        @Test
        @DisplayName("A later one-shot setter beats an earlier Aura setter (Deep Freeze then Wings)")
        void wingsAfterDeepFreezeWins() {
            Permanent bears = addReady(player1, new GrizzlyBears());
            attach(player2, new DeepFreeze(), bears); // base 0/4

            castWingsOfVelisVel(player1, bears); // base 4/4, later timestamp

            assertThat(power(bears)).isEqualTo(4);
            assertThat(toughness(bears)).isEqualTo(4);
        }

        @Test
        @DisplayName("A later Aura setter beats an earlier one-shot setter (Wings then Deep Freeze)")
        void deepFreezeAfterWingsWins() {
            Permanent bears = addReady(player1, new GrizzlyBears());
            castWingsOfVelisVel(player1, bears); // base 4/4

            attach(player2, new DeepFreeze(), bears); // base 0/4, later timestamp

            assertThat(power(bears)).isEqualTo(0);
            assertThat(toughness(bears)).isEqualTo(4);
        }

        @Test
        @DisplayName("Counters (7c) apply on top of a 7b base")
        void countersApplyOnTopOfSetter() {
            Permanent elemental = addReady(player1, new AirElemental());
            elemental.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

            castDiminish(player2, elemental);

            assertThat(power(elemental)).isEqualTo(3);
            assertThat(toughness(elemental)).isEqualTo(3);
        }

        @Test
        @DisplayName("A pump resolved before the setter still applies after it (layer order beats timestamps)")
        void pumpBeforeSetterStillApplies() {
            Permanent bears = addReady(player1, new GrizzlyBears());
            castGiantGrowth(player1, bears); // 7c, earlier timestamp

            castDiminish(player2, bears); // 7b, later timestamp

            // 7b applies before 7c regardless of order of resolution: 1/1 + 3/3 = 4/4.
            assertThat(power(bears)).isEqualTo(4);
            assertThat(toughness(bears)).isEqualTo(4);
        }

        @Test
        @DisplayName("Of two one-shot setters, the later one wins (Wings then Diminish)")
        void secondSpellSetterWins() {
            Permanent bears = addReady(player1, new GrizzlyBears());
            castWingsOfVelisVel(player1, bears);

            castDiminish(player2, bears);

            assertThat(power(bears)).isEqualTo(1);
            assertThat(toughness(bears)).isEqualTo(1);
        }

        @Test
        @DisplayName("Of two one-shot setters, the later one wins (Diminish then Wings)")
        void secondSpellSetterWinsReversedOrder() {
            Permanent bears = addReady(player1, new GrizzlyBears());
            castDiminish(player2, bears);

            castWingsOfVelisVel(player1, bears);

            assertThat(power(bears)).isEqualTo(4);
            assertThat(toughness(bears)).isEqualTo(4);
        }

        @Test
        @DisplayName("A 7b setter overrides the MV-based P/T of a layer 4 animation")
        void setterOverridesAnimationBasePT() {
            addPermanent(player1, new MarchOfTheMachines());
            Permanent deathmantle = addPermanent(player1, new NimDeathmantle());
            assertThat(power(deathmantle)).isEqualTo(2); // sanity: MV of {2} Nim Deathmantle

            castDiminish(player2, deathmantle);

            assertThat(power(deathmantle)).isEqualTo(1);
            assertThat(toughness(deathmantle)).isEqualTo(1);
        }

        @Test
        @DisplayName("An until-end-of-turn setter expires at cleanup")
        void temporarySetterExpires() {
            Permanent bears = addReady(player1, new GrizzlyBears());
            castWingsOfVelisVel(player1, bears);
            assertThat(power(bears)).isEqualTo(4);

            endTurn();

            assertThat(power(bears)).isEqualTo(2);
            assertThat(toughness(bears)).isEqualTo(2);
        }
    }

    // =====================================================================================
    // Layer 7c — P/T additions, including counters (CR 613.4c)
    // =====================================================================================

    @Nested
    @DisplayName("Layer 7c: P/T additions and counters")
    class Layer7cAddPT {

        @Test
        @DisplayName("A one-shot pump adds to the creature's current P/T")
        void pumpAddsToCurrentBase() {
            Permanent bears = addReady(player1, new GrizzlyBears());

            castGiantGrowth(player1, bears);

            assertThat(power(bears)).isEqualTo(5);
            assertThat(toughness(bears)).isEqualTo(5);
        }

        @Test
        @DisplayName("A controller-scoped anthem boosts only its controller's creatures")
        void anthemBoostsOnlyControllersCreatures() {
            addPermanent(player1, new GloriousAnthem());
            Permanent own = addReady(player1, new GrizzlyBears());
            Permanent enemy = addReady(player2, new GrizzlyBears());

            assertThat(power(own)).isEqualTo(3);
            assertThat(power(enemy)).isEqualTo(2);
        }

        @Test
        @DisplayName("An anthem applies to a creature whose controller changed after the anthem entered")
        void anthemAppliesToCreatureStolenLater() {
            addPermanent(player1, new GloriousAnthem());
            Permanent bears = addReady(player2, new GrizzlyBears());

            castThreaten(player1, bears);

            assertThat(power(bears)).isEqualTo(3);
        }

        @Test
        @DisplayName("A layer 6 all-creature-types grant makes subtype-scoped 7c boosts apply")
        void allCreatureTypesGrantFeedsLordBoost() {
            addReady(player1, new ElvishChampion());
            Permanent bears = addReady(player1, new GrizzlyBears());
            Permanent amoeboid = addReady(player1, new AmoeboidChangeling());
            assertThat(power(bears)).isEqualTo(2); // sanity: not an Elf yet

            harness.activateAbility(player1,
                    gd.playerBattlefields.get(player1.getId()).indexOf(amoeboid), 0, null, bears.getId());
            harness.passBothPriorities();

            // The bear now has all creature types, so the Elf-scoped boost applies.
            assertThat(power(bears)).isEqualTo(3);
            assertThat(hasKeyword(bears, Keyword.FORESTWALK)).isTrue();
        }

        @Test
        @DisplayName("Losing all creature types turns subtype-scoped 7c boosts off")
        void losingCreatureTypesRemovesLordBoost() {
            addReady(player1, new ElvishChampion());
            Permanent elves = addReady(player1, new LlanowarElves());
            Permanent amoeboid = addReady(player1, new AmoeboidChangeling());
            assertThat(power(elves)).isEqualTo(2); // sanity

            harness.activateAbility(player1,
                    gd.playerBattlefields.get(player1.getId()).indexOf(amoeboid), 1, null, elves.getId());
            harness.passBothPriorities();

            assertThat(power(elves)).isEqualTo(1);
            assertThat(hasKeyword(elves, Keyword.FORESTWALK)).isFalse();
        }

        @Test
        @DisplayName("Counters stack with static boosts")
        void countersStackWithAnthem() {
            addPermanent(player1, new GloriousAnthem());
            Permanent bears = addReady(player1, new GrizzlyBears());
            bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

            assertThat(power(bears)).isEqualTo(4);
            assertThat(toughness(bears)).isEqualTo(4);
        }

        @Test
        @DisplayName("Multiple anthems stack additively")
        void multipleAnthemsStack() {
            addPermanent(player1, new GloriousAnthem());
            addPermanent(player1, new GloriousAnthem());
            Permanent bears = addReady(player1, new GrizzlyBears());

            assertThat(power(bears)).isEqualTo(4);
            assertThat(toughness(bears)).isEqualTo(4);
        }

        @Test
        @DisplayName("P/T boosts are not abilities: they apply to a creature that lost all abilities")
        void boostAppliesToAbilityLessCreature() {
            addPermanent(player1, new GloriousAnthem());
            Permanent bears = addReady(player1, new GrizzlyBears());
            attach(player2, new DeepFreeze(), bears);

            // Deep Freeze: base 0/4, loses abilities — the anthem still adds +1/+1 in 7c.
            assertThat(power(bears)).isEqualTo(1);
            assertThat(toughness(bears)).isEqualTo(5);
        }

        @Test
        @DisplayName("An until-end-of-turn pump expires at cleanup, counters persist")
        void pumpExpiresCountersPersist() {
            Permanent bears = addReady(player1, new GrizzlyBears());
            bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
            castGiantGrowth(player1, bears);
            assertThat(power(bears)).isEqualTo(6);

            endTurn();

            assertThat(power(bears)).isEqualTo(3);
            assertThat(toughness(bears)).isEqualTo(3);
        }

        @Test
        @DisplayName("A token receives subtype-scoped lord boosts")
        void tokenReceivesLordBoost() {
            Permanent perfect = addReady(player1, new ImperiousPerfect());
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.activateAbility(player1,
                    gd.playerBattlefields.get(player1.getId()).indexOf(perfect), null, null);
            harness.passBothPriorities();

            Permanent token = findPermanent(player1, "Elf Warrior");

            assertThat(power(token)).isEqualTo(2);
            assertThat(toughness(token)).isEqualTo(2);
        }
    }

    // =====================================================================================
    // Layer 7d — P/T switching (CR 613.4d: applied after all other layer 7 effects)
    // =====================================================================================

    @Nested
    @DisplayName("Layer 7d: P/T switching")
    class Layer7dSwitch {

        @Test
        @DisplayName("A switch swaps the creature's current power and toughness")
        void switchSwapsCurrentPT() {
            Permanent merfolk = addReady(player1, new CoralMerfolk()); // 2/1

            castTwistedImage(player2, merfolk);

            assertThat(power(merfolk)).isEqualTo(1);
            assertThat(toughness(merfolk)).isEqualTo(2);
        }

        @Test
        @DisplayName("Two switches cancel each other out")
        void secondSwitchCancelsFirst() {
            Permanent merfolk = addReady(player1, new CoralMerfolk());

            castTwistedImage(player2, merfolk);
            castTwistedImage(player2, merfolk);

            assertThat(power(merfolk)).isEqualTo(2);
            assertThat(toughness(merfolk)).isEqualTo(1);
        }

        @Test
        @DisplayName("A pump resolved after the switch is still applied before it (7c before 7d)")
        void pumpAfterSwitchAppliesBeforeIt() {
            Permanent merfolk = addReady(player1, new CoralMerfolk()); // 2/1

            castTwistedImage(player2, merfolk);
            castGiantGrowth(player1, merfolk);

            // 7c first: 2/1 + 3/3 = 5/4; then 7d switches: 4/5.
            assertThat(power(merfolk)).isEqualTo(4);
            assertThat(toughness(merfolk)).isEqualTo(5);
        }

        @Test
        @DisplayName("A setter resolved after the switch is still applied before it (7b before 7d)")
        void setterAfterSwitchAppliesBeforeIt() {
            Permanent merfolk = addReady(player1, new CoralMerfolk());

            castTwistedImage(player2, merfolk);
            castDiminish(player2, merfolk);

            // 7b sets base 1/1, then the switch swaps 1/1: still 1/1.
            assertThat(power(merfolk)).isEqualTo(1);
            assertThat(toughness(merfolk)).isEqualTo(1);
        }

        @Test
        @DisplayName("Switching a 0/4 base (set earlier in 7b) yields 4/0 and the creature dies")
        void switchAfterSetterKillsZeroToughness() {
            Permanent merfolk = addReady(player1, new CoralMerfolk());
            attach(player2, new Lignify(), merfolk); // base 0/4

            castTwistedImage(player2, merfolk);
            harness.runStateBasedActions();

            harness.assertInGraveyard(player1, "Coral Merfolk");
        }

        @Test
        @DisplayName("Switch before the setter gives the same result (layer order, not timestamps)")
        void switchBeforeSetterSameResult() {
            Permanent merfolk = addReady(player1, new CoralMerfolk());
            castTwistedImage(player2, merfolk);

            attach(player2, new Lignify(), merfolk); // later timestamp, but 7b still first
            harness.runStateBasedActions();

            harness.assertInGraveyard(player1, "Coral Merfolk");
        }

        @Test
        @DisplayName("An until-end-of-turn switch expires at cleanup")
        void switchExpiresAtCleanup() {
            Permanent merfolk = addReady(player1, new CoralMerfolk());
            castTwistedImage(player2, merfolk);
            assertThat(power(merfolk)).isEqualTo(1);

            endTurn();

            assertThat(power(merfolk)).isEqualTo(2);
            assertThat(toughness(merfolk)).isEqualTo(1);
        }

        @Test
        @DisplayName("A switch is not part of the copiable values")
        void switchIsNotCopiable() {
            Permanent merfolk = addReady(player2, new CoralMerfolk());
            castTwistedImage(player2, merfolk);

            Permanent clone = resolveClone(player1, merfolk.getId());

            assertThat(power(clone)).isEqualTo(2);
            assertThat(toughness(clone)).isEqualTo(1);
        }

        @Test
        @DisplayName("A self-switch to X/0 kills the creature via state-based actions")
        void selfSwitchToZeroToughnessDies() {
            Permanent turtleshell = addReady(player1, new TurtleshellChangeling()); // 0/4
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.activateAbility(player1,
                    gd.playerBattlefields.get(player1.getId()).indexOf(turtleshell), null, null);
            harness.passBothPriorities();
            harness.runStateBasedActions();

            harness.assertInGraveyard(player1, "Turtleshell Changeling");
        }

        @Test
        @DisplayName("Counters added before the switch are swapped along with everything else")
        void switchAppliesAfterCounters() {
            Permanent merfolk = addReady(player1, new CoralMerfolk()); // 2/1
            merfolk.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // 3/2

            castTwistedImage(player2, merfolk);

            assertThat(power(merfolk)).isEqualTo(2);
            assertThat(toughness(merfolk)).isEqualTo(3);
        }
    }
}
