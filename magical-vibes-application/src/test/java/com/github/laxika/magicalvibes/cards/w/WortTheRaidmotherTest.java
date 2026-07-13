package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WortTheRaidmotherTest extends BaseCardTest {

    private List<Permanent> goblinWarriors(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Goblin Warrior"))
                .toList();
    }

    private boolean stackHasConspireCopy() {
        return gd.stack.stream().anyMatch(e -> e.getEffectsToResolve().stream()
                .anyMatch(fx -> fx instanceof CopyControllerCastSpellEffect));
    }

    @Test
    @DisplayName("ETB creates two 1/1 red and green Goblin Warrior tokens")
    void etbCreatesTwoGoblinWarriorTokens() {
        harness.setHand(player1, List.of(new WortTheRaidmother()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Wort, queue ETB trigger
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> tokens = goblinWarriors(player1);
        assertThat(tokens).hasSize(2);

        Permanent token = tokens.getFirst();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.GOBLIN, CardSubtype.WARRIOR);
        assertThat(token.getCard().getColors()).contains(CardColor.RED, CardColor.GREEN);
    }

    @Test
    @DisplayName("A green instant you cast gains conspire: tapping two color-sharers queues a copy")
    void grantsConspireToGreenInstant() {
        addCreatureReady(player1, new WortTheRaidmother());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears()); // green
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears()); // green
        Permanent targetBears = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, targetBears.getId(),
                List.of(conspireA.getId(), conspireB.getId()));

        assertThat(conspireA.isTapped()).isTrue();
        assertThat(conspireB.isTapped()).isTrue();
        assertThat(stackHasConspireCopy()).isTrue();
    }

    @Test
    @DisplayName("A blue instant you cast does not gain conspire: chosen creatures stay untapped, no copy")
    void doesNotGrantConspireToBlueInstant() {
        addCreatureReady(player1, new WortTheRaidmother());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        Permanent targetBears = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castWithConspire(player1, 0, targetBears.getId(),
                List.of(conspireA.getId(), conspireB.getId()));

        assertThat(conspireA.isTapped()).isFalse();
        assertThat(conspireB.isTapped()).isFalse();
        assertThat(stackHasConspireCopy()).isFalse();
    }
}
