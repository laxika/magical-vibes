package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UniversityCampus.class, Forest.class})
class UniversityCampusTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new UniversityCampus()));
        harness.playLand(player1, 0);

        Permanent campus = findPermanent(player1, "University Campus");
        assertThat(campus.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping adds white or blue mana")
    void tappingAddsChosenMana() {
        Permanent whiteCampus = addReadyCampus();
        Permanent blueCampus = addReadyCampus();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");
        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(whiteCampus.isTapped()).isTrue();
        assertThat(blueCampus.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying four mana and tapping surveils one")
    void paidAbilitySurveilsOne() {
        Permanent campus = new Permanent(new UniversityCampus());
        campus.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(campus);
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(campus.isTapped()).isTrue();
    }

    private Permanent addReadyCampus() {
        Permanent campus = new Permanent(new UniversityCampus());
        campus.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(campus);
        return campus;
    }
}
