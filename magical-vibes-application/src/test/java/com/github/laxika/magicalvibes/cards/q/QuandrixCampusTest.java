package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QuandrixCampusTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new QuandrixCampus()));
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);

        Permanent campus = findPermanent(player1, "Quandrix Campus");
        assertThat(campus.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping adds green or blue mana")
    void tappingAddsChosenMana() {
        Permanent greenCampus = addReadyCampus();
        Permanent blueCampus = addReadyCampus();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(greenCampus.isTapped()).isTrue();
        assertThat(blueCampus.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying four mana and tapping scries one")
    void paidAbilityScriesOne() {
        Permanent campus = new Permanent(new QuandrixCampus());
        campus.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(campus);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
        assertThat(campus.isTapped()).isTrue();
    }

    private Permanent addReadyCampus() {
        Permanent campus = new Permanent(new QuandrixCampus());
        campus.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(campus);
        return campus;
    }
}
