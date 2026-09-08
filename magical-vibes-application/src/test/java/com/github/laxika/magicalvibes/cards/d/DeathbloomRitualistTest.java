package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeathbloomRitualistTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one mana of the chosen color for each creature card in its controller's graveyard")
    void addsManaPerCreatureCardInControllersGraveyard() {
        Permanent ritualist = addReadyRitualist();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HillGiant(), new Mountain()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(ritualist.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ignores noncreature cards and cards in an opponent's graveyard")
    void ignoresNoncreatureAndOpponentGraveyardCards() {
        addReadyRitualist();
        harness.setGraveyard(player1, List.of(new Mountain()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new HillGiant()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    private Permanent addReadyRitualist() {
        Permanent ritualist = new Permanent(new DeathbloomRitualist());
        ritualist.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ritualist);
        return ritualist;
    }
}
