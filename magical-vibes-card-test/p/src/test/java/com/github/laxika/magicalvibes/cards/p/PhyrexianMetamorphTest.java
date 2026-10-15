package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianMetamorph.class, AirElemental.class, GrizzlyBears.class,
        JayemdaeTome.class, Juggernaut.class, PriestOfUrabrask.class})
class PhyrexianMetamorphTest extends BaseCardTest {


    @Test
    @DisplayName("Copying a creature gains artifact type in addition to creature")
    void copyingCreatureGainsArtifactType() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PhyrexianMetamorph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        GameData gd = harness.getGameData();
        Permanent metamorphPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Phyrexian Metamorph"))
                .findFirst().orElse(null);

        assertThat(metamorphPerm).isNotNull();
        assertThat(metamorphPerm.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(metamorphPerm.getCard().getPower()).isEqualTo(2);
        assertThat(metamorphPerm.getCard().getToughness()).isEqualTo(2);
        // Primary type is creature, but artifact should be added as an additional type
        assertThat(metamorphPerm.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(metamorphPerm.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
    }


    @Test
    @DisplayName("Copying a non-creature artifact is NOT a creature (just an artifact)")
    void copyingNonCreatureArtifactIsNotCreature() {
        harness.addToBattlefield(player2, new JayemdaeTome());
        harness.setHand(player1, List.of(new PhyrexianMetamorph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID tomeId = harness.getPermanentId(player2, "Jayemdae Tome");
        harness.handlePermanentChosen(player1, tomeId);

        GameData gd = harness.getGameData();
        Permanent metamorphPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Phyrexian Metamorph"))
                .findFirst().orElse(null);

        assertThat(metamorphPerm).isNotNull();
        assertThat(metamorphPerm.getCard().getName()).isEqualTo("Jayemdae Tome");
        assertThat(metamorphPerm.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        // Per rules: copying a noncreature artifact means Metamorph is NOT a creature
        assertThat(metamorphPerm.getCard().getAdditionalTypes()).doesNotContain(CardType.CREATURE);
        assertThat(metamorphPerm.getCard().getActivatedAbilities()).hasSize(1);
    }


    @Test
    @DisplayName("Copying an artifact creature preserves both types")
    void copyingArtifactCreaturePreservesBothTypes() {
        harness.addToBattlefield(player2, new Juggernaut());
        harness.setHand(player1, List.of(new PhyrexianMetamorph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID juggernautId = harness.getPermanentId(player2, "Juggernaut");
        harness.handlePermanentChosen(player1, juggernautId);

        GameData gd = harness.getGameData();
        Permanent metamorphPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Phyrexian Metamorph"))
                .findFirst().orElse(null);

        assertThat(metamorphPerm).isNotNull();
        assertThat(metamorphPerm.getCard().getName()).isEqualTo("Juggernaut");
        assertThat(metamorphPerm.getCard().getPower()).isEqualTo(5);
        assertThat(metamorphPerm.getCard().getToughness()).isEqualTo(3);
        // Juggernaut is already "Artifact Creature" — types should be preserved
        assertThat(metamorphPerm.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(metamorphPerm.getCard().getAdditionalTypes()).contains(CardType.CREATURE);
    }


    @Test
    @DisplayName("Copying a creature with flying copies the keyword and adds artifact type")
    void copiesKeywordsAndAddsArtifact() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new PhyrexianMetamorph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.handlePermanentChosen(player1, targetId);

        GameData gd = harness.getGameData();
        Permanent metamorphPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Phyrexian Metamorph"))
                .findFirst().orElse(null);

        assertThat(metamorphPerm).isNotNull();
        assertThat(metamorphPerm.getCard().getName()).isEqualTo("Air Elemental");
        assertThat(metamorphPerm.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(metamorphPerm.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
    }


    @Test
    @DisplayName("Enters as 0/0 and dies when player declines to copy")
    void diesWhenPlayerDeclines() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PhyrexianMetamorph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Phyrexian Metamorph");
        harness.assertInGraveyard(player1, "Phyrexian Metamorph");
    }

    @Test
    @DisplayName("Enters as 0/0 and dies when no artifacts or creatures on battlefield")
    void diesWhenNoValidTargets() {
        harness.setHand(player1, List.of(new PhyrexianMetamorph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phyrexian Metamorph");
        harness.assertInGraveyard(player1, "Phyrexian Metamorph");
    }


    @Test
    @DisplayName("Goes to graveyard as Phyrexian Metamorph when destroyed")
    void goesToGraveyardAsPhyrexianMetamorph() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PhyrexianMetamorph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        GameData gd = harness.getGameData();

        Permanent metamorphPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Phyrexian Metamorph"))
                .findFirst().orElse(null);
        assertThat(metamorphPerm).isNotNull();

        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, metamorphPerm);

        harness.assertInGraveyard(player1, "Phyrexian Metamorph");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void copiedEnterAbilityTriggersForMetamorphController() {
        harness.addToBattlefield(player2, new PriestOfUrabrask());
        harness.setHand(player1, List.of(new PhyrexianMetamorph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Priest of Urabrask"));

        harness.assertOnBattlefield(player1, "Priest of Urabrask");
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void copiedNoncreatureArtifactCanTapImmediatelyToDraw() {
        harness.addToBattlefield(player2, new JayemdaeTome());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new PhyrexianMetamorph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Jayemdae Tome"));

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void canPayPhyrexianManaWithLifeAndCopyOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new PhyrexianMetamorph()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        harness.assertLife(player1, 18);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    void doesNotCopyCountersDamageOrTappedStatus() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        bears.setMarkedDamage(1);
        bears.tap();
        harness.setHand(player1, List.of(new PhyrexianMetamorph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent copy = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(copy.getPlusOnePlusOneCounters()).isZero();
        assertThat(copy.getMarkedDamage()).isZero();
        assertThat(copy.isTapped()).isFalse();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, copy)).isEqualTo(2);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, copy)).isEqualTo(2);
    }

    @Test
    void anotherMetamorphCopiesArtifactExceptionOfFirstCopy() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PhyrexianMetamorph(), new PhyrexianMetamorph()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        UUID firstCopyId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, firstCopyId);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allSatisfy(p -> {
                    assertThat(p.getCard().getName()).isEqualTo("Grizzly Bears");
                    assertThat(p.getCard().hasType(CardType.ARTIFACT)).isTrue();
                    assertThat(p.getCard().hasType(CardType.CREATURE)).isTrue();
                });
    }
}
