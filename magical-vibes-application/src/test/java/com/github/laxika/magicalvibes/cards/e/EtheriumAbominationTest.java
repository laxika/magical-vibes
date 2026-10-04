package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EtheriumAbomination.class, Terminate.class})
@DisplayName("Etherium Abomination")
class EtheriumAbominationTest extends BaseCardTest {

    @Test
    @DisplayName("Unearth returns Etherium Abomination to the battlefield with haste")
    void unearthReturnsWithHaste() {
        EtheriumAbomination card = new EtheriumAbomination();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Etherium Abomination");
        assertThat(perm.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Etherium Abomination");
    }

    @Test
    @DisplayName("Unearthed Etherium Abomination is exiled at the next end step")
    void unearthExiledAtEndStep() {
        EtheriumAbomination card = new EtheriumAbomination();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.passUntil(TurnStep.END_STEP);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Etherium Abomination");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Etherium Abomination"));
    }

    @Test
    void destroyedUnearthedCreatureIsExiledInsteadOfDying() {
        harness.setGraveyard(player1, List.of(new EtheriumAbomination()));
        addUnearthMana();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Etherium Abomination");
        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, permanent.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Etherium Abomination");
        harness.assertNotInGraveyard(player1, "Etherium Abomination");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(permanent.getCard());
    }

    @Test
    void unearthReturnsOnlyTheActivatedCard() {
        EtheriumAbomination first = new EtheriumAbomination();
        EtheriumAbomination second = new EtheriumAbomination();
        harness.setGraveyard(player1, List.of(first, second));
        addUnearthMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Etherium Abomination").getCard().getId()).isEqualTo(first.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second);
    }

    @Test
    void unearthDoesNothingIfTheSourceLeavesTheGraveyardBeforeResolution() {
        EtheriumAbomination card = new EtheriumAbomination();
        harness.setGraveyard(player1, List.of(card));
        addUnearthMana();
        harness.activateGraveyardAbility(player1, 0);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(card));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Etherium Abomination");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card);
    }

    @Test
    void unearthCannotBeActivatedOutsideAMainPhase() {
        harness.setGraveyard(player1, List.of(new EtheriumAbomination()));
        addUnearthMana();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Etherium Abomination");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unearthCannotBeActivatedDuringAnOpponentsTurn() {
        harness.setGraveyard(player1, List.of(new EtheriumAbomination()));
        addUnearthMana();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Etherium Abomination");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unearthRequiresBothColoredManaCosts() {
        harness.setGraveyard(player1, List.of(new EtheriumAbomination()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Etherium Abomination");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unearthCannotBeActivatedWithANonemptyStack() {
        harness.setGraveyard(player1, List.of(new EtheriumAbomination()));
        addUnearthMana();
        harness.activateGraveyardAbility(player1, 0);
        addUnearthMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
    }

    private void addUnearthMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
