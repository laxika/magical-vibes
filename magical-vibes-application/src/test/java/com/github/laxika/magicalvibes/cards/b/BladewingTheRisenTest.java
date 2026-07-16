package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DragonWhelp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BladewingTheRisenTest extends BaseCardTest {

    /** Casts Bladewing and resolves the creature spell so its ETB trigger sets up graveyard targeting. */
    private void castBladewing() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BladewingTheRisen()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → ETB triggers graveyard targeting
    }

    // ===== ETB reanimation =====

    @Test
    @DisplayName("ETB returns a targeted Dragon card from graveyard to the battlefield")
    void etbReturnsDragonToBattlefield() {
        DragonWhelp whelp = new DragonWhelp();
        harness.setGraveyard(player1, List.of(whelp));

        castBladewing();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(whelp.getId()));
        harness.passBothPriorities(); // resolve the ETB triggered ability

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Dragon Whelp"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Dragon Whelp"));
    }

    @Test
    @DisplayName("A non-Dragon card in the graveyard is not a legal target")
    void nonDragonNotTargetable() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castBladewing();

        // No Dragon to return → no graveyard choice, nothing reanimated
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("The optional return can be declined")
    void returnCanBeDeclined() {
        DragonWhelp whelp = new DragonWhelp();
        harness.setGraveyard(player1, List.of(whelp));

        castBladewing();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        // Choose nothing — "you may return"
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dragon Whelp"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Dragon Whelp"));
    }

    @Test
    @DisplayName("Empty graveyard produces no trigger")
    void emptyGraveyardNoTrigger() {
        castBladewing();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    // ===== Activated ability: Dragon creatures get +1/+1 =====

    @Test
    @DisplayName("{B}{R} pumps all Dragon creatures until end of turn")
    void abilityPumpsDragons() {
        harness.addToBattlefield(player1, new BladewingTheRisen());
        Permanent ownDragon = harness.addToBattlefieldAndReturn(player1, new DragonWhelp());
        Permanent nonDragon = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentDragon = harness.addToBattlefieldAndReturn(player2, new DragonWhelp());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null); // Bladewing's {B}{R} ability
        harness.passBothPriorities();

        assertThat(ownDragon.getEffectivePower()).isEqualTo(3);      // 2/3 -> 3/4
        assertThat(ownDragon.getEffectiveToughness()).isEqualTo(4);
        assertThat(opponentDragon.getEffectivePower()).isEqualTo(3); // all players' Dragons
        assertThat(nonDragon.getEffectivePower()).isEqualTo(2);      // Grizzly Bears unaffected
        assertThat(nonDragon.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The Dragon pump wears off at end of turn")
    void abilityPumpWearsOff() {
        harness.addToBattlefield(player1, new BladewingTheRisen());
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new DragonWhelp());

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
