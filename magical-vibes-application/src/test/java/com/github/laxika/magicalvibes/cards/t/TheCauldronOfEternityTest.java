package com.github.laxika.magicalvibes.cards.t;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({TheCauldronOfEternity.class, GrizzlyBears.class, Forest.class, Shock.class})
class TheCauldronOfEternityTest extends BaseCardTest {

    @Test
    @DisplayName("Costs two less for each creature card in the controller's graveyard")
    void costReductionCountsCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new TheCauldronOfEternity()));
        harness.addMana(player1, ManaColor.BLACK, 10);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Puts a controlled creature that dies on the bottom of its owner's library")
    void putsControlledCreatureOnOwnersLibraryBottom() {
        Card forest = new Forest();
        harness.setLibrary(player1, new ArrayList<>(List.of(forest)));
        harness.addToBattlefield(player1, new TheCauldronOfEternity());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getCard().getId()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, creature.getCard());
    }

    @Test
    @DisplayName("Puts a controlled creature on the opponent owner's library bottom")
    void putsStolenCreatureOnItsOwnersLibraryBottom() {
        Card forest = new Forest();
        harness.setLibrary(player2, new ArrayList<>(List.of(forest)));
        harness.addToBattlefield(player1, new TheCauldronOfEternity());
        Permanent stolenCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).remove(stolenCreature);
        gd.playerBattlefields.get(player1.getId()).add(stolenCreature);
        gd.stolenCreatures.put(stolenCreature.getId(), player2.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, stolenCreature));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(stolenCreature.getCard().getId()));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(forest, stolenCreature.getCard());
    }

    @Test
    @DisplayName("Reanimates a target creature card and pays the activation costs")
    void reanimatesTargetCreature() {
        Permanent cauldron = harness.addToBattlefieldAndReturn(player1, new TheCauldronOfEternity());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cauldron), 0,
                null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(8);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature card in the graveyard")
    void cannotTargetNoncreatureCard() {
        Permanent cauldron = harness.addToBattlefieldAndReturn(player1, new TheCauldronOfEternity());
        Card instant = new Shock();
        harness.setGraveyard(player1, List.of(instant));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(cauldron), 0,
                null, instant.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature card");
    }
}
