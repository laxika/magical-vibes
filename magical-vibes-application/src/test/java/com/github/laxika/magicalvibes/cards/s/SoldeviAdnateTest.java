package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BogWraith;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoldeviAdnateTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a black creature adds {B} equal to its mana value")
    void sacrificeBlackCreatureAddsManaValueInBlack() {
        Permanent adnate = addCreatureReady(player1, new SoldeviAdnate());
        addCreatureReady(player1, new BogWraith()); // {3}{B}, mana value 4
        UUID wraith = harness.getPermanentId(player1, "Bog Wraith");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, wraith);

        harness.assertInGraveyard(player1, "Bog Wraith");
        assertThat(adnate.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(4);
    }

    @Test
    @DisplayName("An artifact creature is a legal sacrifice")
    void sacrificeArtifactCreature() {
        addCreatureReady(player1, new SoldeviAdnate());
        addCreatureReady(player1, new GoldMyr()); // colorless artifact creature, mana value 2
        UUID myr = harness.getPermanentId(player1, "Gold Myr");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, myr);

        harness.assertInGraveyard(player1, "Gold Myr");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }

    @Test
    @DisplayName("Soldevi Adnate can sacrifice itself")
    void sacrificesItself() {
        addCreatureReady(player1, new SoldeviAdnate());
        addCreatureReady(player1, new GrizzlyBears()); // green nonartifact, ineligible

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Soldevi Adnate");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature that is neither black nor an artifact cannot be sacrificed")
    void cannotSacrificeIneligibleCreature() {
        addCreatureReady(player1, new SoldeviAdnate());
        addCreatureReady(player1, new BogWraith());
        UUID bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bears))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent adnate = addCreatureReady(player1, new SoldeviAdnate());
        adnate.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
