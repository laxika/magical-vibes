package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PymTechnologies.class)
class PymTechnologiesTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and gains 1 life")
    void entersTappedAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new PymTechnologies()));

        harness.playLand(player1, 0);

        Permanent technologies = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(technologies.isTapped()).isTrue();

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Mana ability adds green mana when green is chosen")
    void manaAbilityAddsGreenMana() {
        Permanent technologies = addReadyTechnologies();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(technologies.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds blue mana when blue is chosen")
    void manaAbilityAddsBlueMana() {
        Permanent technologies = addReadyTechnologies();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(technologies.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Life gain resolves even after the land leaves the battlefield")
    void gainsLifeAfterLandLeaves() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new PymTechnologies()));
        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);

        Permanent technologies = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerGraveyards.get(player1.getId()).add(technologies.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A land can produce mana without waiting a turn")
    void newlyControlledLandCanProduceMana() {
        Permanent technologies = harness.addToBattlefieldAndReturn(player1, new PymTechnologies());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(technologies.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyTechnologies() {
        return addCreatureReady(player1, new PymTechnologies());
    }
}
