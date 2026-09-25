package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({IconicShield.class, GrizzlyBears.class})
class IconicShieldTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+2 and indestructible")
    void equippedCreatureGetsBoostAndIndestructible() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent shield = addReady(player1, new IconicShield());
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Attacking gives another attacking creature indestructible until end of turn")
    void attackingProtectsAnotherAttacker() {
        Permanent equippedCreature = addReady(player1, new GrizzlyBears());
        Permanent otherAttacker = addReady(player1, new GrizzlyBears());
        Permanent shield = addReady(player1, new IconicShield());
        shield.setAttachedTo(equippedCreature.getId());

        declareAttackers(List.of(0, 1));

        harness.handlePermanentChosen(player1, otherAttacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, otherAttacker, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, otherAttacker, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target the equipped creature")
    void cannotTargetEquippedCreature() {
        Permanent equippedCreature = addReady(player1, new GrizzlyBears());
        addReady(player1, new GrizzlyBears());
        Permanent shield = addReady(player1, new IconicShield());
        shield.setAttachedTo(equippedCreature.getId());

        declareAttackers(List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, equippedCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        Permanent equippedCreature = addReady(player1, new GrizzlyBears());
        addReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addReady(player1, new GrizzlyBears());
        Permanent shield = addReady(player1, new IconicShield());
        shield.setAttachedTo(equippedCreature.getId());

        declareAttackers(List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
