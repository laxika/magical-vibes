package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.ArkOfHunger;
import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GarrisonExcavatorTest extends BaseCardTest {

    @Test
    void createsSpiritWhenCardLeavesYourGraveyard() {
        addReadyExcavator(player1);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    void createsOnlyOneSpiritWhenSeveralCardsLeaveTogether() {
        addReadyExcavator(player1);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears(), new Shock())));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    void doesNotTriggerWhenCardEntersYourGraveyard() {
        addReadyExcavator(player1);
        harness.addToBattlefield(player1, new ArkOfHunger());
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.addFirst(new Shock());

        int excavatorIndex = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ark of Hunger"))
                .findFirst()
                .map(gd.playerBattlefields.get(player1.getId())::indexOf)
                .orElseThrow();
        harness.activateAbility(player1, excavatorIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    private void addReadyExcavator(Player player) {
        harness.addToBattlefield(player, new GarrisonExcavator());
    }
}
