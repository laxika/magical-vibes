package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ColossusSteelStalwart;
import com.github.laxika.magicalvibes.cards.j.JennikaBadAppleBigSister;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlackTomCassidy.class, JennikaBadAppleBigSister.class, ColossusSteelStalwart.class})
class BlackTomCassidyTest extends BaseCardTest {

    @Test
    void tappingAddsGreenMana() {
        Permanent blackTom = addCreatureReady(player1, new BlackTomCassidy());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(blackTom.isTapped()).isTrue();
    }

    @Test
    void gainsLifeWhenControllerHasAnotherMutant() {
        Permanent blackTom = addCreatureReady(player1, new BlackTomCassidy());
        addCreatureReady(player1, new JennikaBadAppleBigSister());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(blackTom.isTapped()).isTrue();
    }

    @Test
    void opponentMutantDoesNotGrantLife() {
        addCreatureReady(player1, new BlackTomCassidy());
        addCreatureReady(player2, new JennikaBadAppleBigSister());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void tappedMutantGrantsLifeAndManaAbilityResolvesWithoutUsingStack() {
        addCreatureReady(player1, new BlackTomCassidy());
        Permanent otherMutant = addCreatureReady(player1, new ColossusSteelStalwart());
        otherMutant.setTapped(true);
        int lifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    void severalOtherMutantsGrantOnlyOneLife() {
        addCreatureReady(player1, new BlackTomCassidy());
        addCreatureReady(player1, new ColossusSteelStalwart());
        addCreatureReady(player1, new JennikaBadAppleBigSister());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    void mutantInGraveyardDoesNotGrantLife() {
        addCreatureReady(player1, new BlackTomCassidy());
        harness.setGraveyard(player1, List.of(new ColossusSteelStalwart()));
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }
}
