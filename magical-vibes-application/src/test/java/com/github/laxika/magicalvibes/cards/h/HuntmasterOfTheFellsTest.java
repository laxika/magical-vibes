package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RavagerOfTheFells;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetOpponentAndUpToCreaturesThatPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.NoSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HuntmasterOfTheFellsTest extends BaseCardTest {

    @Test
    @DisplayName("Front and back faces have correct effects configured")
    void hasCorrectEffectsConfigured() {
        HuntmasterOfTheFells card = new HuntmasterOfTheFells();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).get(0)).isInstanceOf(CreateTokenEffect.class);
        CreateTokenEffect token = (CreateTokenEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).get(0);
        assertThat(token.tokenName()).isEqualTo("Wolf");
        assertThat(token.power()).isEqualTo(2);
        assertThat(token.toughness()).isEqualTo(2);
        assertThat(token.color()).isEqualTo(CardColor.GREEN);
        assertThat(token.subtypes()).containsExactly(CardSubtype.WOLF);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).get(1))
                .isInstanceOfSatisfying(GainLifeEffect.class, e -> assertThat(e.amount()).isEqualTo(2));

        assertThat(card.getEffects(EffectSlot.ON_TRANSFORM_TO_FRONT_FACE)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(NoSpellsCastLastTurnConditionalEffect.class);
        NoSpellsCastLastTurnConditionalEffect frontTransform =
                (NoSpellsCastLastTurnConditionalEffect) card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst();
        assertThat(frontTransform.wrapped()).isInstanceOf(TransformSelfEffect.class);

        assertThat(card.getBackFaceCard()).isInstanceOf(RavagerOfTheFells.class);
        assertThat(card.getBackFaceClassName()).isEqualTo("RavagerOfTheFells");

        RavagerOfTheFells backFace = (RavagerOfTheFells) card.getBackFaceCard();
        assertThat(backFace.getEffects(EffectSlot.ON_TRANSFORM_TO_BACK_FACE)).hasSize(1);
        assertThat(backFace.getEffects(EffectSlot.ON_TRANSFORM_TO_BACK_FACE).getFirst())
                .isInstanceOfSatisfying(DealDamageToTargetOpponentAndUpToCreaturesThatPlayerControlsEffect.class, e -> {
                    assertThat(e.opponentDamage()).isEqualTo(2);
                    assertThat(e.creatureDamage()).isEqualTo(2);
                    assertThat(e.maxCreatureTargets()).isEqualTo(1);
                });
        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(TwoOrMoreSpellsCastLastTurnConditionalEffect.class);
    }

    @Test
    @DisplayName("ETB creates a Wolf token and controller gains 2 life")
    void etbCreatesWolfAndGainsLife() {
        int lifeBefore = gd.getLife(player1.getId());

        harness.setHand(player1, List.of(new HuntmasterOfTheFells()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        Permanent wolf = findPermanent(player1, "Wolf");
        assertThat(wolf.getCard().isToken()).isTrue();
        assertThat(wolf.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(wolf.getCard().getSubtypes()).containsExactly(CardSubtype.WOLF);
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Transforms to Ravager when no spells were cast last turn and damages opponent plus their creature")
    void transformsToRavagerAndDamagesOpponentAndCreature() {
        harness.addToBattlefield(player1, new HuntmasterOfTheFells());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent huntmaster = findPermanent(player1, "Huntmaster of the Fells");
        Permanent bear = findPermanent(player2, "Grizzly Bears");
        int player2LifeBefore = gd.getLife(player2.getId());

        gd.spellsCastLastTurn.clear();
        advanceToUpkeepAndResolveTransform(player1);

        assertThat(huntmaster.isTransformed()).isTrue();
        assertThat(huntmaster.getCard().getName()).isEqualTo("Ravager of the Fells");

        assertThat(gd.interaction.permanentChoice().validIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.interaction.permanentChoice().validIds()).contains(bear.getId(), player1.getId());
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore - 2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Ravager transform trigger can choose no creature target")
    void ravagerCanChooseNoCreatureTarget() {
        harness.addToBattlefield(player1, new HuntmasterOfTheFells());
        harness.addToBattlefield(player2, new GiantSpider());
        Permanent spider = findPermanent(player2, "Giant Spider");
        int player2LifeBefore = gd.getLife(player2.getId());

        gd.spellsCastLastTurn.clear();
        advanceToUpkeepAndResolveTransform(player1);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore - 2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(spider);
        assertThat(spider.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Creature target choices are limited to creatures controlled by the targeted opponent")
    void creatureTargetMustBeControlledByTargetedOpponent() {
        harness.addToBattlefield(player1, new HuntmasterOfTheFells());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        Permanent ownBear = findPermanent(player1, "Grizzly Bears");
        Permanent opponentSpider = findPermanent(player2, "Giant Spider");

        gd.spellsCastLastTurn.clear();
        advanceToUpkeepAndResolveTransform(player1);

        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.interaction.permanentChoice().validIds())
                .contains(opponentSpider.getId(), player1.getId())
                .doesNotContain(ownBear.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ravager transforms back when a player cast two or more spells last turn and front trigger resolves")
    void ravagerTransformsBackAndFrontTriggerResolves() {
        harness.addToBattlefield(player1, new HuntmasterOfTheFells());
        Permanent huntmaster = findPermanent(player1, "Huntmaster of the Fells");

        gd.spellsCastLastTurn.clear();
        advanceToUpkeepAndResolveTransform(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(huntmaster.isTransformed()).isTrue();

        int lifeBefore = gd.getLife(player1.getId());
        long wolfCountBefore = wolfCount();
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(huntmaster.isTransformed()).isFalse();
        assertThat(huntmaster.getCard().getName()).isEqualTo("Huntmaster of the Fells");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(wolfCount()).isEqualTo(wolfCountBefore + 1);
    }

    @Test
    @DisplayName("Ravager has trample on the back face")
    void ravagerHasTrample() {
        harness.addToBattlefield(player1, new HuntmasterOfTheFells());
        Permanent huntmaster = findPermanent(player1, "Huntmaster of the Fells");

        gd.spellsCastLastTurn.clear();
        advanceToUpkeepAndResolveTransform(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, huntmaster, Keyword.TRAMPLE)).isTrue();
    }

    private void advanceToUpkeepAndResolveTransform(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private long wolfCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Wolf"))
                .count();
    }
}
