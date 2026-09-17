package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DragonMage;
import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BladewingTheRisen.class, DragonMage.class, GoblinBrigand.class})
class BladewingTheRisenTest extends BaseCardTest {

    /** Casts Bladewing and resolves the creature spell so its ETB trigger sets up graveyard targeting. */
    private void castBladewing() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new BladewingTheRisen(), "{3}{B}{B}{R}{R}");
        harness.passBothPriorities(); // resolve creature → ETB triggers graveyard targeting
    }

    // ===== ETB reanimation =====

    @Test
    @DisplayName("ETB returns a targeted Dragon permanent card from graveyard to the battlefield")
    void etbReturnsDragonToBattlefield() {
        DragonMage dragon = new DragonMage();
        harness.setGraveyard(player1, List.of(dragon));

        castBladewing();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(dragon.getId());

        harness.handleMultipleCardsChosen(player1, List.of(dragon.getId()));
        harness.passBothPriorities(); // resolve the ETB triggered ability

        harness.assertOnBattlefield(player1, "Dragon Mage");
        harness.assertNotInGraveyard(player1, "Dragon Mage");
    }

    @Test
    @DisplayName("A non-Dragon card in the graveyard is not a legal target")
    void nonDragonNotTargetable() {
        harness.setGraveyard(player1, List.of(new GoblinBrigand()));

        castBladewing();

        // No Dragon to return → no graveyard choice, nothing reanimated
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Goblin Brigand");
    }

    @Test
    @DisplayName("The optional return can be declined")
    void returnCanBeDeclined() {
        DragonMage dragon = new DragonMage();
        harness.setGraveyard(player1, List.of(dragon));

        castBladewing();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        // Choose nothing — "you may return"
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dragon Mage");
        harness.assertNotOnBattlefield(player1, "Dragon Mage");
    }

    @Test
    @DisplayName("Empty graveyard produces no trigger")
    void emptyGraveyardNoTrigger() {
        castBladewing();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("The ETB does not target a Dragon permanent card in an opponent's graveyard")
    void etbOnlyTargetsOwnGraveyard() {
        harness.setGraveyard(player2, List.of(new DragonMage()));

        castBladewing();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Dragon Mage");
    }

    // ===== Activated ability: Dragon creatures get +1/+1 =====

    @Test
    @DisplayName("{B}{R} pumps all Dragon creatures until end of turn")
    void abilityPumpsDragons() {
        harness.addToBattlefield(player1, new BladewingTheRisen());
        Permanent ownDragon = harness.addToBattlefieldAndReturn(player1, new DragonMage());
        Permanent nonDragon = harness.addToBattlefieldAndReturn(player1, new GoblinBrigand());
        Permanent opponentDragon = harness.addToBattlefieldAndReturn(player2, new DragonMage());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null); // Bladewing's {B}{R} ability
        harness.passBothPriorities();

        assertThat(ownDragon.getEffectivePower()).isEqualTo(6);
        assertThat(ownDragon.getEffectiveToughness()).isEqualTo(6);
        assertThat(opponentDragon.getEffectivePower()).isEqualTo(6); // all players' Dragons
        assertThat(nonDragon.getEffectivePower()).isEqualTo(2);      // Goblin Brigand unaffected
        assertThat(nonDragon.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The Dragon pump wears off at end of turn")
    void abilityPumpWearsOff() {
        harness.addToBattlefield(player1, new BladewingTheRisen());
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new DragonMage());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(dragon.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(0);
        assertThat(dragon.getToughnessModifier()).isEqualTo(0);
    }
}
