package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SzarekhTheSilentKing.class, Forest.class, Ornithopter.class, SkySkiff.class})
class SzarekhTheSilentKingTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking mills three cards and may return a milled artifact creature")
    void attackingReturnsMilledArtifactCreature() {
        addCreatureReady(player1, new SzarekhTheSilentKing());
        Ornithopter milledArtifactCreature = new Ornithopter();
        harness.setLibrary(player1, List.of(milledArtifactCreature, new Forest(), new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(milledArtifactCreature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Attacking may return a milled Vehicle")
    void attackingReturnsMilledVehicle() {
        addCreatureReady(player1, new SzarekhTheSilentKing());
        SkySkiff milledVehicle = new SkySkiff();
        harness.setLibrary(player1, List.of(new Forest(), milledVehicle, new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(milledVehicle);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest", "Forest");
    }

    @Test
    @DisplayName("Attacking does not offer a nonmatching milled card")
    void attackingDoesNotReturnNonmatchingCard() {
        addCreatureReady(player1, new SzarekhTheSilentKing());
        Card milledNonmatchingCard = new Forest();
        harness.setLibrary(player1, List.of(milledNonmatchingCard, new Forest(), new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(milledNonmatchingCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
