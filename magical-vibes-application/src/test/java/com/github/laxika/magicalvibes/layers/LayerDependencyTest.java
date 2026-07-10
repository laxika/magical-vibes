package com.github.laxika.magicalvibes.layers;

import com.github.laxika.magicalvibes.cards.b.BloodMoon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CR 613.8 dependency: within one layer, an effect applies after the effects it depends on,
 * regardless of timestamps; dependency loops fall back to timestamp order (CR 613.8c).
 * Companion acceptance suite to {@code SevenLayerTest}, whose within-layer ordering tests are
 * timestamp-based and must all stay green alongside these.
 *
 * <p>Setups follow the SevenLayerTest timestamp convention: a permanent's timestamp is its
 * battlefield insertion order; floating effects are stamped on insertion. Synthetic ad-hoc
 * {@link Card}s stand in for effect combinations the card pool cannot produce (an Urborg-style
 * ability-granting land).
 */
class LayerDependencyTest extends BaseCardTest {

    private Permanent addPermanent(Player player, Card card) {
        card.setOwnerId(player.getId());
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private static Card creature(String name, int power, int toughness, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    private static Card artifact(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setManaCost(manaCost);
        return card;
    }

    /** Urborg-style nonbasic land: "Each land is a Swamp in addition to its other land types." */
    private static Card swampGrantingLand(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.LAND);
        card.addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.SWAMP,
                GrantScope.ALL_PERMANENTS, false, new PermanentIsLandPredicate()));
        return card;
    }

    /** Nonbasic land lord: "Creatures you control get +1/+1 and have flying." */
    private static Card boostGrantingLand(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.LAND);
        card.addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, Set.of(Keyword.FLYING),
                GrantScope.OWN_CREATURES));
        return card;
    }

    private void addLoseAllFloatingEffect(Player controller, Permanent target) {
        gd.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), "Test Lose All",
                null, controller.getId(),
                new LosesAllAbilitiesEffect(GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN),
                target.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
    }

    // ===== layer 4: applicability dependency (what the effect applies to) =====

    @Test
    @DisplayName("Xenograft's earlier-timestamp type grant applies to an artifact a later March of the Machines animates")
    void xenograftGrantDependsOnLaterMarchAnimation() {
        Permanent xenograft = addPermanent(player1, new Xenograft());
        xenograft.setChosenSubtype(CardSubtype.GOBLIN);
        Permanent relic = addPermanent(player1, artifact("Test Relic", "{3}"));
        addPermanent(player1, new MarchOfTheMachines());

        // Xenograft's "creatures you control" set depends on March making the artifact a
        // creature in the same layer (CR 613.8) — the earlier timestamp does not matter.
        var bonus = gqs.computeStaticBonus(gd, relic);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.GOBLIN);
        assertThat(gqs.getEffectivePower(gd, relic)).isEqualTo(3); // MV base P/T, sanity
    }

    @Test
    @DisplayName("Sanity: Xenograft entering after March grants the type in plain timestamp order")
    void xenograftAfterMarchGrantsType() {
        Permanent relic = addPermanent(player1, artifact("Test Relic", "{3}"));
        addPermanent(player1, new MarchOfTheMachines());
        Permanent xenograft = addPermanent(player1, new Xenograft());
        xenograft.setChosenSubtype(CardSubtype.GOBLIN);

        assertThat(gqs.computeStaticBonus(gd, relic).grantedSubtypes()).contains(CardSubtype.GOBLIN);
    }

    // ===== layer 4: existence dependency (Blood Moon vs an Urborg-style land) =====

    @Test
    @DisplayName("Sanity: the Urborg-style land grants Swamp to other lands without Blood Moon")
    void swampGrantingLandGrantsWithoutBloodMoon() {
        addPermanent(player1, swampGrantingLand("Test Tomb"));
        Permanent forest = addPermanent(player1, new Forest());

        assertThat(gqs.computeStaticBonus(gd, forest).grantedSubtypes()).contains(CardSubtype.SWAMP);
    }

    @Test
    @DisplayName("A later Blood Moon strips the Urborg-style land's grant: no land is a Swamp (existence dependency)")
    void swampGrantDependsOnLaterBloodMoon() {
        addPermanent(player1, swampGrantingLand("Test Tomb"));
        Permanent forest = addPermanent(player1, new Forest());
        addPermanent(player2, new BloodMoon());

        // The grant exists only while its source keeps its printed abilities; Blood Moon's
        // land-type set removes them (CR 305.7) as part of layer 4, so the grant depends on
        // Blood Moon and applies after it — when it no longer exists. The basic Forest
        // (untouched by Blood Moon) must not become a Swamp either.
        assertThat(gqs.computeStaticBonus(gd, forest).grantedSubtypes()).doesNotContain(CardSubtype.SWAMP);
    }

    @Test
    @DisplayName("An Urborg-style land entering after Blood Moon grants nothing either")
    void swampGrantingLandEnteringAfterBloodMoonIsStripped() {
        addPermanent(player2, new BloodMoon());
        addPermanent(player1, swampGrantingLand("Test Tomb"));
        Permanent forest = addPermanent(player1, new Forest());

        assertThat(gqs.computeStaticBonus(gd, forest).grantedSubtypes()).doesNotContain(CardSubtype.SWAMP);
    }

    // ===== CR 305.7 across layers: a Blood Mooned land's later-layer statics stop existing =====

    @Test
    @DisplayName("A land granting +1/+1 and flying stops granting once Blood Moon sets its land type")
    void bloodMoonedLandStopsGrantingBoostAndKeyword() {
        addPermanent(player1, boostGrantingLand("Test Aerie"));
        Permanent bears = addPermanent(player1, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue(); // sanity
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3); // sanity

        addPermanent(player2, new BloodMoon());

        // The land's printed ability is removed in layer 4 (CR 305.7): its layer-6 keyword
        // grant and its layer-7c boost both stop applying.
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The granting land entering after Blood Moon grants nothing either")
    void boostGrantingLandEnteringAfterBloodMoonIsStripped() {
        addPermanent(player2, new BloodMoon());
        addPermanent(player1, boostGrantingLand("Test Aerie"));
        Permanent bears = addPermanent(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    // ===== layer 6: existence dependency (lose-all on a lord kills its grant) =====

    @Test
    @DisplayName("A lose-all on a lord strips its grant from other creatures even with a later timestamp")
    void loseAllOnLordStripsGrantRegardlessOfTimestamps() {
        Card lordCard = creature("Test Lord", 2, 2, CardSubtype.HUMAN);
        lordCard.addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, Set.of(Keyword.FLYING),
                GrantScope.OWN_CREATURES));
        Permanent lord = addPermanent(player1, lordCard);
        Permanent bears = addPermanent(player1, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue(); // sanity

        addLoseAllFloatingEffect(player2, lord);

        // The lord's grant exists only while the lord has the ability; the lose-all removes it
        // in the same layer, so the grant depends on the lose-all and applies after it (when it
        // no longer exists) even though the lose-all has the LATER timestamp — unlike a grant
        // TO the stripped creature, which is timestamp-ordered (Humility doctrine, pinned by
        // SevenLayerTest's keywordGrantAfterLoseAllApplies).
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2); // the 7c boost is gone too
        assertThat(gqs.computeStaticBonus(gd, lord).losesAllAbilities()).isTrue(); // sanity
    }

    // ===== CR 613.8c: dependency loops fall back to timestamp order =====

    @Test
    @DisplayName("Mutually dependent subtype grants fall back to timestamp order and terminate")
    void dependencyLoopFallsBackToTimestampOrder() {
        Permanent goblin = addPermanent(player1, creature("Test Goblin", 1, 1, CardSubtype.GOBLIN));
        Permanent elf = addPermanent(player1, creature("Test Elf", 1, 1, CardSubtype.ELF));

        Card goblinsAreElves = new Card();
        goblinsAreElves.setName("Goblins Are Elves");
        goblinsAreElves.setType(CardType.ENCHANTMENT);
        goblinsAreElves.addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.ELF,
                GrantScope.ALL_CREATURES, false, new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.GOBLIN))));
        addPermanent(player1, goblinsAreElves);

        Card elvesAreGoblins = new Card();
        elvesAreGoblins.setName("Elves Are Goblins");
        elvesAreGoblins.setType(CardType.ENCHANTMENT);
        elvesAreGoblins.addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.GOBLIN,
                GrantScope.ALL_CREATURES, false, new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.ELF))));
        addPermanent(player1, elvesAreGoblins);

        // Each grant changes what the other applies to — a dependency loop. CR 613.8c ignores
        // the dependencies: timestamp order applies "Goblins are Elves" first (the Goblin
        // gains ELF), then "Elves are Goblins" hits both.
        assertThat(gqs.computeStaticBonus(gd, goblin).grantedSubtypes()).contains(CardSubtype.ELF);
        assertThat(gqs.computeStaticBonus(gd, elf).grantedSubtypes()).contains(CardSubtype.GOBLIN);
    }
}
