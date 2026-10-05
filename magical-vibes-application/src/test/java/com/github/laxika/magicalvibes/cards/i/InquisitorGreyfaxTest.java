package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InquisitorGreyfax.class, GrizzlyBears.class})
class InquisitorGreyfaxTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control get +1/+0 and vigilance")
    void boostsOtherCreaturesAndGrantsVigilance() {
        Permanent greyfax = addCreatureReady(player1, new InquisitorGreyfax());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, greyfax)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Pays one mana and taps an opposing creature to investigate")
    void tapsOpposingCreatureAndInvestigates() {
        Permanent greyfax = addCreatureReady(player1, new InquisitorGreyfax());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(greyfax.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        addCreatureReady(player1, new InquisitorGreyfax());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void investigatesEvenWhenTargetIsAlreadyTapped() {
        addCreatureReady(player1, new InquisitorGreyfax());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void doesNotInvestigateWhenOnlyTargetLeavesBattlefield() {
        Permanent greyfax = addCreatureReady(player1, new InquisitorGreyfax());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(greyfax.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void abilityResolvesAfterGreyfaxLeavesBattlefield() {
        Permanent greyfax = addCreatureReady(player1, new InquisitorGreyfax());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(greyfax);
        gd.playerGraveyards.get(player1.getId()).add(greyfax.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void staticBonusesEndWhenGreyfaxLeavesBattlefield() {
        Permanent greyfax = addCreatureReady(player1, new InquisitorGreyfax());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(greyfax);
        gd.playerGraveyards.get(player1.getId()).add(greyfax.getCard());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void cannotActivateWithoutMana() {
        Permanent greyfax = addCreatureReady(player1, new InquisitorGreyfax());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(greyfax.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new InquisitorGreyfax());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotInvestigateWhenTargetChangesToYourControl() {
        addCreatureReady(player1, new InquisitorGreyfax());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void greyfaxAndBoostedCreatureAttackWithoutTapping() {
        Permanent greyfax = addCreatureReady(player1, new InquisitorGreyfax());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThat(greyfax.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWhenAlreadyTapped() {
        Permanent greyfax = addCreatureReady(player1, new InquisitorGreyfax());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        greyfax.tap();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void clueCanBeSacrificedForTwoManaToDraw() {
        addCreatureReady(player1, new InquisitorGreyfax());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        Permanent clue = findPermanent(player1, "Clue");
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
