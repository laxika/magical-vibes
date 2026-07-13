package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RhysTheRedeemedTest extends BaseCardTest {

    private List<Permanent> creatureTokensNamed(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals(name))
                .toList();
    }

    private Permanent addCreatureToken(Player player, String name) {
        Card card = new Card();
        card.setToken(true);
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.ELF, CardSubtype.WARRIOR));
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    @Test
    @DisplayName("First ability creates a 1/1 Elf Warrior token")
    void firstAbilityCreatesToken() {
        addCreatureReady(player1, new RhysTheRedeemed());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        List<Permanent> tokens = creatureTokensNamed(player1, "Elf Warrior");
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getCard().getPower()).isEqualTo(1);
        assertThat(tokens.get(0).getCard().getToughness()).isEqualTo(1);
        assertThat(tokens.get(0).getCard().getSubtypes())
                .contains(CardSubtype.ELF, CardSubtype.WARRIOR);
    }

    @Test
    @DisplayName("Second ability creates a copy of each creature token you control")
    void secondAbilityCopiesEachCreatureToken() {
        addCreatureReady(player1, new RhysTheRedeemed());
        addCreatureToken(player1, "Elf Warrior");
        addCreatureToken(player1, "Elf Warrior");
        // A non-token creature must not be copied.
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Two originals + two copies; the snapshot means copies aren't themselves copied.
        assertThat(creatureTokensNamed(player1, "Elf Warrior")).hasSize(4);
        // The non-token creature is untouched (still exactly one Grizzly Bears).
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears")))
                .hasSize(1);
    }

    @Test
    @DisplayName("Second ability with no creature tokens creates nothing")
    void secondAbilityWithNoTokensCreatesNothing() {
        addCreatureReady(player1, new RhysTheRedeemed());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 6);

        int before = gd.playerBattlefields.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(before);
    }
}
