package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.a.AdamantWill;
import com.github.laxika.magicalvibes.cards.s.ShortSword;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CastDown.class, BalothGorger.class, ArvadTheCursed.class, AdamantWill.class, ShortSword.class})
class CastDownTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Cast Down targeting a nonlegendary creature puts it on stack")
    void castingPutsOnStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalothGorger());

        harness.setHand(player1, List.of(new CastDown()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(CastDown.class);
        assertThat(entry.getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Cannot target a legendary creature")
    void cannotTargetLegendaryCreature() {
        // Add a nonlegendary creature as valid target so spell is playable
        harness.addToBattlefield(player1, new BalothGorger());

        Permanent legendaryCreature = harness.addToBattlefieldAndReturn(player2, new ArvadTheCursed());

        harness.setHand(player1, List.of(new CastDown()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, legendaryCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonlegendary creature");
    }

    @Test
    @DisplayName("Resolving Cast Down destroys target creature and moves it to graveyard")
    void resolvingDestroysTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalothGorger());

        harness.setHand(player1, List.of(new CastDown()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Baloth Gorger");
        harness.assertInGraveyard(player2, "Baloth Gorger");
        harness.assertInGraveyard(player1, "Cast Down");
    }

    @Test
    @DisplayName("Cast Down fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalothGorger());

        harness.setHand(player1, List.of(new CastDown()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Cast Down");
    }

    @Test
    @DisplayName("Cannot target a noncreature artifact")
    void cannotTargetNoncreatureArtifact() {
        harness.addToBattlefield(player2, new BalothGorger());
        Permanent sword = harness.addToBattlefieldAndReturn(player2, new ShortSword());
        harness.setHand(player1, List.of(new CastDown()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, sword.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonlegendary creature");
    }

    @Test
    @DisplayName("Cast Down can destroy its controller's nonlegendary creature")
    void destroysOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BalothGorger());
        harness.setHand(player1, List.of(new CastDown()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player1, "Baloth Gorger");
        harness.assertInGraveyard(player1, "Baloth Gorger");
        harness.assertInGraveyard(player1, "Cast Down");
    }

    @Test
    @DisplayName("An indestructible nonlegendary creature remains a legal target but survives")
    void indestructibleCreatureSurvives() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BalothGorger());
        harness.setHand(player1, List.of(new AdamantWill(), new CastDown()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        harness.assertNotInGraveyard(player1, "Baloth Gorger");
        harness.assertInGraveyard(player1, "Cast Down");
        assertThat(gd.stack).isEmpty();
    }
}
