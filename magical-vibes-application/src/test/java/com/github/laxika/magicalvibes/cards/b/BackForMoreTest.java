package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.m.MosscoatGoriak;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BackForMore.class, MosscoatGoriak.class, AlmightyBrushwagg.class})
class BackForMoreTest extends BaseCardTest {

    @Test
    void returnsCreatureThenFightsChosenOpponentCreature() {
        MosscoatGoriak returnedCard = new MosscoatGoriak();
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        prepareCast(returnedCard);

        harness.castAndResolveInstant(player1, 0, returnedCard.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(returnedCard.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
    }

    @Test
    void mayChooseNoCreatureForTheReflexiveFight() {
        MosscoatGoriak returnedCard = new MosscoatGoriak();
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        prepareCast(returnedCard);

        harness.castAndResolveInstant(player1, 0, returnedCard.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(returnedCard.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingCreature);
    }

    @Test
    void reflexiveFightCannotTargetYourOwnCreature() {
        MosscoatGoriak returnedCard = new MosscoatGoriak();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        prepareCast(returnedCard);

        harness.castAndResolveInstant(player1, 0, returnedCard.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(ownCreature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsCreatureWithoutAnyOpposingCreatures() {
        MosscoatGoriak returnedCard = new MosscoatGoriak();
        prepareCast(returnedCard);

        harness.castAndResolveInstant(player1, 0, returnedCard.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mosscoat Goriak");
        harness.assertNotInGraveyard(player1, "Mosscoat Goriak");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void missingGraveyardTargetDoesNotReturnOrFight() {
        MosscoatGoriak returnedCard = new MosscoatGoriak();
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        prepareCast(returnedCard);

        harness.castInstant(player1, 0, returnedCard.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mosscoat Goriak");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(opposingCreature.getMarkedDamage()).isZero();
    }

    @Test
    void opponentCanRespondToFightAndDamageUsesCurrentPower() {
        MosscoatGoriak returnedCard = new MosscoatGoriak();
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        prepareCast(returnedCard);

        harness.castAndResolveInstant(player1, 0, returnedCard.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        assertThat(opposingCreature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Mosscoat Goriak");

        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mosscoat Goriak");
        harness.assertNotOnBattlefield(player1, "Mosscoat Goriak");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingCreature);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void noFightDamageIfReturnedCreatureLeavesBeforeFightResolves() {
        MosscoatGoriak returnedCard = new MosscoatGoriak();
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        prepareCast(returnedCard);

        harness.castAndResolveInstant(player1, 0, returnedCard.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getId().equals(returnedCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingCreature);
        assertThat(opposingCreature.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void noFightDamageIfTargetLeavesBeforeFightResolves() {
        MosscoatGoriak returnedCard = new MosscoatGoriak();
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        prepareCast(returnedCard);

        harness.castAndResolveInstant(player1, 0, returnedCard.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        gd.playerBattlefields.get(player2.getId()).remove(opposingCreature);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mosscoat Goriak");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allSatisfy(permanent -> assertThat(permanent.getMarkedDamage()).isZero());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotReturnCreatureFromOpponentsGraveyard() {
        MosscoatGoriak returnedCard = new MosscoatGoriak();
        prepareCast(returnedCard);
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(returnedCard));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, returnedCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotReturnNoncreatureCard() {
        MosscoatGoriak returnedCard = new MosscoatGoriak();
        prepareCast(returnedCard);
        BackForMore noncreature = new BackForMore();
        harness.setGraveyard(player1, List.of(noncreature));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareCast(MosscoatGoriak returnedCard) {
        harness.setGraveyard(player1, List.of(returnedCard));
        harness.setHand(player1, List.of(new BackForMore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
