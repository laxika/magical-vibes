package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SurveyMechan.class, Forest.class, Island.class, GrizzlyBears.class})
class SurveyMechanTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage and makes the targeted player draw cards and gain life")
    void resolvesBothTargetGroups() {
        addCreatureReady(player1, new SurveyMechan());
        addLand(player1, new Forest());
        addLand(player1, new Island());
        Permanent targetCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player2, 10);
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(targetCreature.getId(), player2.getId()));
        harness.assertInGraveyard(player1, "Survey Mechan");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 13);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore + 3);
        assertThat(gd.playerHands.get(player2.getId()).subList(handSizeBefore,
                gd.playerHands.get(player2.getId()).size())).extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Grizzly Bears", "Grizzly Bears");
    }

    private Permanent addLand(Player player, Card land) {
        Permanent permanent = new Permanent(land);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
