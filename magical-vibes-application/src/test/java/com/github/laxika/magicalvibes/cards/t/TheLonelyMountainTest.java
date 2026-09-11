package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HeavyMattock;
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

@CardUsed({TheLonelyMountain.class, HeavyMattock.class})
class TheLonelyMountainTest extends BaseCardTest {

    @Test
    void entersTappedWithoutAnEquipment() {
        playMountain();

        assertThat(findPermanent(player1, "The Lonely Mountain").isTapped()).isTrue();
    }

    @Test
    void entersUntappedWithAnEquipmentYouControl() {
        harness.addToBattlefield(player1, new HeavyMattock());
        playMountain();

        assertThat(findPermanent(player1, "The Lonely Mountain").isTapped()).isFalse();
    }

    @Test
    void doesNotCountAnOpponentsEquipment() {
        harness.addToBattlefield(player2, new HeavyMattock());
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
        harness.addToBattlefield(player1, new HeavyMattock());
        harness.addToBattlefield(player1, new HeavyMattock());
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
        harness.addToBattlefield(player1, new HeavyMattock());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void playMountain() {
        harness.setHand(player1, List.of(new TheLonelyMountain()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addReadyMountain(Player player) {
        Permanent permanent = new Permanent(new TheLonelyMountain());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
