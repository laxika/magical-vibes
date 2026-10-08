package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GleefulSabotage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CrimsonWisps;
import com.github.laxika.magicalvibes.cards.p.PiliPala;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WortTheRaidmother.class, GiantGrowth.class, GrizzlyBears.class, Unsummon.class,
        GleefulSabotage.class, CrimsonWisps.class, PiliPala.class})
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
        resolveAllTriggers();

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

        assertThatThrownBy(() -> harness.castWithConspire(player1, 0, targetBears.getId(),
                List.of(conspireA.getId(), conspireB.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("doesn't have conspire");

        assertThat(conspireA.isTapped()).isFalse();
        assertThat(conspireB.isTapped()).isFalse();
        assertThat(stackHasConspireCopy()).isFalse();
    }

    @Test
    void grantedConspireCopyCanChooseAnotherTarget() {
        addCreatureReady(player1, new WortTheRaidmother());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, first.getId(), List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof GiantGrowth).hasSize(1);
    }

    @Test
    void conspireIsOptional() {
        addCreatureReady(player1, new WortTheRaidmother());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, first.getId());
        resolveAllTriggers();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
    }

    @Test
    void opponentsWortDoesNotGrantConspire() {
        addCreatureReady(player2, new WortTheRaidmother());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castWithConspire(player1, 0, first.getId(),
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("doesn't have conspire");
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    void redInstantCanConspireUsingNewlyCreatedTokens() {
        harness.setHand(player1, List.of(new WortTheRaidmother()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        List<Permanent> tokens = goblinWarriors(player1);
        harness.setHand(player1, List.of(new CrimsonWisps()));
        harness.setLibrary(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithConspire(player1, 0, tokens.getFirst().getId(),
                tokens.stream().map(Permanent::getId).toList());

        assertThat(tokens).allMatch(Permanent::isTapped);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void spellWithPrintedConspireCanPayBothInstancesGrantedByWort() {
        addCreatureReady(player1, new WortTheRaidmother());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent third = addCreatureReady(player1, new GrizzlyBears());
        Permanent fourth = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PiliPala());
        harness.setHand(player1, List.of(new GleefulSabotage()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castWithConspire(player1, 0, target.getId(),
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId()));

        assertThat(List.of(first, second, third, fourth)).allMatch(Permanent::isTapped);
        assertThat(gd.stack).hasSize(3);
    }
}
