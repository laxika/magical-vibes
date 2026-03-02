package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreepingCorrosionTest extends BaseCardTest {

    private static Card indestructibleArtifact() {
        Card card = new Card();
        card.setName("Darksteel Relic");
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{0}");
        card.setColor(null);
        card.setKeywords(Set.of(Keyword.INDESTRUCTIBLE));
        return card;
    }

    @Test
    @DisplayName("Creeping Corrosion has correct effect configuration")
    void hasCorrectEffectConfiguration() {
        CreepingCorrosion card = new CreepingCorrosion();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(DestroyAllPermanentsEffect.class);
        DestroyAllPermanentsEffect effect = (DestroyAllPermanentsEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.targetTypes()).containsExactly(CardType.ARTIFACT);
        assertThat(effect.onlyOpponents()).isFalse();
        assertThat(effect.cannotBeRegenerated()).isFalse();
    }

    @Test
    @DisplayName("Casting Creeping Corrosion puts it on the stack as a sorcery")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new CreepingCorrosion()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Creeping Corrosion");
    }

    @Test
    @DisplayName("Creeping Corrosion destroys artifacts controlled by both players")
    void destroysArtifactsFromBothPlayers() {
        harness.addToBattlefield(player1, new HowlingMine());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new CreepingCorrosion()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Howling Mine"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Ornithopter"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Howling Mine"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Ornithopter"));
    }

    @Test
    @DisplayName("Creeping Corrosion does not destroy nonartifact permanents")
    void doesNotDestroyNonArtifacts() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CreepingCorrosion()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Artifacts with regeneration shields survive Creeping Corrosion")
    void regenerationShieldsProtectArtifacts() {
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent ornithopter = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ornithopter"))
                .findFirst()
                .orElseThrow();
        ornithopter.setRegenerationShield(1);

        harness.setHand(player2, List.of(new CreepingCorrosion()));
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Ornithopter"));
    }

    @Test
    @DisplayName("Indestructible artifacts survive Creeping Corrosion")
    void indestructibleArtifactsSurvive() {
        harness.addToBattlefield(player2, indestructibleArtifact());
        harness.setHand(player1, List.of(new CreepingCorrosion()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Darksteel Relic"));
    }
}
