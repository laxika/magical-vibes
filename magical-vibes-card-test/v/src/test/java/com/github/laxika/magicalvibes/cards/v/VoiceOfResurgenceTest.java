package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoiceOfResurgence.class, GrizzlyBears.class, Shock.class, Unsummon.class})
class VoiceOfResurgenceTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent casting a spell during your turn creates the Elemental token")
    void opponentSpellOnYourTurnCreatesToken() {
        harness.addToBattlefield(player1, new VoiceOfResurgence());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        GameData gd = harness.getGameData();
        Permanent token = findPermanent(player1, "Elemental");
        assertThat(token).isNotNull();
        // Voice + Grizzly Bears + the token itself
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent casting a spell during their own turn does not trigger")
    void opponentSpellOnTheirTurnDoesNotTrigger() {
        harness.addToBattlefield(player1, new VoiceOfResurgence());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertNotOnBattlefield(player1, "Elemental");
    }

    @Test
    @DisplayName("Your own spell during your turn does not trigger")
    void ownSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new VoiceOfResurgence());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, bearsId);

        harness.assertNotOnBattlefield(player1, "Elemental");
    }

    @Test
    @DisplayName("When it dies, it creates the Elemental token")
    void deathCreatesToken() {
        harness.addToBattlefield(player1, new VoiceOfResurgence());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID voiceId = harness.getPermanentId(player1, "Voice of Resurgence");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, voiceId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Voice of Resurgence");
        GameData gd = harness.getGameData();
        Permanent token = findPermanent(player1, "Elemental");
        assertThat(token).isNotNull();
        // Grizzly Bears + the token itself (Voice is already gone)
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("The token counts itself when Voice was the only creature")
    void deathOfOnlyCreatureCreatesOneOneToken() {
        harness.addToBattlefield(player1, new VoiceOfResurgence());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID voiceId = harness.getPermanentId(player1, "Voice of Resurgence");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, voiceId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Voice of Resurgence");
        Permanent token = findPermanent(player1, "Elemental");
        assertThat(token).isNotNull();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent killing Voice during your turn produces both tokens in stack order")
    void lethalSpellOnYourTurnTriggersCastingAndDeath() {
        harness.addToBattlefield(player1, new VoiceOfResurgence());
        UUID voiceId = harness.getPermanentId(player1, "Voice of Resurgence");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, voiceId);

        harness.assertOnBattlefield(player1, "Voice of Resurgence");
        Permanent firstToken = findPermanent(player1, "Elemental");
        assertThat(firstToken).isNotNull();
        assertThat(gqs.getEffectivePower(gd, firstToken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstToken)).isEqualTo(2);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Voice of Resurgence");
        assertThat(gqs.getEffectivePower(gd, firstToken)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, firstToken)).isEqualTo(1);

        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Elemental"))
                .toList();
        assertThat(tokens).hasSize(2);
        for (Permanent token : tokens) {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Token size updates as your creatures enter and leave and ignores opposing creatures")
    void tokenSizeUpdatesWithCreatureCount() {
        harness.addToBattlefield(player1, new VoiceOfResurgence());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Elemental");
        assertThat(token).isNotNull();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);

        harness.addToBattlefield(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, bearsId);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returning Voice to hand does not trigger its death ability")
    void returningVoiceToHandDoesNotCreateToken() {
        harness.addToBattlefield(player1, new VoiceOfResurgence());
        UUID voiceId = harness.getPermanentId(player1, "Voice of Resurgence");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, voiceId);

        harness.assertInHand(player1, "Voice of Resurgence");
        harness.assertNotInGraveyard(player1, "Voice of Resurgence");
        harness.assertNotOnBattlefield(player1, "Elemental");
    }
}
