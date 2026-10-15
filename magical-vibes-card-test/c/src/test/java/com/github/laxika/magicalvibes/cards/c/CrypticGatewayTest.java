package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvenBrigadier;
import com.github.laxika.magicalvibes.cards.b.BarkhideMauler;
import com.github.laxika.magicalvibes.cards.d.DaruCavalier;
import com.github.laxika.magicalvibes.cards.i.Imagecrafter;
import com.github.laxika.magicalvibes.cards.s.ScreamingSeahawk;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrypticGateway.class, BarkhideMauler.class, AvenBrigadier.class, DaruCavalier.class,
        ScreamingSeahawk.class, Imagecrafter.class, Shock.class})
class CrypticGatewayTest extends BaseCardTest {

    @Test
    @DisplayName("Gateway cannot activate with fewer than two untapped controlled creatures")
    void insufficientCreaturesCannotPayCost() {
        harness.addToBattlefield(player1, new CrypticGateway());
        Permanent creature = addCreatureReady(player1, new BarkhideMauler());
        addCreatureReady(player2, new BarkhideMauler());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creatures still on the battlefield use their types at resolution")
    void livingCreatureUsesCurrentType() {
        harness.addToBattlefield(player1, new CrypticGateway());
        Permanent bird = addCreatureReady(player1, new ScreamingSeahawk());
        addCreatureReady(player1, new BarkhideMauler());
        addCreatureReady(player2, new Imagecrafter());
        Card creature = new BarkhideMauler();
        harness.setHand(player1, List.of(creature));

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player2, 0, null, bird.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, CardSubtype.BEAST.name());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertNotInHand(player1, creature.getName());
        assertThat(countPermanents(player1, creature.getName())).isEqualTo(2);
    }

    @Test
    @DisplayName("Summoning-sick creatures can pay Gateway's cost")
    void summoningSickCreaturesCanPayCost() {
        harness.addToBattlefield(player1, new CrypticGateway());
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new ScreamingSeahawk());
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new DaruCavalier());
        Card creature = new AvenBrigadier();
        harness.setHand(player1, List.of(creature));

        harness.activateAbility(player1, 0, null, null);
        assertThat(bird.isTapped()).isTrue();
        assertThat(soldier.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, creature.getName());
        assertThat(findPermanent(player1, creature.getName()).isTapped()).isFalse();
        harness.assertNotInHand(player1, creature.getName());
    }

    @Test
    @DisplayName("Accepting with no eligible creature finishes without putting a card onto the battlefield")
    void noEligibleCreatureFinishesResolution() {
        harness.addToBattlefield(player1, new CrypticGateway());
        addCreatureReady(player1, new ScreamingSeahawk());
        addCreatureReady(player1, new DaruCavalier());
        Card creature = new BarkhideMauler();
        harness.setHand(player1, List.of(creature));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, creature.getName());
        harness.assertNotOnBattlefield(player1, creature.getName());
    }

    @Test
    @DisplayName("A tapped creature that dies after changing type uses its last-known creature type")
    void departedCreatureUsesTypeImmediatelyBeforeLeaving() {
        harness.addToBattlefield(player1, new CrypticGateway());
        Permanent bird = addCreatureReady(player1, new ScreamingSeahawk());
        addCreatureReady(player1, new BarkhideMauler());
        addCreatureReady(player2, new Imagecrafter());
        Card creature = new BarkhideMauler();
        harness.setHand(player1, List.of(creature));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player2, 0, null, bird.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, CardSubtype.BEAST.name());
        harness.castAndResolveInstant(player2, 0, bird.getId());
        harness.assertInGraveyard(player1, bird.getCard().getName());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.assertNotInHand(player1, creature.getName());
        assertThat(countPermanents(player1, creature.getName())).isEqualTo(2);
    }

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
