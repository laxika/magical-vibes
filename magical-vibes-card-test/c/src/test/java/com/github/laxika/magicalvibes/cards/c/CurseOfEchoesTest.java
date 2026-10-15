package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfEchoes.class, CounselOfTheSoratami.class, GrizzlyBears.class, LightningBolt.class})
class CurseOfEchoesTest extends BaseCardTest {

    private Permanent attachCurseToPlayer2() {
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new CurseOfEchoes());
        auraPerm.setAttachedTo(player2.getId());
        return auraPerm;
    }


    @Test
    @DisplayName("Enchanted player casting a sorcery puts the curse trigger on the stack")
    void enchantedPlayerSorceryTriggers() {
        attachCurseToPlayer2();

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player2, List.of(counsel));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 0);

        // Stack: original sorcery + Curse of Echoes triggered ability
        assertThat(gd.stack).hasSize(2);
        StackEntry trigger = gd.stack.getLast();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getDescription()).contains("Curse of Echoes");
    }

    @Test
    @DisplayName("Curse does not trigger when the non-enchanted player (its controller) casts a spell")
    void nonEnchantedPlayerCastDoesNotTrigger() {
        attachCurseToPlayer2();

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);

        // Only the sorcery itself — no curse trigger
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Counsel of the Soratami");
    }

    @Test
    @DisplayName("Curse does not trigger on creature spells")
    void doesNotTriggerOnCreature() {
        attachCurseToPlayer2();

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }


    @Test
    @DisplayName("Resolving the trigger offers the other player an optional copy choice")
    void triggerOffersMayCopyToOtherPlayer() {
        attachCurseToPlayer2();

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player2, List.of(counsel));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 0);
        // Resolve the curse trigger
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting the copy creates a copy controlled by the other player")
    void acceptingCreatesCopyForOtherPlayer() {
        attachCurseToPlayer2();

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player2, List.of(counsel));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();          // resolve trigger -> may copy
        harness.handleMayAbilityChosen(player1, true);

        StackEntry copyEntry = gd.stack.stream()
                .filter(se -> se.getDescription().equals("Copy of Counsel of the Soratami"))
                .findFirst().orElseThrow();
        assertThat(copyEntry.isCopy()).isTrue();
        assertThat(copyEntry.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Declining the copy creates no copy")
    void decliningCreatesNoCopy() {
        attachCurseToPlayer2();

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player2, List.of(counsel));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();          // resolve trigger -> may copy
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).noneMatch(se -> se.getDescription().startsWith("Copy of"));
        // Only the original sorcery remains on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Counsel of the Soratami");
    }

    @Test
    @DisplayName("Copy of a draw spell draws cards for the other player")
    void copyDrawsForOtherPlayer() {
        attachCurseToPlayer2();

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player2, List.of(counsel));
        harness.addMana(player2, ManaColor.BLUE, 3);

        int p1HandBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();          // resolve trigger -> may copy
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();          // resolve the copy -> player1 draws 2

        int p1HandAfter = gd.playerHands.get(player1.getId()).size();
        assertThat(p1HandAfter - p1HandBefore).isEqualTo(2);
    }


    @Test
    @DisplayName("Accepting the copy of a targeted spell offers a retarget choice")
    void copyOfTargetedSpellOffersRetarget() {
        attachCurseToPlayer2();

        GrizzlyBears bears = new GrizzlyBears();
        UUID bearsPermId = harness.addToBattlefieldAndReturn(player2, bears).getId();

        LightningBolt bolt = new LightningBolt();
        harness.setHand(player2, List.of(bolt));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, bearsPermId);
        harness.passBothPriorities();          // resolve trigger -> may copy
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Declining the retarget keeps the copy on the original target, controlled by the other player")
    void decliningRetargetKeepsOriginalTarget() {
        attachCurseToPlayer2();

        GrizzlyBears bears = new GrizzlyBears();
        UUID bearsPermId = harness.addToBattlefieldAndReturn(player2, bears).getId();

        LightningBolt bolt = new LightningBolt();
        harness.setHand(player2, List.of(bolt));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, bearsPermId);
        harness.passBothPriorities();          // resolve trigger -> may copy
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        StackEntry copyEntry = gd.stack.stream()
                .filter(se -> se.getDescription().equals("Copy of Lightning Bolt"))
                .findFirst().orElseThrow();
        assertThat(copyEntry.getControllerId()).isEqualTo(player1.getId());
        assertThat(copyEntry.getTargetId()).isEqualTo(bearsPermId);
    }

    @Test
    @DisplayName("The aura can enchant its controller and copies are offered to the opponent")
    void canEnchantItsController() {
        harness.setHand(player1, List.of(new CurseOfEchoes(), new LightningBolt()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, player1.getId());
        harness.passBothPriorities();

        Permanent curse = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof CurseOfEchoes)
                .findFirst().orElseThrow();
        assertThat(curse.getAttachedTo()).isEqualTo(player1.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The copy can choose a new target and resolves before the original spell")
    void retargetedCopyResolvesBeforeOriginal() {
        attachCurseToPlayer2();
        UUID originalTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        UUID newTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, originalTarget);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, newTarget);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().isCopy()).isTrue();
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(newTarget);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(newTarget));
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(originalTarget));
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(originalTarget));
    }
}
