package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValkyriesSwordTest extends BaseCardTest {

    @Test
    @DisplayName("Paying the ETB cost creates and equips a flying, vigilant Angel Warrior")
    void payingEtbCostCreatesAndEquipsAngel() {
        Permanent sword = castSwordWithMana(5, 2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent angel = findPermanents(player1, "Angel Warrior").getFirst();
        assertThat(sword.getAttachedTo()).isEqualTo(angel.getId());
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(5);
    }

    @Test
    @DisplayName("Declining the ETB cost creates no token and leaves the Sword unattached")
    void decliningEtbCostDoesNotCreateAngel() {
        Permanent sword = castSwordWithMana(5, 2);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Angel Warrior")).isEmpty();
        assertThat(sword.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("The ETB ability attaches only the Sword that created it")
    void onlySourceSwordIsAttached() {
        Permanent otherSword = harness.addToBattlefieldAndReturn(player1, new ValkyriesSword());
        castSwordWithMana(5, 2);
        Permanent sword = gd.playerBattlefields.get(player1.getId()).getLast();

        harness.handleMayAbilityChosen(player1, true);

        Permanent angel = findPermanents(player1, "Angel Warrior").getFirst();
        assertThat(sword.getAttachedTo()).isEqualTo(angel.getId());
        assertThat(otherSword.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip {3} attaches Valkyrie's Sword to a creature you control")
    void equipAttachesSword() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new ValkyriesSword());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    private Permanent castSwordWithMana(int colorless, int white) {
        harness.setHand(player1, List.of(new ValkyriesSword()));
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        harness.addMana(player1, ManaColor.WHITE, white);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof ValkyriesSword)
                .findFirst()
                .orElseThrow();
    }
}
