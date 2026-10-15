package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.r.RazeToTheGround;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeavyweightDemolisher.class, RazeToTheGround.class})
class HeavyweightDemolisherTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {3} during upkeep keeps Heavyweight Demolisher untapped")
    void payingUpkeepCostKeepsItUntapped() {
        Permanent demolisher = addCreatureReady(player1, new HeavyweightDemolisher());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(demolisher.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining the upkeep payment taps Heavyweight Demolisher")
    void decliningUpkeepCostTapsIt() {
        Permanent demolisher = addCreatureReady(player1, new HeavyweightDemolisher());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(demolisher.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Being unable to pay the upkeep cost taps Heavyweight Demolisher")
    void insufficientManaTapsIt() {
        Permanent demolisher = addCreatureReady(player1, new HeavyweightDemolisher());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(demolisher.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The upkeep ability does not trigger during an opponent's upkeep")
    void noTriggerDuringOpponentUpkeep() {
        Permanent demolisher = addCreatureReady(player1, new HeavyweightDemolisher());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(demolisher.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Unearth returns Heavyweight Demolisher with haste and exiles it at the next end step")
    void unearthReturnsAndExilesAtEndStep() {
        harness.setGraveyard(player1, List.of(new HeavyweightDemolisher()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent demolisher = findPermanent(player1, "Heavyweight Demolisher");
        assertThat(demolisher.getGrantedKeywords()).contains(Keyword.HASTE);

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Heavyweight Demolisher");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(cardInExile -> cardInExile.getName().equals("Heavyweight Demolisher"));
    }

    @Test
    void menaceRejectsASingleBlocker() {
        addCreatureReady(player1, new HeavyweightDemolisher());
        addCreatureReady(player2, new HeavyweightDemolisher());
        addCreatureReady(player2, new HeavyweightDemolisher());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new HeavyweightDemolisher());
        Permanent first = addCreatureReady(player2, new HeavyweightDemolisher());
        Permanent second = addCreatureReady(player2, new HeavyweightDemolisher());

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2,
                        List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    void unearthCannotBeActivatedDuringUpkeep() {
        harness.setGraveyard(player1, List.of(new HeavyweightDemolisher()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        harness.assertInGraveyard(player1, "Heavyweight Demolisher");
        harness.assertNotOnBattlefield(player1, "Heavyweight Demolisher");
    }

    @Test
    void unearthedCreatureIsExiledInsteadOfBeingDestroyed() {
        harness.setGraveyard(player1, List.of(new HeavyweightDemolisher()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent demolisher = findPermanent(player1, "Heavyweight Demolisher");
        harness.setHand(player1, List.of(new RazeToTheGround()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, demolisher.getId());

        harness.assertNotOnBattlefield(player1, "Heavyweight Demolisher");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Heavyweight Demolisher"));
    }

    @Test
    void payingUpkeepCostDoesNotUntapTheCreature() {
        Permanent demolisher = addCreatureReady(player1, new HeavyweightDemolisher());
        advanceToUpkeep(player1);
        demolisher.tap();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(demolisher.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void unearthRequiresTwoRedMana() {
        harness.setGraveyard(player1, List.of(new HeavyweightDemolisher()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Heavyweight Demolisher");
        harness.assertNotOnBattlefield(player1, "Heavyweight Demolisher");
    }
}
