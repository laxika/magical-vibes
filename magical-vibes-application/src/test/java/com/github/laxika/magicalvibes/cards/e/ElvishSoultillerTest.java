package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElvishSoultiller.class, ElvishWarrior.class, GrizzlyBears.class, AvianChangeling.class,
        WrathOfGod.class})
class ElvishSoultillerTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, it shuffles only creature cards of the chosen type from its graveyard")
    void shufflesOnlyChosenTypeFromOwnGraveyard() {
        Card soultiller = new ElvishSoultiller();
        Card elf = new ElvishWarrior();
        Card bear = new GrizzlyBears();
        harness.addToBattlefield(player1, soultiller);
        harness.setGraveyard(player1, List.of(elf, bear));

        destroySoultiller();
        harness.handleListChoice(player1, "ELF");

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .contains(soultiller.getId(), elf.getId())
                .doesNotContain(bear.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(bear.getId())
                .doesNotContain(elf.getId(), soultiller.getId());
    }

    @Test
    @DisplayName("Changeling creature cards match the chosen type and an opponent's graveyard is untouched")
    void changelingMatchesChosenTypeAndOpponentGraveyardStays() {
        Card changeling = new AvianChangeling();
        Card opponentBear = new GrizzlyBears();
        harness.addToBattlefield(player1, new ElvishSoultiller());
        harness.setGraveyard(player1, List.of(changeling));
        harness.setGraveyard(player2, List.of(opponentBear));

        destroySoultiller();
        harness.handleListChoice(player1, "BEAR");

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .contains(changeling.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .contains(opponentBear.getId());
    }

    private void destroySoultiller() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
