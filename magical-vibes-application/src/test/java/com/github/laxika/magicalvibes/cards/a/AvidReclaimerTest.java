package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.n.NissaGenesisMage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AvidReclaimerTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {G} — adds green mana and taps")
    void tapForGreen() {
        Permanent reclaimer = addCreatureReady(player1, new AvidReclaimer());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(reclaimer.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("{T}: Add {U} — adds blue mana and taps")
    void tapForBlue() {
        Permanent reclaimer = addCreatureReady(player1, new AvidReclaimer());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(reclaimer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Gains 2 life when controller has a Nissa planeswalker")
    void gainsLifeWithNissa() {
        Permanent reclaimer = addCreatureReady(player1, new AvidReclaimer());
        addReadyNissa(player1, 5);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(reclaimer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not gain life when opponent controls the Nissa")
    void noLifeGainWhenOpponentHasNissa() {
        Permanent reclaimer = addCreatureReady(player1, new AvidReclaimer());
        addReadyNissa(player2, 5);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    private Permanent addReadyNissa(Player player, int loyalty) {
        NissaGenesisMage card = new NissaGenesisMage();
        Permanent perm = new Permanent(card);
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
