package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BlasphemousAct;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.InteractionOptions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvokeCalamity.class, Shock.class, Divination.class, Opt.class, Mountain.class,
        BlasphemousAct.class, GiantGrowth.class})
class InvokeCalamityTest extends BaseCardTest {

    @Test
    void offersEligibleCardsAcrossHandAndGraveyardWithinTheManaValueLimit() {
        Shock shock = new Shock();
        Opt opt = new Opt();
        BlasphemousAct tooExpensive = new BlasphemousAct();
        Mountain land = new Mountain();
        harness.setHand(player1, List.of(new InvokeCalamity(), shock, tooExpensive, land));
        harness.setGraveyard(player1, List.of(opt));
        addInvokeCalamityMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.InvokeCalamityCastChoice interaction =
                gd.interaction.activeInteraction(PendingInteraction.InvokeCalamityCastChoice.class);
        assertThat(interaction.validCardIds()).containsExactly(shock.getId(), opt.getId());
        InteractionOptions.MultiCardPick options =
                (InteractionOptions.MultiCardPick) interaction.legalOptions();
        assertThat(options.minCount()).isZero();
        assertThat(options.maxCount()).isEqualTo(2);
    }

    @Test
    void selectedSpellsAreCastFromTheirOriginalZonesAndExiledAfterward() {
        Shock shock = new Shock();
        Divination divination = new Divination();
        InvokeCalamity invokeCalamity = new InvokeCalamity();
        harness.setHand(player1, List.of(invokeCalamity, shock));
        harness.setGraveyard(player1, List.of(divination));
        addInvokeCalamityMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId(), divination.getId()));

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(shock, divination, invokeCalamity);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .doesNotContain(shock, divination);
    }

    @Test
    void selectedGraveyardSpellWithNoLegalTargetsIsExiledOnce() {
        GiantGrowth growth = new GiantGrowth();
        InvokeCalamity invokeCalamity = new InvokeCalamity();
        harness.setHand(player1, List.of(invokeCalamity));
        harness.setGraveyard(player1, List.of(growth));
        addInvokeCalamityMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(growth.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(growth, invokeCalamity);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(growth);
    }

    private void addInvokeCalamityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 4);
    }
}
