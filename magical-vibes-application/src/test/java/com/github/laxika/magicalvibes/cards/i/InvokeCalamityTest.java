package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BlasphemousAct;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.c.CatharticReunion;
import com.github.laxika.magicalvibes.cards.f.FireIce;
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
import static org.assertj.core.api.Assertions.assertThatCode;

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
    void selectedGraveyardSpellWithNoLegalTargetsRemainsInGraveyard() {
        GiantGrowth growth = new GiantGrowth();
        InvokeCalamity invokeCalamity = new InvokeCalamity();
        harness.setGraveyard(player1, List.of(growth));
        harness.castFromHand(player1, invokeCalamity, "{1}{R}{R}{R}{R}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(growth.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(invokeCalamity);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(growth);
    }

    @Test
    void mayChooseNoSpellsAndStillExilesInvokeCalamity() {
        Shock shock = new Shock();
        InvokeCalamity calamity = new InvokeCalamity();
        harness.setGraveyard(player1, List.of(shock));
        harness.castFromHand(player1, calamity, "{1}{R}{R}{R}{R}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(calamity);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shock);
    }

    @Test
    @CardUsed({FireIce.class})
    void splitSpellBudgetUsesTheChosenHalfRatherThanBothHalves() {
        FireIce fireIce = new FireIce();
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(fireIce, divination));
        harness.castFromHand(player1, new InvokeCalamity(), "{1}{R}{R}{R}{R}");
        harness.passBothPriorities();

        assertThatCode(() -> harness.handleMultipleCardsChosen(player1,
                List.of(fireIce.getId(), divination.getId()))).doesNotThrowAnyException();
    }

    @Test
    void selectedHandSpellWithNoLegalTargetsRemainsInHand() {
        GiantGrowth growth = new GiantGrowth();
        InvokeCalamity calamity = new InvokeCalamity();
        harness.setHand(player1, List.of(calamity, growth));
        addInvokeCalamityMana();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(growth.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(growth);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(calamity);
    }

    @Test
    void canCastTwoSorceriesWithTotalManaValueExactlySix() {
        Divination first = new Divination();
        Divination second = new Divination();
        InvokeCalamity calamity = new InvokeCalamity();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain(), new Mountain(), new Mountain()));
        harness.castFromHand(player1, calamity, "{1}{R}{R}{R}{R}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(calamity, first, second);
    }

    @Test
    @CardUsed({CatharticReunion.class})
    void graveyardSpellMustPayItsMandatoryDiscardCostBeforeBeingCast() {
        CatharticReunion reunion = new CatharticReunion();
        Mountain first = new Mountain();
        Mountain second = new Mountain();
        harness.setHand(player1, List.of(new InvokeCalamity(), first, second));
        harness.setGraveyard(player1, List.of(reunion));
        addInvokeCalamityMana();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(reunion.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == reunion);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    private void addInvokeCalamityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 4);
    }
}
