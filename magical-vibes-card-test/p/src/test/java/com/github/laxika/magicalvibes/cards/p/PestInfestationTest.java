package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PestInfestation.class, Millstone.class, GloriousAnthem.class, Shock.class,
        GrizzlyBears.class, Naturalize.class})
class PestInfestationTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys up to X artifacts and enchantments and creates twice X Pests")
    void destroysTargetsAndCreatesTwiceXPests() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        castPestInfestation(2, List.of(artifact.getId(), enchantment.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(artifact.getId())
                        || permanent.getId().equals(enchantment.getId()));
        assertThat(pests(player1)).hasSize(4);
    }

    @Test
    @DisplayName("Each Pest gains its controller 1 life when it dies")
    void pestDeathGainsLife() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        castPestInfestation(1, List.of(artifact.getId()));

        Permanent pest = pests(player1).getFirst();
        int lifeBefore = gd.getLife(player1.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, pest.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(pests(player1)).hasSize(1);
    }

    @Test
    @DisplayName("X=0 creates no Pests and requires no targets")
    void xZeroDoesNothing() {
        castPestInfestation(0, List.of());

        assertThat(pests(player1)).isEmpty();
    }

    @Test
    @DisplayName("A creature cannot be targeted")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2,
                new GrizzlyBears());
        harness.setHand(player1, List.of(new PestInfestation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    private void castPestInfestation(int xValue, List<UUID> targetIds) {
        harness.setHand(player1, List.of(new PestInfestation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue * 2);
        harness.castSorcery(player1, 0, xValue, targetIds);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Choosing no targets still creates twice X Pests")
    void createsPestsWithoutTargets() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());

        castPestInfestation(2, List.of());

        assertThat(pests(player1)).hasSize(4);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
    }

    @Test
    @DisplayName("Choosing fewer than X targets does not reduce the number of Pests")
    void fewerTargetsStillCreatesTwiceXPests() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());

        castPestInfestation(3, List.of(artifact.getId()));

        harness.assertInGraveyard(player1, "Millstone");
        assertThat(pests(player1)).hasSize(6);
    }

    @Test
    @DisplayName("Cannot choose more than X targets")
    void rejectsMoreThanXTargets() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new PestInfestation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1,
                List.of(artifact.getId(), enchantment.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creates no Pests when every chosen target becomes illegal")
    void noPestsWhenAllTargetsLeave() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        harness.setHand(player1, List.of(new PestInfestation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 1, List.of(artifact.getId()));
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player2, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(pests(player1)).isEmpty();
        harness.assertInGraveyard(player1, "Pest Infestation");
    }

    @Test
    @DisplayName("One remaining legal target allows all twice X Pests to be created")
    void remainingTargetAllowsTokenCreation() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new PestInfestation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, 2, List.of(artifact.getId(), enchantment.getId()));
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player2, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Glorious Anthem");
        assertThat(pests(player1)).hasSize(4);
    }

    @Test
    @DisplayName("X greater than 100 allows more than 100 targets")
    void canChooseMoreThanOneHundredTargets() {
        List<UUID> targets = IntStream.range(0, 101)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new Millstone()).getId())
                .toList();

        castPestInfestation(101, targets);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(pests(player1)).hasSize(202);
    }

    private List<Permanent> pests(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.PEST))
                .toList();
    }
}
