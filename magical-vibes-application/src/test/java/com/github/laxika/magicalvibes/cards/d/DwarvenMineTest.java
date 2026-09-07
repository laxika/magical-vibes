package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({DwarvenMine.class, Mountain.class})
class DwarvenMineTest extends BaseCardTest {

    @Test
    void entersUntappedAndCreatesDwarfWithFewerThanThreeOtherMountains() {
        addMountain(player1);
        addMountain(player1);

        playMine();

        assertThat(findMine(player1).isTapped()).isFalse();
        harness.passBothPriorities();

        assertThat(dwarfTokens(player1)).hasSize(1);
    }

    @Test
    void entersTappedAndDoesNotCreateDwarfWithThreeOtherMountains() {
        addMountain(player1);
        addMountain(player1);
        addMountain(player1);

        playMine();

        assertThat(findMine(player1).isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(dwarfTokens(player1)).isEmpty();
    }

    @Test
    void createsDwarfEvenIfItIsTappedBeforeTheTriggerResolves() {
        playMine();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(dwarfTokens(player1)).hasSize(1);
    }

    private void playMine() {
        harness.setHand(player1, List.of(new DwarvenMine()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private void addMountain(Player player) {
        gd.playerBattlefields.get(player.getId()).add(new Permanent(new Mountain()));
    }

    private Permanent findMine(Player player) {
        return findPermanent(player, "Dwarven Mine");
    }

    private List<Permanent> dwarfTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.DWARF))
                .toList();
    }
}
