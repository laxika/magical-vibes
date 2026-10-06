package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SagesNouliths.class, GrizzlyBears.class})
class SagesNoulithsTest extends BaseCardTest {

    @Test
    void enteringCreatesAndEquipsHero() {
        harness.setHand(player1, List.of(new SagesNouliths()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent nouliths = findPermanent(player1, "Sage's Nouliths");
        Permanent hero = findPermanent(player1, "Hero");

        assertThat(nouliths.getAttachedTo()).isEqualTo(hero.getId());
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero))
                .contains(CardSubtype.HERO, CardSubtype.CLERIC);
    }

    @Test
    void equippedCreatureCanUntapAnAttackingCreature() {
        Permanent nouliths = addNoulithsReady(player1);
        Permanent creature = addCreatureReady(player1);
        nouliths.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(1));
        assertThat(creature.isTapped()).isTrue();

        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void attackTriggerCannotTargetNonAttackingCreature() {
        Permanent nouliths = addNoulithsReady(player1);
        Permanent creature = addCreatureReady(player1);
        Permanent bystander = addCreatureReady(player1);
        nouliths.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void attackTriggerCanUntapAnotherAttacker() {
        Permanent nouliths = addNoulithsReady(player1);
        Permanent equipped = addCreatureReady(player1);
        Permanent other = addCreatureReady(player1);
        nouliths.setAttachedTo(equipped.getId());

        declareAttackers(player1, List.of(1, 2));
        harness.handlePermanentChosen(player1, other.getId());
        resolveAllTriggers();

        assertThat(other.isTapped()).isFalse();
        assertThat(equipped.isTapped()).isTrue();
    }

    @Test
    void attackTriggerStillResolvesAfterEquipmentLeaves() {
        Permanent nouliths = addNoulithsReady(player1);
        Permanent creature = addCreatureReady(player1);
        nouliths.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(nouliths);
        resolveAllTriggers();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).doesNotContain(CardSubtype.CLERIC);
    }

    @Test
    void equipMovesBonusesToNewCreatureWithoutCreatingAnotherHero() {
        harness.setHand(player1, List.of(new SagesNouliths()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent nouliths = findPermanent(player1, "Sage's Nouliths");
        Permanent hero = findPermanent(player1, "Hero");
        Permanent creature = addCreatureReady(player1);
        int originalPower = gqs.getEffectivePower(gd, creature);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        resolveAllTriggers();

        assertThat(nouliths.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(originalPower + 1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).contains(CardSubtype.BEAR, CardSubtype.CLERIC);
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero)).contains(CardSubtype.HERO).doesNotContain(CardSubtype.CLERIC);
        assertThat(countPermanents(player1, "Hero")).isEqualTo(1);
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        addNoulithsReady(player1);
        Permanent opponentCreature = addCreatureReady(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipRequiresThreeMana() {
        Permanent nouliths = addNoulithsReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(nouliths.getAttachedTo()).isNull();
    }

    @Test
    void jobSelectCreatesHeroEvenIfEquipmentLeavesBeforeTriggerResolves() {
        harness.setHand(player1, List.of(new SagesNouliths()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent nouliths = findPermanent(player1, "Sage's Nouliths");
        gd.playerBattlefields.get(player1.getId()).remove(nouliths);
        resolveAllTriggers();

        Permanent hero = findPermanent(player1, "Hero");
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero)).contains(CardSubtype.HERO).doesNotContain(CardSubtype.CLERIC);
    }

    private Permanent addNoulithsReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SagesNouliths());
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
