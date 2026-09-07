package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HadaFreeblade;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DranasChosen.class, HadaFreeblade.class, GrizzlyBears.class})
class DranasChosenTest extends BaseCardTest {

    @Test
    @DisplayName("Cohort taps an Ally and creates a tapped Zombie")
    void cohortTapsAnAllyAndCreatesTappedZombie() {
        Permanent chosen = addCreatureReady(player1, new DranasChosen());
        Permanent ally = addCreatureReady(player1, new HadaFreeblade());

        harness.activateAbility(player1, battlefieldIndex(chosen), 0, null, null);
        harness.passBothPriorities();

        assertThat(chosen.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
        List<Permanent> zombies = findPermanents(player1, "Zombie").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(zombies).hasSize(1);
        assertThat(zombies.getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cohort requires another untapped Ally")
    void cohortRequiresAnotherUntappedAlly() {
        Permanent chosen = addCreatureReady(player1, new DranasChosen());
        Permanent nonAlly = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(chosen), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(chosen.isTapped()).isFalse();
        assertThat(nonAlly.isTapped()).isFalse();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
