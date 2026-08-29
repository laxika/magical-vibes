package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeedOfHope.class, Forest.class, LightningBolt.class})
class SeedOfHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Mills two cards, may return a milled permanent, and gains 2 life")
    void millsReturnsPermanentAndGainsLife() {
        harness.setLife(player1, 20);
        setTopCards(new Forest(), new LightningBolt());

        castAndResolve();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Gains 2 life when the optional return is declined")
    void decliningReturnStillGainsLife() {
        harness.setLife(player1, 20);
        setTopCards(new Forest(), new LightningBolt());

        castAndResolve();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Gains 2 life without offering a nonpermanent card")
    void noPermanentMilled() {
        harness.setLife(player1, 20);
        setTopCards(new LightningBolt(), new LightningBolt());

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        harness.assertInGraveyard(player1, "Lightning Bolt");
    }

    private void castAndResolve() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SeedOfHope()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void setTopCards(com.github.laxika.magicalvibes.model.Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
