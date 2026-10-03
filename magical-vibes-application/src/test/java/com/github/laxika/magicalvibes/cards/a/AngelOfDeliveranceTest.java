package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelOfDeliverance.class, Forest.class, GrizzlyBears.class, Naturalize.class, Pacifism.class})
class AngelOfDeliveranceTest extends BaseCardTest {

    @Test
    @DisplayName("Delirium exiles a target creature an opponent controls when this deals damage")
    void deliriumExilesOpponentCreature() {
        setDelirium();
        addAttacker();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("The ability does not trigger without delirium")
    void doesNotTriggerWithoutDelirium() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Naturalize()));
        addAttacker();
        addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("The ability cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        setDelirium();
        addAttacker();
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The condition is checked again when the ability resolves")
    void checksDeliriumOnResolution() {
        setDelirium();
        addAttacker();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.handlePermanentChosen(player1, target.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void fourCardsWithOnlyThreeTypesDoNotEnableDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new Forest(), new Naturalize()));
        addAttacker();
        addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 14);
    }

    @Test
    void opponentGraveyardDoesNotEnableDelirium() {
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new Forest(), new Naturalize(), new Pacifism()));
        addAttacker();
        addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void damageToCreatureAlsoTriggersExile() {
        setDelirium();
        addAttacker();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Naturalize(), new Pacifism()));
    }

    private void addAttacker() {
        Permanent angel = addCreatureReady(player1, new AngelOfDeliverance());
        angel.setAttacking(true);
    }
}
