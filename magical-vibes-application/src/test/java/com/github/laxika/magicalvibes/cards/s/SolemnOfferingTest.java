package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SolemnOfferingTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting Solemn Offering puts it on the stack with target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new SolemnOffering()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Solemn Offering");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving destroys target artifact and gains 4 life")
    void destroysArtifactAndGainsLife() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new SolemnOffering()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Resolving destroys target enchantment and gains 4 life")
    void destroysEnchantmentAndGainsLife() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new SolemnOffering()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Solemn Offering goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new SolemnOffering()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Solemn Offering");
    }

    @Test
    @DisplayName("Fizzles and does not gain life when target is removed before resolution")
    void fizzlesAndDoesNotGainLifeWhenTargetRemoved() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new SolemnOffering()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Solemn Offering");
    }

    @Test
    @DisplayName("Cannot target a creature with Solemn Offering")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SolemnOffering()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
}
