package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MerrowLevitatorTest extends BaseCardTest {

    @Test
    @DisplayName("{T} ability grants flying to the target creature")
    void grantsFlyingToTarget() {
        addCreatureReady(player1, new MerrowLevitator());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("{T} ability targeting a non-creature is rejected")
    void illegalTargetRejected() {
        addCreatureReady(player1, new MerrowLevitator());
        Permanent forest = addCreatureReady(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting a blue spell lets the controller untap Merrow Levitator")
    void untapsWhenCastingBlueSpell() {
        Permanent levitator = addCreatureReady(player1, new MerrowLevitator());
        levitator.tap();
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(levitator.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the may ability leaves Merrow Levitator tapped")
    void staysTappedWhenDeclining() {
        Permanent levitator = addCreatureReady(player1, new MerrowLevitator());
        levitator.tap();
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(levitator.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a non-blue spell does not trigger the untap")
    void nonBlueSpellDoesNotTrigger() {
        Permanent levitator = addCreatureReady(player1, new MerrowLevitator());
        levitator.tap();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(com.github.laxika.magicalvibes.model.PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(levitator.isTapped()).isTrue();
    }
}
