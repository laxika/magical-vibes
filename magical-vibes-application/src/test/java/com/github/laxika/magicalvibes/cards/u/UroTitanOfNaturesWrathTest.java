package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UroTitanOfNaturesWrath.class, Forest.class, GrizzlyBears.class})
class UroTitanOfNaturesWrathTest extends BaseCardTest {

    @Test
    void normalCastSacrificesUroAndResolvesItsTrigger() {
        Forest forest = new Forest();
        setLibrary(new GrizzlyBears());
        harness.setHand(player1, List.of(new UroTitanOfNaturesWrath(), forest));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof UroTitanOfNaturesWrath);
        assertThat(findPermanent(forest).isTapped()).isFalse();
    }

    @Test
    void escapedCastKeepsUroOnTheBattlefield() {
        UroTitanOfNaturesWrath uro = new UroTitanOfNaturesWrath();
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(uro);
        graveyard.addAll(IntStream.range(0, 5).mapToObj(ignored -> new GrizzlyBears()).toList());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new Forest()));
        setLibrary(new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castFromGraveyard(player1, 0, IntStream.rangeClosed(1, 5).boxed().toList());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent escapedUro = findPermanent(uro);
        assertThat(escapedUro).isNotNull();
        assertThat(escapedUro.isEscaped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(findPermanentByName("Forest").isTapped()).isFalse();
    }

    @Test
    void attackingUroResolvesItsTrigger() {
        addCreatureReady(player1, new UroTitanOfNaturesWrath());
        setLibrary(new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(findPermanentByName("Forest").isTapped()).isFalse();
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }

    private Permanent findPermanent(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst()
                .orElse(null);
    }

    private Permanent findPermanentByName(String name) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
