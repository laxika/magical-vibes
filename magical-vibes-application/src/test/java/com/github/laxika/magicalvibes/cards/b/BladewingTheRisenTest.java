package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DragonMage;
import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
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

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

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

        harness.handleMultipleCardsChosen(player1, List.of(dragon.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Dragon Mage");
        harness.assertNotOnBattlefield(player1, "Dragon Mage");
    }

    @Test
    @DisplayName("Empty graveyard produces no legal graveyard target")
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

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(dragon.getPowerModifier()).isEqualTo(0);
        assertThat(dragon.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @CardUsed({NamelessInversion.class})
    @DisplayName("A Dragon instant with changeling is not a Dragon permanent card")
    void dragonInstantIsNotALegalTarget() {
        DragonMage dragon = new DragonMage();
        NamelessInversion instant = new NamelessInversion();
        harness.setGraveyard(player1, List.of(dragon, instant));

        castBladewing();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(dragon.getId());
    }

    @Test
    @DisplayName("A Dragon removed from the graveyard before resolution is not returned")
    void missingTargetIsNotReturned() {
        DragonMage dragon = new DragonMage();
        harness.setGraveyard(player1, List.of(dragon));

        castBladewing();
        harness.handleMultipleCardsChosen(player1, List.of(dragon.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dragon Mage");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Repeated activations stack and do not pump Dragons entering after resolution")
    void repeatedPumpsExcludeLaterDragons() {
        Permanent bladewing = harness.addToBattlefieldAndReturn(player1, new BladewingTheRisen());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent laterDragon = harness.addToBattlefieldAndReturn(player2, new DragonMage());

        assertThat(bladewing.getEffectivePower()).isEqualTo(6);
        assertThat(bladewing.getEffectiveToughness()).isEqualTo(6);
        assertThat(laterDragon.getEffectivePower()).isEqualTo(5);
        assertThat(laterDragon.getEffectiveToughness()).isEqualTo(5);
    }
}
