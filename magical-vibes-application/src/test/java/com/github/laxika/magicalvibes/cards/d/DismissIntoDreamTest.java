package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DismissIntoDreamTest extends BaseCardTest {

    @Test
    @DisplayName("Each creature an opponent controls is an Illusion in addition to its other types")
    void opponentCreaturesBecomeIllusions() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new DismissIntoDream()));

        Permanent bears = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.computeStaticBonus(gd, bears).grantedSubtypes()).contains(CardSubtype.ILLUSION);
        assertThat(bears.getCard().getSubtypes()).contains(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("Creatures its controller controls are unaffected")
    void ownCreaturesAreNotIllusions() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new DismissIntoDream()));

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.computeStaticBonus(gd, bears).grantedSubtypes()).doesNotContain(CardSubtype.ILLUSION);
    }

    @Test
    @DisplayName("Targeting an opponent's creature with a spell sacrifices it")
    void targetedOpponentCreatureIsSacrificed() {
        harness.addToBattlefield(player2, new SerraAngel());
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new DismissIntoDream()));

        Permanent bears = findPermanent(player2, "Serra Angel");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        gs.playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Serra Angel"));
    }

    @Test
    @DisplayName("Targeting your own creature does not sacrifice it")
    void ownTargetedCreatureSurvives() {
        harness.addToBattlefield(player1, new SerraAngel());
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new DismissIntoDream()));

        Permanent angel = findPermanent(player1, "Serra Angel");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        gs.playCard(gd, player1, 0, 0, angel.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Serra Angel")).isNotNull();
    }
}
