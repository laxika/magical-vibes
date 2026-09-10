package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfestationExpert.class, InfestedWerewolf.class})
class InfestationExpertTest extends BaseCardTest {

    @Test
    void frontFaceCreatesOneInsectWhenItEnters() {
        castInfestationExpert();

        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);
    }

    @Test
    void frontFaceCreatesOneInsectWhenItAttacks() {
        Permanent expert = addCreatureReady(player1, new InfestationExpert());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(expert)));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);
    }

    @Test
    void entersAsBackFaceAtNightAndCreatesTwoInsects() {
        gd.dayNight = DayNight.NIGHT;

        castInfestationExpert();

        Permanent expert = findPermanent(player1, "Infested Werewolf");
        assertThat(expert.isTransformed()).isTrue();
        assertThat(countPermanents(player1, "Insect")).isEqualTo(2);
    }

    @Test
    void backFaceCreatesTwoInsectsWhenItAttacks() {
        Permanent werewolf = addTransformedExpert(player1);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(werewolf)));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Insect")).isEqualTo(2);
    }

    private void castInfestationExpert() {
        harness.setHand(player1, List.of(new InfestationExpert()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addTransformedExpert(Player player) {
        InfestationExpert card = new InfestationExpert();
        Permanent werewolf = addCreatureReady(player, card);
        werewolf.setCard(card.getBackFaceCard());
        werewolf.setTransformed(true);
        return werewolf;
    }
}
