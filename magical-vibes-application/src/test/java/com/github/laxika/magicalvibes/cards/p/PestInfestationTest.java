package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PestInfestation.class, Millstone.class, GloriousAnthem.class, Shock.class,
        GrizzlyBears.class})
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
        harness.setHand(player1, List.of(new PestInfestation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

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

    private List<Permanent> pests(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.PEST))
                .toList();
    }
}
