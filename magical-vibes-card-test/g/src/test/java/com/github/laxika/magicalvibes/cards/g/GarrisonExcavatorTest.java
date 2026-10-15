package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.ArkOfHunger;
import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GarrisonExcavator.class, GrizzlyBears.class, Disentomb.class,
        Reminisce.class, Shock.class, ArkOfHunger.class})
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

        int excavatorIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Ark of Hunger"));
        harness.activateAbility(player1, excavatorIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void createsSpiritWithOracleCharacteristicsWhenNoncreatureLeaves() {
        addReadyExcavator(player1);
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        Card spirit = findPermanent(player1, "Spirit").getCard();
        assertThat(spirit.isToken()).isTrue();
        assertThat(spirit.getPower()).isEqualTo(2);
        assertThat(spirit.getToughness()).isEqualTo(2);
        assertThat(spirit.getColors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
        assertThat(spirit.getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(spirit.getKeywords()).isEmpty();
    }

    @Test
    void doesNotTriggerWhenCardsLeaveOpponentsGraveyard() {
        addReadyExcavator(player1);
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Shock()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }

    @Test
    void doesNotTriggerWhenShufflingEmptyGraveyard() {
        addReadyExcavator(player1);
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    private void addReadyExcavator(Player player) {
        harness.addToBattlefield(player, new GarrisonExcavator());
    }
}
