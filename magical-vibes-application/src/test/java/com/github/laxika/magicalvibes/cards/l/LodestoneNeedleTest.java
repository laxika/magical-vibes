package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GuidestoneCompass;
import com.github.laxika.magicalvibes.cards.s.SunshotMilitia;
import com.github.laxika.magicalvibes.cards.c.CareeningMineCart;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LodestoneNeedle.class, GuidestoneCompass.class, Forest.class,
        SunshotMilitia.class, CareeningMineCart.class})
class LodestoneNeedleTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by tapping a creature and putting two stun counters on it")
    void entersAndStunsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SunshotMilitia());
        harness.setHand(player1, List.of(new LodestoneNeedle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target a noncreature artifact with its enter-the-battlefield ability")
    void entersAndStunsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CareeningMineCart());
        harness.setHand(player1, List.of(new LodestoneNeedle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0, artifact.getId());
        resolveAllTriggers();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(artifact.getCounterCount(CounterType.STUN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Craft returns it transformed and exiles another artifact")
    void craftsIntoGuidestoneCompass() {
        Permanent needle = harness.addToBattlefieldAndReturn(player1, new LodestoneNeedle());
        CareeningMineCart material = new CareeningMineCart();
        Permanent battlefieldMaterial = harness.addToBattlefieldAndReturn(player1, material);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(needle, battlefieldMaterial);
        assertThat(gd.findExiledCard(material.getId())).isNotNull();

        harness.passBothPriorities();

        Permanent compass = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GuidestoneCompass)
                .findFirst().orElseThrow();
        assertThat(compass.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Guidestone Compass lets a controlled creature explore")
    void compassExploresControlledCreature() {
        Permanent compass = harness.addToBattlefieldAndReturn(player1, new GuidestoneCompass());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SunshotMilitia());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(compass.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void canDeclineToTargetAnAvailableCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SunshotMilitia());
        harness.setHand(player1, List.of(new LodestoneNeedle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Lodestone Needle");
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void alreadyTappedCreatureStillGetsTwoStunCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SunshotMilitia());
        creature.tap();
        harness.setHand(player1, List.of(new LodestoneNeedle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(2);
    }

    @Test
    void flashAllowsCastingDuringOpponentsEndStep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SunshotMilitia());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new LodestoneNeedle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0, creature.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Lodestone Needle");
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(2);
    }

    @Test
    void craftsWithArtifactCardFromGraveyard() {
        Permanent needle = harness.addToBattlefieldAndReturn(player1, new LodestoneNeedle());
        Card material = new CareeningMineCart();
        harness.setGraveyard(player1, List.of(material));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(needle);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(material);
        assertThat(gd.findExiledCard(material.getId())).isNotNull();
        assertThat(gd.findExiledCard(needle.getCard().getId())).isNotNull();

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.isTransformed() && permanent.getCard() instanceof GuidestoneCompass);
        assertThat(gd.findExiledCard(needle.getCard().getId())).isNull();
        assertThat(gd.findExiledCard(material.getId())).isNotNull();
    }

    @Test
    void cannotCraftWithoutAnotherArtifact() {
        Permanent needle = harness.addToBattlefieldAndReturn(player1, new LodestoneNeedle());
        harness.addToBattlefield(player1, new SunshotMilitia());
        harness.addToBattlefield(player2, new CareeningMineCart());
        harness.setGraveyard(player1, List.of(new SunshotMilitia()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(needle);
        assertThat(gd.findExiledCard(needle.getCard().getId())).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void craftCannotBeActivatedOutsideMainPhase() {
        Permanent needle = harness.addToBattlefieldAndReturn(player1, new LodestoneNeedle());
        harness.addToBattlefield(player1, new CareeningMineCart());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.END_STEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(needle);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void compassCannotTargetOpponentsCreature() {
        Permanent compass = harness.addToBattlefieldAndReturn(player1, new GuidestoneCompass());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SunshotMilitia());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(compass.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void compassCannotBeActivatedOutsideMainPhase() {
        Permanent compass = harness.addToBattlefieldAndReturn(player1, new GuidestoneCompass());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SunshotMilitia());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.END_STEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(compass.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exploringNonlandCanPutItIntoGraveyard() {
        harness.addToBattlefield(player1, new GuidestoneCompass());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SunshotMilitia());
        Card revealed = new LodestoneNeedle();
        harness.setLibrary(player1, List.of(revealed));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(revealed);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void exploringNonlandCanLeaveItOnTop() {
        harness.addToBattlefield(player1, new GuidestoneCompass());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SunshotMilitia());
        Card revealed = new LodestoneNeedle();
        harness.setLibrary(player1, List.of(revealed));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealed);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(revealed);
    }

    @Test
    void exploringEmptyLibraryStillAddsCounter() {
        harness.addToBattlefield(player1, new GuidestoneCompass());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SunshotMilitia());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void cannotTargetANonartifactNoncreatureLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new LodestoneNeedle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(land.isTapped()).isFalse();
        assertThat(land.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void stunCountersReplaceTwoUntapsBeforeCreatureUntaps() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SunshotMilitia());
        harness.setHand(player1, List.of(new LodestoneNeedle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0, creature.getId());
        resolveAllTriggers();

        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(1);

        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isZero();

        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }
}
