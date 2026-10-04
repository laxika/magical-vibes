package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.MeagerMeal;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GollumSilentSlinker.class, MeagerMeal.class, GundabadOpportunist.class})
class GollumSilentSlinkerTest extends BaseCardTest {

    @Test
    void adventurePutsCounterOnCreatureAndGivesTargetPlayerLife() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GundabadOpportunist());
        GollumSilentSlinker card = new GollumSilentSlinker();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAdventure(player1, 0, List.of(player2.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player2, 22);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCanOmitCreatureTarget() {
        GollumSilentSlinker card = new GollumSilentSlinker();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAdventure(player1, 0, List.of(player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 22);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        GollumSilentSlinker card = new GollumSilentSlinker();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAdventure(player1, 0, List.of(player1.getId()));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gollum, Silent Slinker");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void adventureCanTargetOwnCreatureAndOwnPlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GundabadOpportunist());
        GollumSilentSlinker card = new GollumSilentSlinker();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAdventure(player1, 0, List.of(player1.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureStillGainsLifeAndExilesWhenCreatureTargetLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GundabadOpportunist());
        GollumSilentSlinker card = new GollumSilentSlinker();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAdventure(player1, 0, List.of(player2.getId(), creature.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, creature));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player2, 22);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureRequiresPlayerTargetEvenWithNoCreatureTarget() {
        GollumSilentSlinker card = new GollumSilentSlinker();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Gollum, Silent Slinker");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new GollumSilentSlinker());
        addCreatureReady(player2, new GundabadOpportunist());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new GollumSilentSlinker());
        Permanent firstBlocker = addCreatureReady(player2, new GundabadOpportunist());
        Permanent secondBlocker = addCreatureReady(player2, new GundabadOpportunist());

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2,
                        List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }
}
