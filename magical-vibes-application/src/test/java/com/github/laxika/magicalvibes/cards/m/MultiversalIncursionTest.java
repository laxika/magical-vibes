package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BaronHelmutZemo;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.y.YellowjacketHeartlessMarauder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MultiversalIncursion.class, GrizzlyBears.class, BaronHelmutZemo.class,
        Island.class, YellowjacketHeartlessMarauder.class})
class MultiversalIncursionTest extends BaseCardTest {

    @Test
    void copiesEachNontokenCreatureAndSkipsCreatureTokens() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        addCreatureToken(player1, "Token Bear");
        castMultiversalIncursion();

        assertThat(tokensNamed(player1, "Grizzly Bears")).hasSize(1);
        assertThat(tokensNamed(player1, "Token Bear")).hasSize(1);
    }

    @Test
    void copiedLegendaryCreatureIsNotLegendary() {
        addLegendaryCreature(player1);
        castMultiversalIncursion();

        List<Permanent> copies = tokensNamed(player1, "Legendary Bear");
        assertThat(copies).hasSize(1);
        assertThat(copies.get(0).getCard().getSupertypes())
                .doesNotContain(CardSupertype.LEGENDARY);
    }

    private void castMultiversalIncursion() {
        harness.castFromHand(player1, new MultiversalIncursion(), "{5}{U}{U}");
        harness.passBothPriorities();
    }

    @Test
    void copiesOnlyYourCreaturesAndSkipsNoncreaturePermanents() {
        harness.addToBattlefield(player1, new BaronHelmutZemo());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new YellowjacketHeartlessMarauder());

        castMultiversalIncursion();

        assertThat(tokensNamed(player1, "Baron Helmut Zemo")).hasSize(1);
        assertThat(tokensNamed(player1, "Island")).isEmpty();
        assertThat(tokensNamed(player1, "Yellowjacket, Heartless Marauder")).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    void resolvesWithoutCreatures() {
        harness.addToBattlefield(player1, new Island());

        castMultiversalIncursion();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Multiversal Incursion");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copiedCreatureSeesOtherCopiesEnteringSimultaneously() {
        harness.addToBattlefield(player1, new BaronHelmutZemo());
        Permanent original = harness.addToBattlefieldAndReturn(
                player1, new YellowjacketHeartlessMarauder());

        castMultiversalIncursion();

        Permanent copy = tokensNamed(player1, "Yellowjacket, Heartless Marauder").getFirst();
        assertThat(copy.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        for (int i = 0; i < 3; i++) {
            harness.passBothPriorities();
        }

        assertThat(original.getPowerModifier()).isEqualTo(2);
        assertThat(copy.getPowerModifier()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addCreatureToken(Player player, String name) {
        Card card = new Card();
        card.setToken(true);
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addLegendaryCreature(Player player) {
        Card card = new Card();
        card.setName("Legendary Bear");
        card.setType(CardType.CREATURE);
        card.setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        card.setPower(2);
        card.setToughness(2);
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private List<Permanent> tokensNamed(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> name.equals(permanent.getCard().getName()))
                .toList();
    }
}
