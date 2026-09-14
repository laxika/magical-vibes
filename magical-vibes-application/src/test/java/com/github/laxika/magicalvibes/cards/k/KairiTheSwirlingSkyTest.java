package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.y.YavimayaWurm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KairiTheSwirlingSky.class, GrizzlyBears.class, HillGiant.class, MindStone.class,
        Shock.class, YavimayaWurm.class})
class KairiTheSwirlingSkyTest extends BaseCardTest {

    private static final String RETURN_MODE =
            "Return any number of target nonland permanents with total mana value 6 or less to their owners' hands";
    private static final String MILL_MODE =
            "Mill six cards, then return up to two instant and/or sorcery cards from your graveyard to your hand";

    @Test
    void deathTriggerReturnsTargetedPermanentsWithinTotalManaValue() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent highManaValue = harness.addToBattlefieldAndReturn(player2, new YavimayaWurm());
        harness.addToBattlefield(player1, new KairiTheSwirlingSky());

        killKairi();
        harness.handleListChoice(player1, RETURN_MODE);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(bears.getId(), giant.getId(), stone.getId(), highManaValue.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(giant.getId(), stone.getId())
                .doesNotContain(highManaValue.getId());

        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(permanent -> permanent.getId())
                .doesNotContain(bears.getId(), giant.getId()).contains(highManaValue.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(permanent -> permanent.getId())
                .contains(stone.getId());
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getId)
                .contains(bears.getCard().getId(), giant.getCard().getId());
    }

    @Test
    void deathTriggerMillsThenReturnsUpToTwoInstantOrSorceryCards() {
        Card firstShock = new Shock();
        Card secondShock = new Shock();
        harness.setLibrary(player1, List.of(
                firstShock, secondShock,
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new KairiTheSwirlingSky());

        killKairi();
        harness.handleListChoice(player1, MILL_MODE);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(firstShock));
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(secondShock));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(firstShock.getId(), secondShock.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).filteredOn(card -> card instanceof GrizzlyBears)
                .hasSize(4);
    }

    private void killKairi() {
        Permanent kairi = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof KairiTheSwirlingSky)
                .findFirst()
                .orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, kairi));
        harness.passBothPriorities();
    }
}
