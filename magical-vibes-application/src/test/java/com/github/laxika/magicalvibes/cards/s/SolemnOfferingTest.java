package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.h.HonorOfThePure;
import com.github.laxika.magicalvibes.cards.d.DarksteelColossus;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
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

@CardUsed({SolemnOffering.class, HonorOfThePure.class, HowlingMine.class,
        RuneclawBear.class, DarksteelColossus.class})
class SolemnOfferingTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Solemn Offering puts it on the stack with target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new HowlingMine());
        harness.setHand(player1, List.of(new SolemnOffering()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Howling Mine");
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
        harness.addToBattlefield(player2, new HowlingMine());
        harness.setHand(player1, List.of(new SolemnOffering()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Howling Mine");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Howling Mine");
        harness.assertInGraveyard(player2, "Howling Mine");
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Resolving destroys target enchantment and gains 4 life")
    void destroysEnchantmentAndGainsLife() {
        harness.addToBattlefield(player2, new HonorOfThePure());
        harness.setHand(player1, List.of(new SolemnOffering()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Honor of the Pure");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Honor of the Pure");
        harness.assertInGraveyard(player2, "Honor of the Pure");
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Solemn Offering goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new HowlingMine());
        harness.setHand(player1, List.of(new SolemnOffering()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Howling Mine");
        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Solemn Offering");
    }

    @Test
    @DisplayName("Fizzles and does not gain life when target is removed before resolution")
    void fizzlesAndDoesNotGainLifeWhenTargetRemoved() {
        harness.addToBattlefield(player2, new HowlingMine());
        harness.setHand(player1, List.of(new SolemnOffering()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Howling Mine");
        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Solemn Offering");
    }

    @Test
    @DisplayName("Cannot target a creature with Solemn Offering")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new SolemnOffering()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID creatureId = harness.getPermanentId(player2, "Runeclaw Bear");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gains life even when the targeted artifact is indestructible")
    void gainsLifeWhenDestructionIsPrevented() {
        harness.addToBattlefield(player2, new DarksteelColossus());
        harness.setHand(player1, List.of(new SolemnOffering()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Darksteel Colossus");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Darksteel Colossus");
        harness.assertNotInGraveyard(player2, "Darksteel Colossus");
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Solemn Offering");
    }

    @Test
    @DisplayName("Can destroy your own artifact and gain life")
    void canTargetOwnArtifact() {
        harness.addToBattlefield(player1, new HowlingMine());
        harness.setHand(player1, List.of(new SolemnOffering()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player1, "Howling Mine");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Howling Mine");
        harness.assertInGraveyard(player1, "Howling Mine");
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }
}
