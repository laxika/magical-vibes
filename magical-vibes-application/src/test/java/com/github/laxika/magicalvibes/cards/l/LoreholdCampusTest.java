package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoreholdCampusTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new LoreholdCampus()));
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);

        Permanent campus = findPermanent(player1, "Lorehold Campus");
        assertThat(campus.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping adds red or white mana")
    void tappingAddsChosenMana() {
        Permanent redCampus = addReadyCampus();
        Permanent whiteCampus = addReadyCampus();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(redCampus.isTapped()).isTrue();
        assertThat(whiteCampus.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying four mana and tapping scries one")
    void paidAbilityScriesOne() {
        Permanent campus = new Permanent(new LoreholdCampus());
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
        Permanent campus = new Permanent(new LoreholdCampus());
        campus.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(campus);
        return campus;
    }
}
