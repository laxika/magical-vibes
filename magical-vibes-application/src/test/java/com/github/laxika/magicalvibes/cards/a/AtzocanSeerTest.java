package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SunCollaredRaptor;
import com.github.laxika.magicalvibes.cards.s.SunSentinel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AtzocanSeer.class, SunCollaredRaptor.class, SunSentinel.class})
class AtzocanSeerTest extends BaseCardTest {

    @ParameterizedTest
    @ValueSource(strings = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Tapping Atzocan Seer adds one mana of the chosen color")
    void addsManaOfAnyColor(String color) {
        Permanent seer = addReadySeer();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.valueOf(color))).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(seer.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing Atzocan Seer returns a target Dinosaur card from the graveyard")
    void sacrificesToReturnDinosaur() {
        addReadySeer();
        Card dinosaur = new SunCollaredRaptor();
        harness.setGraveyard(player1, List.of(dinosaur));

        harness.activateAbility(player1, 0, 1, null, dinosaur.getId(), Zone.GRAVEYARD);
        harness.assertNotOnBattlefield(player1, "Atzocan Seer");
        harness.assertInGraveyard(player1, "Atzocan Seer");
        harness.assertInGraveyard(player1, "Sun-Collared Raptor");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Sun-Collared Raptor");
        harness.assertNotInGraveyard(player1, "Sun-Collared Raptor");
        harness.assertInGraveyard(player1, "Atzocan Seer");
    }

    @Test
    @DisplayName("Cannot target a non-Dinosaur card in the graveyard")
    void cannotTargetNonDinosaur() {
        addReadySeer();
        Card nonDinosaur = new SunSentinel();
        harness.setGraveyard(player1, List.of(nonDinosaur));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 1, null, nonDinosaur.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Atzocan Seer");
    }

    @Test
    void cannotSacrificeWithoutTarget() {
        addReadySeer();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Atzocan Seer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetOpponentsDinosaur() {
        addReadySeer();
        Card dinosaur = new SunCollaredRaptor();
        harness.setGraveyard(player2, List.of(dinosaur));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 1, null, dinosaur.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Atzocan Seer");
        harness.assertInGraveyard(player2, "Sun-Collared Raptor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canSacrificeWhileTappedAndSummoningSick() {
        Permanent seer = harness.addToBattlefieldAndReturn(player1, new AtzocanSeer());
        seer.setSummoningSick(true);
        seer.setTapped(true);
        Card dinosaur = new SunCollaredRaptor();
        harness.setGraveyard(player1, List.of(dinosaur));

        harness.activateAbility(player1, 0, 1, null, dinosaur.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Sun-Collared Raptor");
        harness.assertInGraveyard(player1, "Atzocan Seer");
    }

    @Test
    void cannotTapForManaWhileSummoningSick() {
        Permanent seer = harness.addToBattlefieldAndReturn(player1, new AtzocanSeer());
        seer.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(seer.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void cannotTapForManaTwice() {
        addReadySeer();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void doesNotReturnTargetThatLeftGraveyard() {
        addReadySeer();
        Card dinosaur = new SunCollaredRaptor();
        harness.setGraveyard(player1, List.of(dinosaur));
        harness.activateAbility(player1, 0, 1, null, dinosaur.getId(), Zone.GRAVEYARD);

        gd.playerGraveyards.get(player1.getId()).remove(dinosaur);
        harness.setExile(player1, List.of(dinosaur));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(dinosaur);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(dinosaur);
        harness.assertInGraveyard(player1, "Atzocan Seer");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadySeer() {
        Permanent seer = harness.addToBattlefieldAndReturn(player1, new AtzocanSeer());
        seer.setSummoningSick(false);
        return seer;
    }
}
