package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed({RavagingBlaze.class, Cancel.class, GrizzlyBears.class, ActOfTreason.class})
class RavagingBlazeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to target creature only without spell mastery")
    void dealsXDamageToCreatureOnly() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RavagingBlaze()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, 3, targetId);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Spell mastery also deals X damage to the creature's controller")
    void spellMasteryDamagesController() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Cancel(), new Cancel()));
        harness.setHand(player1, List.of(new RavagingBlaze()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, 3, targetId);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("A single instant in the graveyard is not enough for spell mastery")
    void oneInstantIsNotEnough() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Cancel()));
        harness.setHand(player1, List.of(new RavagingBlaze()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, 2, targetId);

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Fizzles when the target creature leaves before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Cancel(), new Cancel()));
        harness.setHand(player1, List.of(new RavagingBlaze()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, 3, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Ravaging Blaze");
    }

    @Test
    @DisplayName("Creature survives when X is smaller than its toughness")
    void creatureSurvivesSmallX() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Cancel(), new Cancel()));
        harness.setHand(player1, List.of(new RavagingBlaze()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, 1, targetId);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Spell mastery counts two sorceries")
    void spellMasteryCountsSorceries() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new ActOfTreason(), new ActOfTreason()));
        harness.setHand(player1, List.of(new RavagingBlaze()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, 3, targetId);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Ravaging Blaze");
    }

    @Test
    @DisplayName("Spell mastery counts an instant and a sorcery")
    void spellMasteryCountsMixedTypes() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Cancel(), new ActOfTreason()));
        harness.setHand(player1, List.of(new RavagingBlaze()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, 1, targetId);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Ravaging Blaze");
    }

    @Test
    @DisplayName("Creature cards do not enable spell mastery")
    void creaturesDoNotCountForSpellMastery() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Cancel(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new RavagingBlaze()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, 1, targetId);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Ravaging Blaze");
    }

    @Test
    @DisplayName("Targeting your own creature damages you with spell mastery")
    void ownCreatureControllerTakesDamage() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Cancel(), new Cancel()));
        harness.setHand(player1, List.of(new RavagingBlaze()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, 3, targetId);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Ravaging Blaze");
    }

    @Test
    @DisplayName("X zero deals no damage even with spell mastery")
    void zeroXDealsNoDamage() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Cancel(), new Cancel()));
        harness.setHand(player1, List.of(new RavagingBlaze()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Ravaging Blaze");
    }

    @Test
    @DisplayName("Spell mastery is checked at resolution when gained")
    void spellMasteryGainedBeforeResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Cancel()));
        harness.setHand(player1, List.of(new RavagingBlaze()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, 1, targetId);
        harness.setGraveyard(player1, List.of(new Cancel(), new ActOfTreason()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Ravaging Blaze");
    }

    @Test
    @DisplayName("Spell mastery is checked at resolution when lost")
    void spellMasteryLostBeforeResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Cancel(), new ActOfTreason()));
        harness.setHand(player1, List.of(new RavagingBlaze()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, 1, targetId);
        harness.setGraveyard(player1, List.of(new Cancel()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Ravaging Blaze");
    }

    @Test
    @DisplayName("Opponents graveyard does not enable spell mastery")
    void opponentGraveyardDoesNotCount() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Cancel()));
        harness.setGraveyard(player2, List.of(new Cancel(), new Cancel()));
        harness.setHand(player1, List.of(new RavagingBlaze()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, 1, targetId);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Ravaging Blaze");
    }
}
