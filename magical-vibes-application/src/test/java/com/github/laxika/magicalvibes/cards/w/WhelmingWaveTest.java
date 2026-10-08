package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FloodtideSerpent;
import com.github.laxika.magicalvibes.cards.n.NyxbornTriton;
import com.github.laxika.magicalvibes.cards.s.SpringleafDrum;
import com.github.laxika.magicalvibes.cards.t.Tromokratis;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhelmingWave.class, GrizzlyBears.class, FloodtideSerpent.class,
        NyxbornTriton.class, SpringleafDrum.class, Tromokratis.class})
class WhelmingWaveTest extends BaseCardTest {

    @Test
    @DisplayName("Returns non-exempt creatures to their owners' hands")
    void returnsNonExemptCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent kraken = addCreatureWithSubtype(player1, CardSubtype.KRAKEN);
        Permanent leviathan = addCreatureWithSubtype(player1, CardSubtype.LEVIATHAN);
        Permanent octopus = addCreatureWithSubtype(player2, CardSubtype.OCTOPUS);
        Permanent serpent = addCreatureWithSubtype(player2, CardSubtype.SERPENT);

        harness.setHand(player1, List.of(new WhelmingWave()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactlyInAnyOrder(kraken, leviathan);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactlyInAnyOrder(octopus, serpent);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getId())
                .contains(ownCreature.getCard().getId());
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getId())
                .contains(opponentCreature.getCard().getId());
    }

    @Test
    @DisplayName("Returns enchantment creatures while leaving real exempt creatures and artifacts")
    void returnsEnchantmentCreaturesAndLeavesOtherPermanents() {
        Permanent triton = addCreatureReady(player1, new NyxbornTriton());
        Permanent kraken = addCreatureReady(player1, new Tromokratis());
        Permanent serpent = addCreatureReady(player2, new FloodtideSerpent());
        Permanent drum = harness.addToBattlefieldAndReturn(player2, new SpringleafDrum());

        harness.setHand(player1, List.of(new WhelmingWave()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(kraken);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactlyInAnyOrder(serpent, drum);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(triton.getCard());
        harness.assertInGraveyard(player1, "Whelming Wave");
    }

    @Test
    @DisplayName("Returns a creature to its owner rather than its current controller")
    void returnsStolenCreatureToOwner() {
        Permanent stolen = addCreatureReady(player2, new NyxbornTriton());
        gd.stolenCreatures.put(stolen.getId(), player1.getId());

        harness.setHand(player1, List.of(new WhelmingWave()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(stolen.getCard());
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(stolen.getCard());
    }

    @Test
    @DisplayName("A nonexempt creature token leaves the battlefield without remaining in hand")
    void removesNonExemptToken() {
        Card token = new NyxbornTriton();
        token.setToken(true);
        addCreatureReady(player2, token);

        harness.setHand(player1, List.of(new WhelmingWave()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(token);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(token);
    }

    private Permanent addCreatureWithSubtype(Player player, CardSubtype subtype) {
        Card card = new GrizzlyBears();
        card.setSubtypes(List.of(subtype));
        return addCreatureReady(player, card);
    }
}
