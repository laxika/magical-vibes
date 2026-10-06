package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SiblingRivalry.class, GrizzlyBears.class, AngelsFeather.class, Pacifism.class})
class SiblingRivalryTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Sibling Rivalry steals and untaps a creature, grants haste, and creates a Powerstone")
    void resolvesOnCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        castSiblingRivalry(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(
                permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.POWERSTONE)
        ).hasSize(1).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Resolving Sibling Rivalry can target an artifact")
    void resolvesOnArtifact() {
        Permanent target = addReadyArtifact(player2);
        target.tap();
        castSiblingRivalry(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Sibling Rivalry's control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castSiblingRivalry(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent enchantment = new Permanent(new Pacifism());
        enchantment.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(enchantment);
        harness.setHand(player1, List.of(new SiblingRivalry()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    @DisplayName("Sibling Rivalry can untap your own creature and still creates a Powerstone")
    void resolvesOnOwnCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();

        castSiblingRivalry(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(countPermanents(player1, "Powerstone")).isEqualTo(1);
        assertThat(findPermanent(player1, "Powerstone").isTapped()).isTrue();
    }

    @Test
    @DisplayName("An illegal target prevents Sibling Rivalry from creating a Powerstone")
    void noPowerstoneWhenTargetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SiblingRivalry()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Powerstone")).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof SiblingRivalry);
    }

    @Test
    @DisplayName("Artifact control and haste expire but the Powerstone remains")
    void artifactEffectsExpireWithoutRemovingPowerstone() {
        Permanent target = addReadyArtifact(player2);
        target.tap();
        castSiblingRivalry(target);

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        Permanent powerstone = findPermanent(player1, "Powerstone");
        assertThat(powerstone.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target).contains(powerstone);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }
    private void castSiblingRivalry(Permanent target) {
        harness.setHand(player1, List.of(new SiblingRivalry()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private Permanent addReadyArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new AngelsFeather());
    }
}
