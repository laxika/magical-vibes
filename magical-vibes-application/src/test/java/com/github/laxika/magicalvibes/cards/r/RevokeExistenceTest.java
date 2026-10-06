package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
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

@CardUsed({RevokeExistence.class, FountainOfYouth.class, AngelicChorus.class, GrizzlyBears.class,
        DarksteelAxe.class, Memnite.class})
class RevokeExistenceTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting Revoke Existence puts it on the stack with target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new RevokeExistence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isInstanceOf(RevokeExistence.class);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving exiles target artifact")
    void resolvesAndExilesArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new RevokeExistence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Fountain of Youth"));
        harness.assertNotInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Resolving exiles target enchantment")
    void resolvesAndExilesEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new RevokeExistence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Angelic Chorus"));
        harness.assertNotInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Cannot target a creature that is neither an artifact nor an enchantment")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RevokeExistence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new RevokeExistence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castSorcery(player1, 0, targetId);

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiles an artifact creature")
    void exilesArtifactCreature() {
        harness.addToBattlefield(player2, new Memnite());
        harness.setHand(player1, List.of(new RevokeExistence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Memnite");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Memnite");
        harness.assertNotInGraveyard(player2, "Memnite");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Memnite"));
        harness.assertInGraveyard(player1, "Revoke Existence");
    }

    @Test
    @DisplayName("Can exile an indestructible artifact you control")
    void exilesOwnIndestructibleArtifact() {
        harness.addToBattlefield(player1, new DarksteelAxe());
        harness.setHand(player1, List.of(new RevokeExistence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Darksteel Axe");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Darksteel Axe");
        harness.assertNotInGraveyard(player1, "Darksteel Axe");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Darksteel Axe"));
        harness.assertInGraveyard(player1, "Revoke Existence");
    }
}
