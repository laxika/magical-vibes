package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArdentSoldier;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SaprolingInfestationTest extends BaseCardTest {

    @Test
    void createsSaprolingWhenAnyPlayerCastsKickedSpell() {
        harness.addToBattlefield(player1, new SaprolingInfestation());
        harness.setHand(player2, List.of(new ArdentSoldier()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);

        harness.castKickedCreature(player2, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = saprolings(player1);
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getPower()).isEqualTo(1);
        assertThat(tokens.getFirst().getCard().getToughness()).isEqualTo(1);
        assertThat(tokens.getFirst().getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(tokens.getFirst().getCard().getSubtypes()).contains(CardSubtype.SAPROLING);
    }

    @Test
    void doesNotCreateSaprolingForNonKickedSpell() {
        harness.addToBattlefield(player1, new SaprolingInfestation());
        harness.setHand(player1, List.of(new ArdentSoldier()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(saprolings(player1)).isEmpty();
    }

    private List<Permanent> saprolings(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Saproling"))
                .toList();
    }
}
