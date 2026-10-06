package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CaptainSisay;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RobaranMercenaries.class, CaptainSisay.class, ProdigalPyromancer.class})
class RobaranMercenariesTest extends BaseCardTest {

    @Test
    @DisplayName("Gains activated abilities from legendary creatures you control")
    void gainsLegendaryCreatureAbilities() {
        Permanent robaran = addCreatureReady(player1, new RobaranMercenaries());
        addCreatureReady(player1, new CaptainSisay());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(robaran.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not gain activated abilities from nonlegendary creatures")
    void ignoresNonlegendaryCreatures() {
        addCreatureReady(player1, new RobaranMercenaries());
        addCreatureReady(player1, new ProdigalPyromancer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not gain activated abilities from legendary creatures an opponent controls")
    void ignoresOpponentsLegendaryCreatures() {
        addCreatureReady(player1, new RobaranMercenaries());
        addCreatureReady(player2, new CaptainSisay());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The gained ability resolves for Robaran's controller without tapping its donor")
    void resolvesGainedSearchAbility() {
        Permanent robaran = addCreatureReady(player1, new RobaranMercenaries());
        Permanent sisay = addCreatureReady(player1, new CaptainSisay());
        harness.setLibrary(player1, List.of(new CaptainSisay(), new ProdigalPyromancer()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Captain Sisay");
        harness.assertNotInHand(player1, "Prodigal Pyromancer");
        assertThat(robaran.isTapped()).isTrue();
        assertThat(sisay.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Loses access to a donor's ability when that donor leaves the battlefield")
    void losesAbilityWhenDonorLeaves() {
        addCreatureReady(player1, new RobaranMercenaries());
        Permanent sisay = addCreatureReady(player1, new CaptainSisay());
        gd.playerBattlefields.get(player1.getId()).remove(sisay);
        harness.setGraveyard(player1, List.of(sisay.getCard()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An activated ability still resolves after its donor leaves the battlefield")
    void activatedAbilitySurvivesDonorLeaving() {
        addCreatureReady(player1, new RobaranMercenaries());
        Permanent sisay = addCreatureReady(player1, new CaptainSisay());
        harness.setLibrary(player1, List.of(new CaptainSisay()));
        harness.activateAbility(player1, 0, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(sisay);
        harness.setGraveyard(player1, List.of(sisay.getCard()));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Captain Sisay");
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the gained tap cost")
    void cannotActivateGainedTapAbilityWhileSummoningSick() {
        harness.addToBattlefield(player1, new RobaranMercenaries());
        addCreatureReady(player1, new CaptainSisay());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
