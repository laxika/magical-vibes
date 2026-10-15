package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DwarvenMattock;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({TheLonelyMountain.class, DwarvenMattock.class})
class TheLonelyMountainTest extends BaseCardTest {

    @Test
    void entersTappedWithoutAnEquipment() {
        playMountain();

        assertThat(findPermanent(player1, "The Lonely Mountain").isTapped()).isTrue();
    }

    @Test
    void entersUntappedWithAnEquipmentYouControl() {
        harness.addToBattlefield(player1, new DwarvenMattock());
        playMountain();

        assertThat(findPermanent(player1, "The Lonely Mountain").isTapped()).isFalse();
    }

    @Test
    void doesNotCountAnOpponentsEquipment() {
        harness.addToBattlefield(player2, new DwarvenMattock());
        playMountain();

        assertThat(findPermanent(player1, "The Lonely Mountain").isTapped()).isTrue();
    }

    @Test
    void tapsForRedMana() {
        Permanent mountain = addReadyMountain(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(mountain.isTapped()).isTrue();
    }

    @Test
    void equipmentReducesTokenAbilityCostAndCreatesADwarf() {
        Permanent mountain = addReadyMountain(player1);
        harness.addToBattlefield(player1, new DwarvenMattock());
        harness.addToBattlefield(player1, new DwarvenMattock());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(mountain.isTapped()).isTrue();

        harness.passBothPriorities();

        List<Permanent> dwarves = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.DWARF))
                .toList();
        assertThat(dwarves).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, dwarves.getFirst())).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dwarves.getFirst())).isEqualTo(2);
    }

    @Test
    void tokenAbilityRequiresSorcerySpeed() {
        addReadyMountain(player1);
        harness.addToBattlefield(player1, new DwarvenMattock());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tokenAbilityCostsFourGenericAndOneRedWithoutEquipment() {
        Permanent mountain = addReadyMountain(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(mountain.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Dwarf")).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Dwarf")).isEqualTo(1);
        assertThat(findPermanent(player1, "Dwarf").getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(countPermanents(player2, "Dwarf")).isZero();
    }

    @Test
    void opponentsEquipmentDoesNotReduceActivationCost() {
        Permanent mountain = addReadyMountain(player1);
        harness.addToBattlefield(player2, new DwarvenMattock());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mountain.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void excessEquipmentReducesOnlyTheGenericCost() {
        addReadyMountain(player1);
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new DwarvenMattock());
        }
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Dwarf")).isEqualTo(1);
    }

    @Test
    void equipmentCannotRemoveTheRedManaRequirement() {
        Permanent mountain = addReadyMountain(player1);
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new DwarvenMattock());
        }
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mountain.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tokenAbilityCannotBeActivatedDuringOpponentsMainPhase() {
        addReadyMountain(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tokenAbilityCannotBeActivatedWithAnAbilityOnTheStack() {
        Permanent mountain = addReadyMountain(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        mountain.untap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Dwarf")).isEqualTo(1);
    }

    private void playMountain() {
        harness.setHand(player1, List.of(new TheLonelyMountain()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addReadyMountain(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new TheLonelyMountain());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
