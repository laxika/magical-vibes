package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TravelersAmulet;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.l.LilianaOfTheVeil;
import com.github.laxika.magicalvibes.cards.p.ParallelLives;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Permanent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Bramblecrush.class, Forest.class, TravelersAmulet.class, WalkingCorpse.class,
        LilianaOfTheVeil.class, ParallelLives.class})
class BramblecrushTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Bramblecrush puts it on stack with target")
    void castingPutsOnStack() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        harness.setHand(player1, List.of(new Bramblecrush()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Bramblecrush");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving destroys target artifact")
    void resolvesDestroyArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new TravelersAmulet()).getId();
        harness.setHand(player1, List.of(new Bramblecrush()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Traveler's Amulet");
        harness.assertInGraveyard(player2, "Traveler's Amulet");
    }

    @Test
    @DisplayName("Resolving destroys target enchantment")
    void resolvesDestroyEnchantment() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new ParallelLives()).getId();
        harness.setHand(player1, List.of(new Bramblecrush()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Parallel Lives");
        harness.assertInGraveyard(player2, "Parallel Lives");
    }

    @Test
    @DisplayName("Resolving destroys target land")
    void resolvesDestroyLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        harness.setHand(player1, List.of(new Bramblecrush()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Resolving destroys target planeswalker")
    void resolvesDestroyPlaneswalker() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new LilianaOfTheVeil()).getId();
        harness.setHand(player1, List.of(new Bramblecrush()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Liliana of the Veil");
        harness.assertInGraveyard(player2, "Liliana of the Veil");
    }

    @Test
    @DisplayName("Cannot target a creature with Bramblecrush")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new Bramblecrush()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID creatureId = harness.getPermanentId(player2, "Walking Corpse");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        harness.setHand(player1, List.of(new Bramblecrush()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Bramblecrush");
    }

    @Test
    @DisplayName("Can destroy a noncreature permanent controlled by the caster")
    void destroysOwnPermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new Forest()).getId();
        harness.setHand(player1, List.of(new Bramblecrush()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Bramblecrush");
    }

    @Test
    @DisplayName("Cannot target a land that is currently a creature")
    void cannotTargetAnimatedLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        land.setAnimatedUntilEndOfTurn(true);
        land.setAnimatedPower(3);
        land.setAnimatedToughness(3);
        harness.setHand(player1, List.of(new Bramblecrush()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Does not destroy a target that becomes a creature before resolution")
    void targetBecomingCreatureIsIllegalOnResolution() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Bramblecrush()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, land.getId());

        land.setAnimatedUntilEndOfTurn(true);
        land.setAnimatedPower(3);
        land.setAnimatedToughness(3);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player1, "Bramblecrush");
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
