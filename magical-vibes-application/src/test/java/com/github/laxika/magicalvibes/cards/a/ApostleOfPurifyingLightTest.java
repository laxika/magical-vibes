package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApostleOfPurifyingLightTest extends BaseCardTest {

    @Test
    void exilesTargetCardFromOpponentsGraveyard() {
        Permanent apostle = addApostle(player1);
        Card target = new GrizzlyBears();
        Card remaining = new Cancel();
        harness.setGraveyard(player2, List.of(target, remaining));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int apostleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(apostle);
        harness.activateAbility(player1, apostleIndex, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
    }

    @Test
    void exilesTargetCardFromOwnGraveyard() {
        Permanent apostle = addApostle(player1);
        Card target = new Cancel();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int apostleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(apostle);
        harness.activateAbility(player1, apostleIndex, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Cancel");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
    }

    @Test
    void rejectsTargetNotInGraveyard() {
        Permanent apostle = addApostle(player1);
        Card target = new Cancel();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int apostleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(apostle);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, apostleIndex, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fizzlesIfTargetLeavesGraveyardBeforeResolution() {
        Permanent apostle = addApostle(player1);
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int apostleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(apostle);
        harness.activateAbility(player1, apostleIndex, 0, null, target.getId(), Zone.GRAVEYARD);
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(target);
    }

    private Permanent addApostle(Player player) {
        Permanent apostle = new Permanent(new ApostleOfPurifyingLight());
        apostle.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(apostle);
        return apostle;
    }
}
