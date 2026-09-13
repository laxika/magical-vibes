package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrownedCatacomb;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmolderingMarsh.class, Island.class, Swamp.class, DrownedCatacomb.class})
class SmolderingMarshTest extends BaseCardTest {

    @Test
    void entersTappedWithFewerThanTwoBasicLands() {
        harness.addToBattlefield(player1, new Island());

        playSmolderingMarsh();

        assertThat(findSmolderingMarsh(player1).isTapped()).isTrue();
    }

    @Test
    void entersUntappedWithTwoBasicLands() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());

        playSmolderingMarsh();

        assertThat(findSmolderingMarsh(player1).isTapped()).isFalse();
    }

    @Test
    void nonbasicLandsDoNotCount() {
        harness.addToBattlefield(player1, new DrownedCatacomb());
        harness.addToBattlefield(player1, new DrownedCatacomb());

        playSmolderingMarsh();

        assertThat(findSmolderingMarsh(player1).isTapped()).isTrue();
    }

    @Test
    void opponentsBasicLandsDoNotCount() {
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Swamp());

        playSmolderingMarsh();

        assertThat(findSmolderingMarsh(player1).isTapped()).isTrue();
    }

    @Test
    void tappingProducesBlackMana() {
        addReadySmolderingMarsh(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void tappingProducesRedMana() {
        addReadySmolderingMarsh(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    private void playSmolderingMarsh() {
        harness.setHand(player1, List.of(new SmolderingMarsh()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addReadySmolderingMarsh(Player player) {
        Permanent permanent = new Permanent(new SmolderingMarsh());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent findSmolderingMarsh(Player player) {
        return findPermanent(player, "Smoldering Marsh");
    }
}
