package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SylvanAwakening;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HallOfStormGiants.class, Mountain.class, Shock.class, SylvanAwakening.class})
class HallOfStormGiantsTest extends BaseCardTest {

    @Test
    @DisplayName("Hall of Storm Giants enters tapped with two other lands")
    void entersTappedWithTwoOtherLands() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        playHall();

        assertThat(findHall().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Hall of Storm Giants enters untapped with fewer than two other lands")
    void entersUntappedWithFewerThanTwoOtherLands() {
        harness.addToBattlefield(player1, new Mountain());
        playHall();

        assertThat(findHall().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping Hall of Storm Giants produces one blue mana")
    void tappingProducesBlueMana() {
        Permanent hall = addHallReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(hall.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Hall of Storm Giants becomes a 7/7 blue Giant creature and stays a land")
    void animatesAsBlueGiant() {
        Permanent hall = animateHall();

        assertThat(gqs.isCreature(gd, hall)).isTrue();
        assertThat(gqs.isLand(gd, hall)).isTrue();
        assertThat(gqs.getEffectivePower(gd, hall)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, hall)).isEqualTo(7);
        assertThat(gqs.getEffectiveColors(gd, hall)).containsExactly(CardColor.BLUE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hall)).contains(CardSubtype.GIANT);
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they do not pay three mana")
    void wardCountersOpponentSpellWhenPaymentDeclined() {
        Permanent hall = animateHall();
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, hall.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying three mana allows an opponent's spell to target the animated Hall")
    void wardCanBePaid() {
        Permanent hall = animateHall();
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player2, 0, hall.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(hall.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Each resolved animation grants a separate ward instance")
    void repeatedAnimationGrantsSeparateWardInstances() {
        Permanent hall = animateHall();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, hall.getId());

        assertThat(gd.stack).hasSize(3);
    }

    @Test
    @DisplayName("Animation by another spell does not grant Hall ward")
    void externalAnimationDoesNotGrantWard() {
        Permanent hall = addHallReady(player1);
        harness.setHand(player1, List.of(new SylvanAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, hall)).isTrue();
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, hall.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(hall.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Hall's ward does not trigger for its controller's spell")
    void ownSpellDoesNotTriggerWard() {
        Permanent hall = animateHall();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, hall.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(hall.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Hall's animation expires at end of turn")
    void animationExpiresAtEndOfTurn() {
        Permanent hall = animateHall();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isCreature(gd, hall)).isFalse();
        assertThat(gqs.isLand(gd, hall)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, hall)).isEmpty();
        assertThat(gqs.effectiveCreatureSubtypes(gd, hall)).doesNotContain(CardSubtype.GIANT);
    }

    @Test
    @DisplayName("Opponent's lands do not make Hall enter tapped")
    void opponentsLandsDoNotCount() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());

        playHall();

        assertThat(findHall().isTapped()).isFalse();
    }

    private void playHall() {
        harness.setHand(player1, List.of(new HallOfStormGiants()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addHallReady(Player player) {
        Permanent hall = harness.addToBattlefieldAndReturn(player, new HallOfStormGiants());
        hall.setSummoningSick(false);
        return hall;
    }

    private Permanent animateHall() {
        addHallReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        return findHall();
    }

    private void prepareOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent findHall() {
        return findPermanent(player1, "Hall of Storm Giants");
    }
}
