package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.ManaLeak;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reverberate.class, CounselOfTheSoratami.class, GrizzlyBears.class,
        Fireball.class, LightningBolt.class, ManaLeak.class})
class ReverberateTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Reverberate puts it on the stack targeting a spell")
    void castingPutsOnStackTargetingSpell() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Reverberate()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);

        UUID counselCardId = counsel.getId();
        harness.castInstant(player2, 0, counselCardId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry reverberateEntry = gd.stack.getLast();
        assertThat(reverberateEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(reverberateEntry.getCard()).isInstanceOf(Reverberate.class);
        assertThat(reverberateEntry.getTargetId()).isEqualTo(counselCardId);
    }

    @Test
    @DisplayName("Cannot target a creature spell with Reverberate")
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Reverberate()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        UUID bearsCardId = bears.getId();
        assertThatThrownBy(() -> harness.castInstant(player2, 0, bearsCardId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolving — copying a sorcery =====

    @Test
    @DisplayName("Resolving creates a copy of the target sorcery on the stack")
    void resolvingCreatesCopyOnStack() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Reverberate()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, counsel.getId());

        GameData gd = harness.getGameData();
        // Original counsel + copy should be on the stack
        assertThat(gd.stack).hasSize(2);
        StackEntry copyEntry = gd.stack.getLast();
        assertThat(copyEntry.getDescription()).isEqualTo("Copy of Counsel of the Soratami");
        assertThat(copyEntry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(copyEntry.isCopy()).isTrue();
        assertThat(copyEntry.getControllerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Copy of draw spell makes the copy controller draw cards")
    void copyOfDrawSpellDrawsForCopyController() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Reverberate()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, counsel.getId());

        GameData gd = harness.getGameData();
        int p2HandAfterCast = gd.playerHands.get(player2.getId()).size();

        // Resolve Reverberate
        harness.passBothPriorities();
        // Resolve copy of Counsel — player2 draws 2
        harness.passBothPriorities();

        int p2HandAfter = gd.playerHands.get(player2.getId()).size();
        assertThat(p2HandAfter - p2HandAfterCast).isEqualTo(2);
    }

    @Test
    @DisplayName("Original spell still resolves after copy resolves")
    void originalSpellStillResolves() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Reverberate()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, counsel.getId());

        GameData gd = harness.getGameData();
        int p1HandAfterCast = gd.playerHands.get(player1.getId()).size();

        // Resolve Reverberate → copy created
        harness.passBothPriorities();
        // Resolve copy → player2 draws 2
        harness.passBothPriorities();
        // Resolve original → player1 draws 2
        harness.passBothPriorities();

        int p1HandAfter = gd.playerHands.get(player1.getId()).size();
        assertThat(p1HandAfter - p1HandAfterCast).isEqualTo(2);
    }

    // ===== Reverberate goes to graveyard =====

    @Test
    @DisplayName("Reverberate goes to caster's graveyard after resolving")
    void reverberateGoesToCasterGraveyard() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Reverberate()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, counsel.getId());

        harness.assertInGraveyard(player2, "Reverberate");
    }

    // ===== Stack is empty after everything resolves =====

    @Test
    @DisplayName("Stack is empty after Reverberate, copy, and original all resolve")
    void stackEmptyAfterFullResolution() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Reverberate()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, counsel.getId());
        // Resolve copy
        harness.passBothPriorities();
        // Resolve original
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canRetargetAnInstantWithoutChangingTheOriginal() {
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Reverberate()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bolt.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        harness.assertNotInGraveyard(player2, "Lightning Bolt");
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Lightning Bolt");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayKeepTheOriginalTarget() {
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Reverberate()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bolt.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
        harness.assertLife(player1, 20);
    }

    @Test
    void preservesXAndDoesNotRequirePaymentForTheCopy() {
        Fireball fireball = new Fireball();
        harness.setHand(player1, List.of(fireball));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setHand(player2, List.of(new Reverberate()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 4, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, fireball.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
        harness.passBothPriorities();
        harness.assertLife(player2, 12);
    }

    @Test
    void canChooseNewTargetsForEveryTargetOfTheCopy() {
        Permanent firstNewTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondNewTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Fireball fireball = new Fireball();
        harness.setHand(player1, List.of(fireball));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setHand(player2, List.of(new Reverberate()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 4, List.of(player1.getId(), player2.getId()));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, fireball.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, firstNewTarget.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, secondNewTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(firstNewTarget, secondNewTarget);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(firstNewTarget.getCard(), secondNewTarget.getCard());
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void canCopyASpellCopy() {
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Reverberate(), new Reverberate()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bolt.getId());
        harness.handleMayAbilityChosen(player2, false);
        UUID firstCopyId = gd.stack.getLast().getTargetableId();
        harness.castAndResolveInstant(player2, 0, firstCopyId);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
        harness.passBothPriorities();
        harness.assertLife(player2, 11);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canRetargetACopiedCounterspellToTheOriginalCounterspell() {
        LightningBolt bolt = new LightningBolt();
        ManaLeak leak = new ManaLeak();
        harness.setHand(player1, List.of(bolt, new Reverberate()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(leak));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bolt.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, leak.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, leak.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Mana Leak");
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotCreateACopyWhenTheTargetSpellHasBeenCountered() {
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt, new ManaLeak()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new Reverberate()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bolt.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, bolt.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.assertInGraveyard(player2, "Reverberate");
    }

}
