package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RageExtractorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell with Phyrexian mana triggers target selection")
    void phyrexianSpellTriggersTargetSelection() {
        harness.addToBattlefield(player1, new RageExtractor());
        harness.setHand(player1, List.of(new RageExtractor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Deals damage equal to spell's mana value to target player")
    void dealsDamageEqualToManaValueToPlayer() {
        harness.addToBattlefield(player1, new RageExtractor());
        harness.setHand(player1, List.of(new RageExtractor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castArtifact(player1, 0);

        // Choose opponent as target for the triggered ability
        harness.handlePermanentChosen(player1, player2.getId());

        // Resolve triggered ability (mana value of Rage Extractor = 5)
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 5);
    }

    @Test
    @DisplayName("Deals damage equal to spell's mana value to target creature")
    void dealsDamageEqualToManaValueToCreature() {
        harness.addToBattlefield(player1, new RageExtractor());
        harness.addToBattlefield(player2, new SuntailHawk());
        UUID hawkId = harness.getPermanentId(player2, "Suntail Hawk");
        harness.setHand(player1, List.of(new RageExtractor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);

        // Choose the creature as target
        harness.handlePermanentChosen(player1, hawkId);

        // Resolve triggered ability — 5 damage kills the 1/1
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Suntail Hawk"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Suntail Hawk"));
    }

    @Test
    @DisplayName("Non-Phyrexian spell does not trigger Rage Extractor")
    void nonPhyrexianSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new RageExtractor());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        // No target selection prompt — only the creature spell on the stack
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Opponent casting Phyrexian spell does not trigger Rage Extractor")
    void opponentPhyrexianSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new RageExtractor());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new RageExtractor()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castArtifact(player2, 0);

        GameData gd = harness.getGameData();
        // No target selection prompt — only the artifact spell on the stack
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }
}
