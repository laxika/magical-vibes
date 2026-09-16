package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvenBrigadier;
import com.github.laxika.magicalvibes.cards.b.BarkhideMauler;
import com.github.laxika.magicalvibes.cards.d.DaruCavalier;
import com.github.laxika.magicalvibes.cards.s.ScreamingSeahawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrypticGateway.class, BarkhideMauler.class, AvenBrigadier.class, DaruCavalier.class,
        ScreamingSeahawk.class})
class CrypticGatewayTest extends BaseCardTest {

    @Test
    @DisplayName("Taps two creatures that need not share a type and only offers a creature sharing with both")
    void tapsTwoCreaturesAndFiltersHandByBothTypes() {
        Permanent gateway = harness.addToBattlefieldAndReturn(player1, new CrypticGateway());
        Permanent bird = addCreatureReady(player1, new ScreamingSeahawk());
        Permanent soldier = addCreatureReady(player1, new DaruCavalier());
        Card invalidCreature = new BarkhideMauler();
        Card nonCreature = new CrypticGateway();
        Card validCreature = new AvenBrigadier();
        harness.setHand(player1, List.of(invalidCreature, nonCreature, validCreature));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gateway.isTapped()).isFalse();
        assertThat(bird.isTapped()).isTrue();
        assertThat(soldier.isTapped()).isTrue();

        harness.passBothPriorities();
        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(((PendingInteraction.HandChoice) gameData.interaction.activeInteraction()).validIndices())
                .containsExactly(2);
        harness.handleCardChosen(player1, 2);

        harness.assertOnBattlefield(player1, validCreature.getName());
        assertThat(gameData.playerHands.get(player1.getId())).containsExactly(invalidCreature, nonCreature);
    }

    @Test
    @DisplayName("Only two untapped creatures controlled by the player can pay the cost")
    void costOnlyUsesTwoUntappedControlledCreatures() {
        harness.addToBattlefieldAndReturn(player1, new CrypticGateway());
        Permanent first = addCreatureReady(player1, new ScreamingSeahawk());
        Permanent second = addCreatureReady(player1, new DaruCavalier());
        Permanent extra = addCreatureReady(player1, new BarkhideMauler());
        Permanent tapped = addCreatureReady(player1, new BarkhideMauler());
        tapped.tap();
        Permanent opponentCreature = addCreatureReady(player2, new BarkhideMauler());
        harness.setHand(player1, List.of(new AvenBrigadier()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(((PendingInteraction.PermanentChoice) gd.interaction.activeInteraction()).validIds())
                .containsExactly(first.getId(), second.getId(), extra.getId());
        harness.handlePermanentChosen(player1, first.getId());
        assertThat(((PendingInteraction.PermanentChoice) gd.interaction.activeInteraction()).validIds())
                .containsExactly(second.getId(), extra.getId());
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(extra.isTapped()).isFalse();
        assertThat(tapped.isTapped()).isTrue();
        assertThat(opponentCreature.isTapped()).isFalse();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Declining the may choice leaves the tapped creatures tapped and puts nothing onto the battlefield")
    void decliningMayDoesNotPutCreatureOntoBattlefield() {
        harness.addToBattlefieldAndReturn(player1, new CrypticGateway());
        Permanent bird = addCreatureReady(player1, new ScreamingSeahawk());
        Permanent soldier = addCreatureReady(player1, new DaruCavalier());
        Card creature = new AvenBrigadier();
        harness.setHand(player1, List.of(creature));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gameData = harness.getGameData();
        assertThat(bird.isTapped()).isTrue();
        assertThat(soldier.isTapped()).isTrue();
        assertThat(gameData.playerHands.get(player1.getId())).containsExactly(creature);
        harness.assertNotOnBattlefield(player1, creature.getName());
    }
}
