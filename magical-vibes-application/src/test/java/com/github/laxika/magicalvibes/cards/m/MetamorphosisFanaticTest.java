package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MetamorphosisFanatic.class, GrizzlyBears.class, Forest.class, Solemnity.class, DoublingSeason.class})
class MetamorphosisFanaticTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns up to one target creature with a lifelink counter")
    void etbReturnsCreatureWithLifelinkCounter() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        castFanaticNormally();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The optional ETB return can be declined")
    void etbReturnCanBeDeclined() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castFanaticNormally();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ETB cannot target a noncreature card")
    void etbCannotTargetNoncreature() {
        harness.setGraveyard(player1, List.of(new Forest()));
        castFanaticNormally();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Miracle casting the creature still resolves its reanimation ETB")
    void miracleCastResolvesEtb() {
        Card creature = new GrizzlyBears();
        MetamorphosisFanatic fanatic = new MetamorphosisFanatic();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(fanatic));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true); // reveal

        harness.passBothPriorities(); // resolve miracle trigger -> cast prompt
        harness.handleMayAbilityChosen(player1, true); // cast for miracle cost
        harness.passBothPriorities(); // resolve creature -> ETB target prompt

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Solemnity prevents the returned creature from receiving a lifelink counter")
    void counterPlacementRespectsSolemnity() {
        harness.addToBattlefield(player2, new Solemnity());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        castFanaticNormally();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getCounterCount(CounterType.LIFELINK)).isZero();
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Doubling Season doubles the lifelink counter on the returned creature")
    void counterPlacementRespectsDoublingSeason() {
        harness.addToBattlefield(player1, new DoublingSeason());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        castFanaticNormally();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getCounterCount(CounterType.LIFELINK)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ETB offers only creatures from its controller's graveyard")
    void etbCannotTargetOpponentsGraveyard() {
        Card ownCreature = new GrizzlyBears();
        Card opposingCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        castFanaticNormally();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(ownCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ETB resolves without a target when the graveyard is empty")
    void etbWithEmptyGraveyard() {
        harness.setGraveyard(player1, List.of());
        castFanaticNormally();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Metamorphosis Fanatic");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Drawing Fanatic as the second card of a turn does not offer miracle")
    void secondDrawDoesNotOfferMiracle() {
        harness.setLibrary(player1, List.of(new Forest(), new MetamorphosisFanatic()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));

        harness.assertInHand(player1, "Metamorphosis Fanatic");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An ETB target that leaves the graveyard is not returned or replaced")
    void targetLeavesGraveyardBeforeResolution() {
        Card target = new GrizzlyBears();
        Card otherCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target, otherCreature));
        castFanaticNormally();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.setGraveyard(player1, List.of(otherCreature));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherCreature);
        assertThat(gd.stack).isEmpty();
    }

    private void castFanaticNormally() {
        harness.castFromHand(player1, new MetamorphosisFanatic(), "{4}{B}{B}");
        harness.passBothPriorities();
    }
}
