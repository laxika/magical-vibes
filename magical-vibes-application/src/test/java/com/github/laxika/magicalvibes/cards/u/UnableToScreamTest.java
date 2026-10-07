package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.e.ErraticApparition;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.m.MasterOfPearls;
import com.github.laxika.magicalvibes.cards.m.MishrasFactory;
import com.github.laxika.magicalvibes.cards.s.ShowstoppingSurprise;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnableToScream.class, AirElemental.class, FountainOfYouth.class, MasterOfPearls.class,
        ErraticApparition.class, ShowstoppingSurprise.class, MishrasFactory.class})
class UnableToScreamTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature becomes a 0/2 Toy artifact and loses its abilities")
    void transformsEnchantedCreature() {
        Permanent airElemental = addCreatureReady(player2, new AirElemental());
        addAttachedAura(airElemental);

        assertThat(gqs.getEffectivePower(gd, airElemental)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(2);
        assertThat(gqs.isArtifact(gd, airElemental)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, airElemental)).contains(CardSubtype.TOY);
        assertThat(gqs.hasKeyword(gd, airElemental,
                com.github.laxika.magicalvibes.model.Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A face-down enchanted creature cannot be turned face up")
    void preventsFaceDownCreatureFromTurningFaceUp() {
        Permanent faceDown = addFaceDownMasterOfPearls();
        addAttachedAura(faceDown);
        prepareToTurnFaceUp();

        assertThatThrownBy(() -> harness.turnFaceUp(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(faceDown)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be turned face up");
        assertThat(faceDown.isFaceDown()).isTrue();
    }

    @Test
    @DisplayName("Removing Unable to Scream lets the face-down creature turn face up")
    void removingAuraAllowsFaceUpTurn() {
        Permanent faceDown = addFaceDownMasterOfPearls();
        Permanent aura = addAttachedAura(faceDown);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        prepareToTurnFaceUp();

        harness.turnFaceUp(player2, gd.playerBattlefields.get(player2.getId()).indexOf(faceDown));

        assertThat(faceDown.isFaceDown()).isFalse();
    }

    @Test
    @DisplayName("Unable to Scream can target only a creature")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new UnableToScream()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void resolvingAuraRetainsCreatureSubtypeAndAppliesCountersAboveBaseStats() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ErraticApparition());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new UnableToScream()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Unable to Scream").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, creature)).isTrue();
        assertThat(gqs.isArtifact(gd, creature)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).contains(CardSubtype.SPIRIT, CardSubtype.TOY);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void removingAuraRestoresPrintedAbilitiesAndStats() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ErraticApparition());
        Permanent aura = addAttachedAura(creature);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.isArtifact(gd, creature)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).doesNotContain(CardSubtype.TOY);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void spellCannotTurnEnchantedCreatureFaceUp() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MasterOfPearls());
        creature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        addAttachedAura(creature);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new ShowstoppingSurprise()));
        harness.addMana(player2, ManaColor.RED, 5);

        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(creature.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void animatedLandRemainsCreatureAfterAnimationExpires() {
        Permanent factory = harness.addToBattlefieldAndReturn(player1, new MishrasFactory());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new UnableToScream()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, factory.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(factory.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, factory)).isTrue();
        assertThat(gqs.isLand(gd, factory)).isTrue();
        assertThat(gqs.isArtifact(gd, factory)).isTrue();
        assertThat(gqs.getEffectivePower(gd, factory)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, factory)).isEqualTo(2);
        assertThat(findPermanent(player1, "Unable to Scream").getAttachedTo()).isEqualTo(factory.getId());
    }

    private Permanent addFaceDownMasterOfPearls() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new MasterOfPearls());
        permanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        return permanent;
    }

    private Permanent addAttachedAura(Permanent target) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UnableToScream());
        aura.setAttachedTo(target.getId());
        return aura;
    }

    private void prepareToTurnFaceUp() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.WHITE, 2);
    }
}
