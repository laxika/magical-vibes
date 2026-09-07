package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RelmsSketching.class, AngelsFeather.class, Forest.class, GrizzlyBears.class, Pacifism.class})
class RelmsSketchingTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a token copy of an artifact")
    void createsTokenCopyOfArtifact() {
        harness.addToBattlefield(player2, new AngelsFeather());

        castAndResolve(harness.getPermanentId(player2, "Angel's Feather"));

        assertThat(countTokenCopies(player1, "Angel's Feather")).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates a token copy of a creature")
    void createsTokenCopyOfCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAndResolve(harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(countTokenCopies(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates a token copy of a land")
    void createsTokenCopyOfLand() {
        harness.addToBattlefield(player2, new Forest());

        castAndResolve(harness.getPermanentId(player2, "Forest"));

        assertThat(countTokenCopies(player1, "Forest")).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        Permanent enchantment = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(enchantment);
        harness.setHand(player1, List.of(new RelmsSketching()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land");
    }

    private void castAndResolve(UUID targetId) {
        harness.setHand(player1, List.of(new RelmsSketching()));
        addMana();
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private long countTokenCopies(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .count();
    }
}
