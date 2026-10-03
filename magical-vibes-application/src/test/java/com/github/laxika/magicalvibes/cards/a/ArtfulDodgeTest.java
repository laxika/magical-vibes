package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArtfulDodge.class, DawntreaderElk.class, EvolvingWilds.class})
class ArtfulDodgeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Artful Dodge targeting a creature puts it on the stack")
    void castingTargetingCreaturePutsOnStack() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");

        harness.setHand(player1, List.of(new ArtfulDodge()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Artful Dodge");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new EvolvingWilds());
        UUID landId = harness.getPermanentId(player1, "Evolving Wilds");

        harness.setHand(player1, List.of(new ArtfulDodge()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving makes target creature unblockable this turn")
    void resolvingMakesCreatureUnblockable() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");

        harness.setHand(player1, List.of(new ArtfulDodge()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, targetId);

        Permanent target = findPermanent(player1, "Dawntreader Elk");
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable resets at end of turn")
    void unblockableResetsAtEndOfTurn() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");

        harness.setHand(player1, List.of(new ArtfulDodge()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, targetId);

        Permanent target = findPermanent(player1, "Dawntreader Elk");
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Can target opponent's creature")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player2, new DawntreaderElk());
        UUID targetId = harness.getPermanentId(player2, "Dawntreader Elk");

        harness.setHand(player1, List.of(new ArtfulDodge()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, targetId);

        Permanent target = findPermanent(player2, "Dawntreader Elk");
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Artful Dodge goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");

        harness.setHand(player1, List.of(new ArtfulDodge()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Artful Dodge");
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");

        harness.setHand(player1, List.of(new ArtfulDodge()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, targetId);

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // Artful Dodge still goes to graveyard
        harness.assertInGraveyard(player1, "Artful Dodge");
    }

    @Test
    @DisplayName("Flashback from graveyard makes creature unblockable")
    void flashbackFromGraveyard() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");

        harness.setGraveyard(player1, List.of(new ArtfulDodge()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveFlashback(player1, 0, targetId);

        Permanent target = findPermanent(player1, "Dawntreader Elk");
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Flashback exiles the spell after resolving")
    void flashbackExilesAfterResolving() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        UUID targetId = harness.getPermanentId(player1, "Dawntreader Elk");

        harness.setGraveyard(player1, List.of(new ArtfulDodge()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveFlashback(player1, 0, targetId);

        GameData gd = harness.getGameData();
        // Should NOT be in graveyard
        harness.assertNotInGraveyard(player1, "Artful Dodge");
        // Should be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Artful Dodge"));
    }

    @Test
    @DisplayName("Flashback exiles the spell when its target disappears")
    void flashbackExilesWhenTargetDisappears() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        harness.setGraveyard(player1, List.of(new ArtfulDodge()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castFlashback(player1, 0, target.getId());
        harness.getGameData().playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertNotInGraveyard(player1, "Artful Dodge");
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card instanceof ArtfulDodge);
        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Flashback cannot be cast during combat")
    void flashbackRequiresSorceryTiming() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        harness.setGraveyard(player1, List.of(new ArtfulDodge()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Artful Dodge");
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
