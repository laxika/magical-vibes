package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReturnOfTheMoleMan.class, Forest.class, GrizzlyBears.class, Shock.class})
class ReturnOfTheMoleManTest extends BaseCardTest {

    @Test
    void landfallMayMillTwoCards() {
        harness.addToBattlefield(player1, new ReturnOfTheMoleMan());
        Card first = new GrizzlyBears();
        Card second = new Shock();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void decliningLandfallMillLeavesLibraryUnchanged() {
        harness.addToBattlefield(player1, new ReturnOfTheMoleMan());
        Card card = new GrizzlyBears();
        harness.setLibrary(player1, List.of(card));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void sacrificeCreatesOneMoloidPerPermanentCardInGraveyard() {
        harness.addToBattlefield(player1, new ReturnOfTheMoleMan());
        harness.setGraveyard(player1, List.of(new Forest(), new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Moloid")).hasSize(3);
        harness.assertInGraveyard(player1, "Return of the Mole Man");
    }

    @Test
    void tokenCountUsesGraveyardAtResolutionAndIgnoresOpponentsGraveyard() {
        harness.addToBattlefield(player1, new ReturnOfTheMoleMan());
        harness.setGraveyard(player2, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Return of the Mole Man");
        harness.assertInGraveyard(player1, "Return of the Mole Man");
        assertThat(findPermanents(player1, "Moloid")).isEmpty();
        gd.playerGraveyards.get(player1.getId()).add(new Forest());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Moloid")).hasSize(2);
        assertThat(findPermanents(player2, "Moloid")).isEmpty();
    }

    @Test
    void moloidAttackMayMillOneCard() {
        createSingleMoloid();
        Card first = new Forest();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        findPermanent(player1, "Moloid").setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void moloidAttackMillCanBeDeclined() {
        createSingleMoloid();
        Card card = new Forest();
        harness.setLibrary(player1, List.of(card));
        findPermanent(player1, "Moloid").setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        harness.addToBattlefield(player1, new ReturnOfTheMoleMan());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Return of the Mole Man");
        harness.assertNotInGraveyard(player1, "Return of the Mole Man");
    }

    @Test
    void landfallMillsAvailableCardWhenLibraryHasOnlyOne() {
        harness.addToBattlefield(player1, new ReturnOfTheMoleMan());
        Card card = new GrizzlyBears();
        harness.setLibrary(player1, List.of(card));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
    }

    private void createSingleMoloid() {
        harness.addToBattlefield(player1, new ReturnOfTheMoleMan());
        harness.setGraveyard(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Moloid")).hasSize(1);
    }
}
