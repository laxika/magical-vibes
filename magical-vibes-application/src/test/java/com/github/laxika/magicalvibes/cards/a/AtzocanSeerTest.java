package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FrenziedRaptor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AtzocanSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Atzocan Seer adds one mana of the chosen color")
    void addsManaOfAnyColor() {
        Permanent seer = addReadySeer();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(seer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing Atzocan Seer returns a target Dinosaur card from the graveyard")
    void sacrificesToReturnDinosaur() {
        addReadySeer();
        Card dinosaur = new FrenziedRaptor();
        harness.setGraveyard(player1, List.of(dinosaur));

        harness.activateAbility(player1, 0, 1, null, dinosaur.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Frenzied Raptor");
        harness.assertNotInGraveyard(player1, "Frenzied Raptor");
        harness.assertInGraveyard(player1, "Atzocan Seer");
    }

    @Test
    @DisplayName("Cannot target a non-Dinosaur card in the graveyard")
    void cannotTargetNonDinosaur() {
        addReadySeer();
        Card nonDinosaur = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonDinosaur));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 1, null, nonDinosaur.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Atzocan Seer");
    }

    private Permanent addReadySeer() {
        Permanent seer = new Permanent(new AtzocanSeer());
        seer.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(seer);
        return seer;
    }
}
