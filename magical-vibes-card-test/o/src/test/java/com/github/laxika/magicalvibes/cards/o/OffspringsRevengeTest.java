package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DemonOfDeathsGate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.NightsquadCommando;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SpriteDragon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OffspringsRevenge.class, DemonOfDeathsGate.class, GrizzlyBears.class,
        HillGiant.class, NightsquadCommando.class, Plains.class, SerraAngel.class, SpriteDragon.class})
class OffspringsRevengeTest extends BaseCardTest {

    @Test
    @DisplayName("Targets only red, white, or black creature cards in the controller's graveyard")
    void targetsMatchingCreatureCards() {
        HillGiant red = new HillGiant();
        SerraAngel white = new SerraAngel();
        DemonOfDeathsGate black = new DemonOfDeathsGate();
        GrizzlyBears green = new GrizzlyBears();
        Plains land = new Plains();
        harness.setGraveyard(player1, new ArrayList<>(List.of(red, white, black, green, land)));
        harness.addToBattlefield(player1, new OffspringsRevenge());

        advanceToCombat(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(red.getId(), white.getId(), black.getId());
    }

    @Test
    @DisplayName("Creates a 1/1 copy and grants it haste until the controller's next turn")
    void createsOneOneCopyWithTemporaryHaste() {
        HillGiant red = new HillGiant();
        harness.setGraveyard(player1, List.of(red));
        harness.addToBattlefield(player1, new OffspringsRevenge());

        advanceToCombat(player1);
        harness.handleMultipleCardsChosen(player1, List.of(red.getId()));
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Hill Giant");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(red);

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger on an opponent's turn")
    void doesNotTriggerOnOpponentsCombat() {
        harness.setGraveyard(player1, List.of(new HillGiant()));
        harness.addToBattlefield(player1, new OffspringsRevenge());

        advanceToCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not allow declining while a legal target exists")
    void targetIsMandatory() {
        HillGiant red = new HillGiant();
        harness.setGraveyard(player1, List.of(red));
        harness.addToBattlefield(player1, new OffspringsRevenge());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must choose 1 cards");
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Copies all colors and preserves printed haste after the temporary grant expires")
    void copiesMulticoloredCreatureWithPrintedHaste() {
        SpriteDragon dragon = new SpriteDragon();
        harness.setGraveyard(player1, List.of(dragon));
        harness.addToBattlefield(player1, new OffspringsRevenge());

        advanceToCombat(player1);
        harness.handleMultipleCardsChosen(player1, List.of(dragon.getId()));
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Sprite Dragon");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectiveColors(gd, token)).containsExactlyInAnyOrder(CardColor.RED, CardColor.BLUE);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(dragon);

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("A copied creature's enters ability triggers in a later combat after attacking")
    void copiedEntersAbilityTriggers() {
        NightsquadCommando commando = new NightsquadCommando();
        harness.setGraveyard(player1, List.of(commando));
        harness.addToBattlefield(player1, new OffspringsRevenge());
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        advanceToCombat(player1);
        harness.handleMultipleCardsChosen(player1, List.of(commando.getId()));
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent copy = findPermanent(player1, "Nightsquad Commando");
        assertThat(copy.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(1);
        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(1);
        Permanent soldier = findPermanent(player1, "Human Soldier");
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Copies printed abilities while replacing power and toughness")
    void copiesAbilitiesOfBlackCreature() {
        DemonOfDeathsGate demon = new DemonOfDeathsGate();
        harness.setGraveyard(player1, List.of(demon));
        harness.addToBattlefield(player1, new OffspringsRevenge());

        advanceToCombat(player1);
        harness.handleMultipleCardsChosen(player1, List.of(demon.getId()));
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Demon of Death's Gate");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Cannot target an opponent's matching creature card")
    void excludesOpponentsGraveyard() {
        HillGiant own = new HillGiant();
        HillGiant opposing = new HillGiant();
        harness.setGraveyard(player1, List.of(own));
        harness.setGraveyard(player2, List.of(opposing));
        harness.addToBattlefield(player1, new OffspringsRevenge());

        advanceToCombat(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(own.getId());
    }

    @Test
    @DisplayName("Creates no token when the selected card leaves the graveyard before resolution")
    void missingTargetCreatesNoToken() {
        HillGiant red = new HillGiant();
        harness.setGraveyard(player1, List.of(red));
        harness.addToBattlefield(player1, new OffspringsRevenge());

        advanceToCombat(player1);
        harness.handleMultipleCardsChosen(player1, List.of(red.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(red);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not request a target or create a token when no legal target exists")
    void noLegalTarget() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Plains()));
        harness.setGraveyard(player2, List.of(new HillGiant()));
        harness.addToBattlefield(player1, new OffspringsRevenge());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }
}
