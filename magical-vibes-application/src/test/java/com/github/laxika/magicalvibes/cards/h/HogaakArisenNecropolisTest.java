package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HogaakArisenNecropolis.class, GrizzlyBears.class, Shock.class})
class HogaakArisenNecropolisTest extends BaseCardTest {

    @Test
    @DisplayName("Hogaak cannot be cast with mana alone")
    void cannotBeCastWithManaAlone() {
        harness.setHand(player1, List.of(new HogaakArisenNecropolis()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Hogaak can be cast from hand with Convoke and Delve")
    void castsFromHandWithConvokeAndDelve() {
        List<Card> graveyard = List.of(
                new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
        harness.setGraveyard(player1, graveyard);
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HogaakArisenNecropolis()));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId()),
                false, null, null, null, null, List.of(0, 1, 2, 3, 4));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof HogaakArisenNecropolis);
    }

    @Test
    @DisplayName("Hogaak can be cast from the graveyard with Convoke and Delve")
    void castsFromGraveyardWithConvokeAndDelve() {
        List<Card> graveyard = List.of(
                new HogaakArisenNecropolis(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
        harness.setGraveyard(player1, graveyard);
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());

        gs.playFlashbackSpell(gd, player1, 0, null, null, List.of(), List.of(1, 2, 3, 4, 5),
                null, List.of(), null, null, List.of(), Map.of(), List.of(), List.of(), List.of(),
                List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(graveyard.subList(1, 6));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof HogaakArisenNecropolis);
    }
}
