package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AbsoluteGrace;
import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.a.ArcLightning;
import com.github.laxika.magicalvibes.cards.f.FaithHealer;
import com.github.laxika.magicalvibes.cards.w.Windfall;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnergyField.class, ArcLightning.class, ArgothianSwine.class, FaithHealer.class,
        AbsoluteGrace.class, Windfall.class})
class EnergyFieldTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all damage from sources controlled by an opponent")
    void preventsDamageFromOpponentSources() {
        harness.addToBattlefield(player1, new EnergyField());
        harness.setHand(player2, List.of(new ArcLightning()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, Map.of(player1.getId(), 3));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Energy Field");
    }

    @Test
    @DisplayName("Does not prevent damage from sources controlled by its controller")
    void doesNotPreventDamageFromOwnSources() {
        harness.addToBattlefield(player1, new EnergyField());
        harness.setHand(player1, List.of(new ArcLightning()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, Map.of(player1.getId(), 3));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertInGraveyard(player1, "Energy Field");
    }

    @Test
    @DisplayName("Prevents combat damage from an opponent's creature")
    void preventsCombatDamageFromOpponentCreature() {
        harness.addToBattlefield(player1, new EnergyField());
        addCreatureReady(player2, new ArgothianSwine());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Energy Field");
    }

    @Test
    @DisplayName("Triggers when a card is put into its controller's graveyard from the battlefield")
    void triggersWhenBattlefieldCardIsPutIntoGraveyard() {
        harness.addToBattlefield(player1, new EnergyField());
        harness.addToBattlefield(player1, new FaithHealer());
        Permanent grace = harness.addToBattlefieldAndReturn(player1, new AbsoluteGrace());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, grace.getId());

        harness.assertInGraveyard(player1, "Absolute Grace");
        assertThat(gd.stack).isNotEmpty();
        resolveAllTriggers();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Energy Field");
    }

    @Test
    @DisplayName("Triggers when a card is discarded from its controller's hand")
    void triggersWhenCardIsDiscardedFromHand() {
        harness.addToBattlefield(player1, new EnergyField());
        harness.setHand(player1, List.of(new Windfall(), new Windfall()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Windfall(), new Windfall()));
        harness.setLibrary(player2, List.of(new Windfall(), new Windfall()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Windfall");
        harness.assertInGraveyard(player1, "Energy Field");
    }
}
