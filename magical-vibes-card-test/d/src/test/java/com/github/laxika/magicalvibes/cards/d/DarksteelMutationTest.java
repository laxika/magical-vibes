package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;




@CardUsed({DarksteelMutation.class, FountainOfYouth.class, SerraAngel.class, Ornithopter.class,
        LightningBolt.class, LlanowarElves.class})
class DarksteelMutationTest extends BaseCardTest {

    @Test
    void transformsEnchantedCreatureIntoIndestructibleInsectArtifact() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castAndResolve(angel);

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardTypes(gd, angel))
                .containsExactlyInAnyOrder(CardType.ARTIFACT, CardType.CREATURE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, angel)).containsExactly(CardSubtype.INSECT);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();
    }

    @Test
    void removingAuraRestoresEnchantedCreature() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castAndResolve(angel);

        Permanent aura = findPermanent(player1, "Darksteel Mutation");
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveCardTypes(gd, angel)).containsExactly(CardType.CREATURE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, angel)).containsExactly(CardSubtype.ANGEL);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new DarksteelMutation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castAndResolve(Permanent target) {
        harness.setHand(player1, List.of(new DarksteelMutation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }
    @Test
    void transformsEnchantedCreatureIntoIndestructibleInsectArtifactCreature() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        castDarksteelMutation(angel);

        assertThat(gqs.getEffectivePower(gd, angel)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, angel)).containsExactly(CardColor.WHITE);
        assertThat(gqs.getEffectiveCardTypes(gd, angel))
                .containsExactlyInAnyOrder(CardType.ARTIFACT, CardType.CREATURE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, angel)).containsExactly(CardSubtype.INSECT);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();
    }

    private void castDarksteelMutation(Permanent target) {
        harness.setHand(player1, List.of(new DarksteelMutation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }


    @Test
    void retainsPowerToughnessCounters() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        angel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castAndResolve(angel);

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(3);
        assertThat(angel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void indestructibleCreatureSurvivesLethalDamage() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        castAndResolve(angel);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, angel.getId());

        harness.assertOnBattlefield(player2, "Serra Angel");
        assertThat(angel.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void zeroToughnessKillsCreatureDespiteIndestructible() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        angel.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        castAndResolve(angel);

        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertInGraveyard(player2, "Serra Angel");
        harness.assertInGraveyard(player1, "Darksteel Mutation");
    }

    @Test
    void transformsAlreadyArtifactCreatureWithoutChangingItsColor() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        castAndResolve(thopter);

        assertThat(gqs.getEffectiveColors(gd, thopter)).isEmpty();
        assertThat(gqs.getEffectivePower(gd, thopter)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, thopter)).containsExactly(CardSubtype.INSECT);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void removesPrintedManaAbility() {
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elves.setSummoningSick(false);
        castAndResolve(elves);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Permanent has lost its abilities");
        assertThat(elves.isTapped()).isFalse();
    }

}
