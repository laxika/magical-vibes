package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FoulmireKnight;
import com.github.laxika.magicalvibes.cards.g.GorgonsHead;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SyrGwynHeroOfAshvale.class, GorgonsHead.class, GrizzlyBears.class, FoulmireKnight.class})
class SyrGwynHeroOfAshvaleTest extends BaseCardTest {

    @Test
    @DisplayName("An equipped creature you control attacking draws a card and makes you lose 1 life")
    void equippedCreatureAttackingDrawsAndLosesLife() {
        harness.addToBattlefield(player1, new SyrGwynHeroOfAshvale());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new GorgonsHead());
        equipment.setAttachedTo(attacker.getId());
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.setLife(player1, 20);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("An unequipped creature attacking does not trigger the ability")
    void unequippedCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new SyrGwynHeroOfAshvale());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Card remaining = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(remaining));
        harness.setLife(player1, 20);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Syr Gwyn grants your Equipment equip Knight {0}")
    void grantsKnightEquipZero() {
        harness.addToBattlefield(player1, new SyrGwynHeroOfAshvale());
        Permanent knight = addCreatureReady(player1, new FoulmireKnight());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new GorgonsHead());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        int equipmentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(equipment);
        harness.activateAbility(player1, equipmentIndex, 1, null, knight.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(knight.getId());
    }

    @Test
    @DisplayName("Syr Gwyn's granted equip ability cannot target a non-Knight")
    void grantedEquipCannotTargetNonKnight() {
        harness.addToBattlefield(player1, new SyrGwynHeroOfAshvale());
        Permanent nonKnight = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new GorgonsHead());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        int equipmentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(equipment);
        assertThatThrownBy(() -> harness.activateAbility(player1, equipmentIndex, 1, null, nonKnight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Knight");
    }
}
