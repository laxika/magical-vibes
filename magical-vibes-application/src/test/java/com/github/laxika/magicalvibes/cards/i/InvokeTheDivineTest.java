package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AdamantWill;
import com.github.laxika.magicalvibes.cards.g.GuardiansOfKoilos;
import com.github.laxika.magicalvibes.cards.h.HistoryOfBenalia;
import com.github.laxika.magicalvibes.cards.j.JoustingLance;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
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

@CardUsed({InvokeTheDivine.class, JoustingLance.class, HistoryOfBenalia.class, LlanowarElves.class, AdamantWill.class, GuardiansOfKoilos.class})
class InvokeTheDivineTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Invoke the Divine puts it on the stack with target")
    void castingPutsOnStack() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new JoustingLance()).getId();
        harness.setHand(player1, List.of(new InvokeTheDivine()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Invoke the Divine");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving destroys target artifact and gains 4 life")
    void destroysArtifactAndGainsLife() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new JoustingLance()).getId();
        harness.setHand(player1, List.of(new InvokeTheDivine()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Jousting Lance");
        harness.assertInGraveyard(player2, "Jousting Lance");
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Resolving destroys target enchantment and gains 4 life")
    void destroysEnchantmentAndGainsLife() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new HistoryOfBenalia()).getId();
        harness.setHand(player1, List.of(new InvokeTheDivine()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "History of Benalia");
        harness.assertInGraveyard(player2, "History of Benalia");
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Invoke the Divine goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new JoustingLance()).getId();
        harness.setHand(player1, List.of(new InvokeTheDivine()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Invoke the Divine");
    }

    @Test
    @DisplayName("Fizzles and does not gain life when target is removed before resolution")
    void fizzlesAndDoesNotGainLifeWhenTargetRemoved() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new JoustingLance()).getId();
        harness.setHand(player1, List.of(new InvokeTheDivine()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Invoke the Divine");
    }

    @Test
    @DisplayName("Cannot target a creature with Invoke the Divine")
    void cannotTargetCreature() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new LlanowarElves()).getId();
        harness.setHand(player1, List.of(new InvokeTheDivine()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy an artifact controlled by the caster")
    void destroysOwnArtifactAndGainsLife() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new JoustingLance()).getId();
        harness.setHand(player1, List.of(new InvokeTheDivine()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Jousting Lance");
        harness.assertInGraveyard(player1, "Jousting Lance");
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Artifact creatures are legal targets")
    void destroysArtifactCreatureAndGainsLife() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GuardiansOfKoilos()).getId();
        harness.setHand(player1, List.of(new InvokeTheDivine()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Guardians of Koilos");
        harness.assertInGraveyard(player2, "Guardians of Koilos");
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Still gains life when the legal target becomes indestructible")
    void gainsLifeEvenWhenDestructionFails() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GuardiansOfKoilos()).getId();
        harness.setHand(player1, List.of(new InvokeTheDivine()));
        harness.setHand(player2, List.of(new AdamantWill()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, targetId);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Guardians of Koilos");
        harness.assertNotInGraveyard(player2, "Guardians of Koilos");
        harness.assertInGraveyard(player1, "Invoke the Divine");
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }
}
