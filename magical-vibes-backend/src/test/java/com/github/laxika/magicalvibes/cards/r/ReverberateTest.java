package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReverberateTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Reverberate has correct card properties")
    void hasCorrectProperties() {
        Reverberate card = new Reverberate();

        assertThat(card.isNeedsSpellTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(CopySpellEffect.class);
        assertThat(card.getTargetFilter()).isEqualTo(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                "Target must be an instant or sorcery spell."
        ));
    }

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
        assertThat(reverberateEntry.getCard().getName()).isEqualTo("Reverberate");
        assertThat(reverberateEntry.getTargetPermanentId()).isEqualTo(counselCardId);
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
        harness.castInstant(player2, 0, counsel.getId());

        // Resolve Reverberate — should create a copy on stack
        harness.passBothPriorities();

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
        harness.castInstant(player2, 0, counsel.getId());

        // Resolve Reverberate
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Reverberate"));
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
        harness.castInstant(player2, 0, counsel.getId());

        // Resolve Reverberate
        harness.passBothPriorities();
        // Resolve copy
        harness.passBothPriorities();
        // Resolve original
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
    }
}
