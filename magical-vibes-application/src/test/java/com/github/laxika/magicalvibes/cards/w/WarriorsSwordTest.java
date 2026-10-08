package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarriorsSword.class, GrizzlyBears.class, Disenchant.class})
class WarriorsSwordTest extends BaseCardTest {

    @Test
    void enteringCreatesAndEquipsHero() {
        harness.castFromHand(player1, new WarriorsSword(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent sword = findPermanent(player1, "Warrior's Sword");
        Permanent hero = findPermanent(player1, "Hero");

        assertThat(sword.getAttachedTo()).isEqualTo(hero.getId());
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero))
                .contains(CardSubtype.HERO, CardSubtype.WARRIOR);
    }

    @Test
    void equipMovesSwordAndItsBonuses() {
        Permanent sword = addCreatureReady(player1, new WarriorsSword());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        sword.setAttachedTo(first.getId());

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, first)).doesNotContain(CardSubtype.WARRIOR);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, second)).contains(CardSubtype.WARRIOR);
    }

    @Test
    void jobSelectStillCreatesHeroWhenSwordIsDestroyedInResponse() {
        harness.castFromHand(player1, new WarriorsSword(), "{3}{R}");
        harness.passBothPriorities();
        Permanent sword = findPermanent(player1, "Warrior's Sword");

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, sword.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Warrior's Sword");
        harness.assertNotOnBattlefield(player1, "Warrior's Sword");
        assertThat(countPermanents(player1, "Hero")).isEqualTo(1);
        Permanent hero = findPermanent(player1, "Hero");
        assertThat(hero.getCard().isToken()).isTrue();
        assertThat(gqs.isCreature(gd, hero)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, hero)).isEmpty();
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero)).contains(CardSubtype.HERO)
                .doesNotContain(CardSubtype.WARRIOR);
    }

    @Test
    void destroyingSwordRemovesBonusesButLeavesHeroAlive() {
        harness.castFromHand(player1, new WarriorsSword(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent sword = findPermanent(player1, "Warrior's Sword");
        Permanent hero = findPermanent(player1, "Hero");

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, sword.getId());

        harness.assertInGraveyard(player1, "Warrior's Sword");
        harness.assertOnBattlefield(player1, "Hero");
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero)).contains(CardSubtype.HERO)
                .doesNotContain(CardSubtype.WARRIOR);
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new WarriorsSword());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sword.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipRequiresFiveMana() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new WarriorsSword());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Not enough mana");
        assertThat(sword.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotBeActivatedOutsideMainPhase() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new WarriorsSword());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("sorcery speed");
        assertThat(sword.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotBeActivatedOnOpponentsTurn() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new WarriorsSword());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("sorcery speed");
        assertThat(sword.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotBeActivatedWhileJobSelectIsOnStack() {
        harness.castFromHand(player1, new WarriorsSword(), "{3}{R}");
        harness.passBothPriorities();
        Permanent sword = findPermanent(player1, "Warrior's Sword");
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("stack is empty");
        assertThat(sword.getAttachedTo()).isNull();
        harness.passBothPriorities();
        assertThat(sword.getAttachedTo()).isEqualTo(findPermanent(player1, "Hero").getId());
    }
}
