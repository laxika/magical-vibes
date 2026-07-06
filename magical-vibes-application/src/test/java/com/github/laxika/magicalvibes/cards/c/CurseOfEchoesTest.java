package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByEnchantedPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CurseOfEchoesTest extends BaseCardTest {

    private Permanent attachCurseToPlayer2() {
        Permanent auraPerm = new Permanent(new CurseOfEchoes());
        auraPerm.setAttachedTo(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);
        return auraPerm;
    }

    // ===== Trigger fires only for the enchanted player =====

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

    // ===== "may copy" choice =====

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
        harness.passBothPriorities();          // resolve the copy-creation ability

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
        harness.passBothPriorities();          // resolve copy-creation ability
        harness.passBothPriorities();          // resolve the copy -> player1 draws 2

        int p1HandAfter = gd.playerHands.get(player1.getId()).size();
        assertThat(p1HandAfter - p1HandBefore).isEqualTo(2);
    }

    // ===== Targeted spell — retarget option =====

    @Test
    @DisplayName("Accepting the copy of a targeted spell offers a retarget choice")
    void copyOfTargetedSpellOffersRetarget() {
        attachCurseToPlayer2();

        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player2, bears);
        UUID bearsPermId = harness.getPermanentId(player2, "Grizzly Bears");

        LightningBolt bolt = new LightningBolt();
        harness.setHand(player2, List.of(bolt));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, bearsPermId);
        harness.passBothPriorities();          // resolve trigger -> may copy
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();          // resolve copy-creation ability -> may retarget

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Declining the retarget keeps the copy on the original target, controlled by the other player")
    void decliningRetargetKeepsOriginalTarget() {
        attachCurseToPlayer2();

        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player2, bears);
        UUID bearsPermId = harness.getPermanentId(player2, "Grizzly Bears");

        LightningBolt bolt = new LightningBolt();
        harness.setHand(player2, List.of(bolt));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, bearsPermId);
        harness.passBothPriorities();          // resolve trigger -> may copy
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();          // resolve copy-creation ability -> may retarget
        harness.handleMayAbilityChosen(player1, false);

        StackEntry copyEntry = gd.stack.stream()
                .filter(se -> se.getDescription().equals("Copy of Lightning Bolt"))
                .findFirst().orElseThrow();
        assertThat(copyEntry.getControllerId()).isEqualTo(player1.getId());
        assertThat(copyEntry.getTargetId()).isEqualTo(bearsPermId);
    }
}
