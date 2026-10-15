package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BardsBow.class, GrizzlyBears.class})
class BardsBowTest extends BaseCardTest {

    @Test
    @DisplayName("Job select creates and equips a Hero Bard")
    void jobSelectCreatesAndEquipsHeroBard() {
        harness.setHand(player1, List.of(new BardsBow()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bow = findPermanent(player1, "Bard's Bow");
        Permanent hero = findPermanent(player1, "Hero");

        assertThat(bow.getAttachedTo()).isEqualTo(hero.getId());
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero))
                .contains(CardSubtype.HERO, CardSubtype.BARD);
        assertThat(gqs.hasKeyword(gd, hero, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Equip moves the Bow and its bonuses")
    void equipMovesBow() {
        Permanent bow = addCreatureReady(player1, new BardsBow());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        bow.setAttachedTo(first.getId());

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(bow.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, first)).doesNotContain(CardSubtype.BARD);
        assertThat(gqs.hasKeyword(gd, first, Keyword.REACH)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, second)).contains(CardSubtype.BARD);
        assertThat(gqs.hasKeyword(gd, second, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Job select waits for its trigger and still creates a Hero if the Bow leaves")
    void jobSelectCreatesHeroAfterBowLeaves() {
        harness.setHand(player1, List.of(new BardsBow()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Hero")).isZero();
        Permanent bow = findPermanent(player1, "Bard's Bow");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, bow));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bard's Bow");
        harness.assertInGraveyard(player1, "Bard's Bow");
        assertThat(countPermanents(player1, "Hero")).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, findPermanent(player1, "Hero"))).isEmpty();
        assertThat(gqs.isToken(gd, findPermanent(player1, "Hero"))).isTrue();
        Permanent hero = findPermanent(player1, "Hero");
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero)).contains(CardSubtype.HERO)
                .doesNotContain(CardSubtype.BARD);
        assertThat(gqs.hasKeyword(gd, hero, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Moving the Bow leaves the original Hero alive without the bonuses")
    void movingBowDoesNotKillHero() {
        harness.setHand(player1, List.of(new BardsBow()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent bow = findPermanent(player1, "Bard's Bow");
        Permanent hero = findPermanent(player1, "Hero");
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(bow.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(findPermanent(player1, "Hero").getId()).isEqualTo(hero.getId());
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero)).contains(CardSubtype.HERO)
                .doesNotContain(CardSubtype.BARD);
        assertThat(gqs.hasKeyword(gd, hero, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipRejectsOpposingCreature() {
        harness.addToBattlefield(player1, new BardsBow());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Equip requires all six mana")
    void equipRejectsInsufficientMana() {
        Permanent bow = harness.addToBattlefieldAndReturn(player1, new BardsBow());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bow.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void equipRejectsCombatTiming() {
        harness.addToBattlefield(player1, new BardsBow());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("An equip target leaving does not detach the Bow from its current creature")
    void equipTargetLeavingKeepsCurrentAttachment() {
        Permanent bow = harness.addToBattlefieldAndReturn(player1, new BardsBow());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        bow.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, second.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, second));
        harness.passBothPriorities();

        assertThat(bow.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, first, Keyword.REACH)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, first)).contains(CardSubtype.BEAR, CardSubtype.BARD);
    }
}
