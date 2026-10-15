package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.c.CrashingDrawbridge;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.ShamblingSuit;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KenrithsTransformation.class, FountainOfYouth.class, Ornithopter.class,
        CrashingDrawbridge.class, ShamblingSuit.class})
class KenrithsTransformationTest extends BaseCardTest {

    @Test
    void transformsEnchantedCreatureIntoGreenElk() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        castAndResolve(thopter);

        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, thopter)).containsExactly(CardColor.GREEN);
        assertThat(gqs.getEffectiveCardTypes(gd, thopter)).containsExactly(CardType.CREATURE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, thopter)).containsExactly(CardSubtype.ELK);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isFalse();
    }

    @Test
    void drawsACardWhenItEnters() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setLibrary(player1, List.of(new FountainOfYouth()));

        castAndResolve(thopter);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void removingAuraRestoresEnchantedCreature() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        castAndResolve(thopter);

        Permanent aura = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Kenrith's Transformation"));
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, thopter)).isEmpty();
        assertThat(gqs.getEffectiveCardTypes(gd, thopter))
                .containsExactlyInAnyOrder(CardType.ARTIFACT, CardType.CREATURE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, thopter)).containsExactly(CardSubtype.THOPTER);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new KenrithsTransformation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void countersStillModifyTheNewBasePowerAndToughness() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CrashingDrawbridge());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castAndResolve(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    void removesPrintedActivatedAbilities() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CrashingDrawbridge());
        creature.setSummoningSick(false);

        castAndResolve(creature);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void suppressesCharacteristicDefiningPowerAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ShamblingSuit());
        harness.addToBattlefield(player2, new CrashingDrawbridge());

        castAndResolve(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void keepsAbilitiesGrantedAfterTransformation() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ShamblingSuit());
        Permanent bridge = harness.addToBattlefieldAndReturn(player1, new CrashingDrawbridge());
        bridge.setSummoningSick(false);

        castAndResolve(creature);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
    }

    @Test
    void doesNotEnterOrDrawWhenTargetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CrashingDrawbridge());
        harness.setLibrary(player1, List.of(new ShamblingSuit()));
        harness.setHand(player1, List.of(new KenrithsTransformation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kenrith's Transformation");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof KenrithsTransformation);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawTriggerResolvesAfterAuraLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CrashingDrawbridge());
        harness.setLibrary(player1, List.of(new ShamblingSuit()));

        castAndResolve(creature);
        Permanent aura = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Kenrith's Transformation"));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement().isInstanceOf(ShamblingSuit.class);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEFENDER)).isTrue();
    }

    private void castAndResolve(Permanent target) {
        harness.setHand(player1, List.of(new KenrithsTransformation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
