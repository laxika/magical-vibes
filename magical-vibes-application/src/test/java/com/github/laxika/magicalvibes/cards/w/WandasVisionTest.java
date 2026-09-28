package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WandasVision.class, Forest.class, GrizzlyBears.class, LightningBolt.class})
class WandasVisionTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell exiles until a nonland and offers it for free")
    void secondSpellExilesUntilNonlandAndOffersFreeCast() {
        WandasVision vision = new WandasVision();
        Forest land = new Forest();
        GrizzlyBears creature = new GrizzlyBears();
        setUpVision(vision, land, creature);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land, creature);

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining the free cast leaves the exiled cards in exile")
    void decliningFreeCastLeavesCardsExiled() {
        WandasVision vision = new WandasVision();
        Forest land = new Forest();
        GrizzlyBears creature = new GrizzlyBears();
        setUpVision(vision, land, creature);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land, creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
    }

    private void setUpVision(WandasVision vision, Forest land, GrizzlyBears creature) {
        harness.addToBattlefield(player1, vision);
        harness.setLibrary(player1, List.of(land, creature));
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
