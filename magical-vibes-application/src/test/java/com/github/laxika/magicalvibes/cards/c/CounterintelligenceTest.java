package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Counterintelligence.class, ShuFootSoldiers.class, Mountain.class})
class CounterintelligenceTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Returns two target creatures to their owners' hands")
    void bouncesTwoCreatures() {
        Permanent a = harness.addToBattlefieldAndReturn(player2, new ShuFootSoldiers());
        Permanent b = harness.addToBattlefieldAndReturn(player2, new ShuFootSoldiers());
        harness.setHand(player1, List.of(new Counterintelligence()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(a.getId(), b.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Shu Foot Soldiers"))
                .hasSize(2);
    }

    @Test
    @DisplayName("May return only one target creature")
    void bouncesOneCreature() {
        Permanent soldiers = harness.addToBattlefieldAndReturn(player2, new ShuFootSoldiers());
        harness.setHand(player1, List.of(new Counterintelligence()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(soldiers.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInHand(player2, "Shu Foot Soldiers");
    }

    @Test
    @DisplayName("Can target a creature controlled by the caster")
    void bouncesCreatureYouControl() {
        Permanent soldiers = harness.addToBattlefieldAndReturn(player1, new ShuFootSoldiers());
        harness.setHand(player1, List.of(new Counterintelligence()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(soldiers.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Shu Foot Soldiers");
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new Counterintelligence()));
        giveMana();

        UUID mountainId = mountain.getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(mountainId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
