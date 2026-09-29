package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Clue;
import com.github.laxika.magicalvibes.cards.f.Food;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MadameVastra.class, Clue.class, Food.class, GrizzlyBears.class})
class MadameVastraTest extends BaseCardTest {

    @Test
    void partnerWithLetsTheTargetPlayerSearchForJennyFlint() {
        Card jenny = new Card();
        jenny.setName("Jenny Flint");
        harness.setLibrary(player2, List.of(jenny));

        harness.enterBattlefieldAndReturn(player1, new MadameVastra());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).contains(jenny);
    }

    @Test
    void mustBeBlockedIfAble() {
        addCreatureReady(player1, new MadameVastra());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void createsAClueAndFoodWhenACreatureItDamagedDies() {
        addCreatureReady(player1, new MadameVastra());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Food")).hasSize(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
