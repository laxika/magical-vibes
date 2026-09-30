package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MycoidResurrection.class, GrizzlyBears.class, Forest.class, Shock.class, Zombify.class})
class MycoidResurrectionTest extends BaseCardTest {

    @Test
    void perpetuallyBoostsCreatureCardsByOwnPermanentCountBeforeReturningOne() {
        GrizzlyBears firstBear = new GrizzlyBears();
        GrizzlyBears secondBear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstBear, secondBear, new Forest(), new Shock()));
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new MycoidResurrection(), new Zombify()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        assertThat(choice.validIndices()).map(graveyard::get).extracting(Card::getId)
                .containsExactlyInAnyOrder(firstBear.getId(), secondBear.getId());
        harness.handleGraveyardCardChosen(player1, graveyard.indexOf(firstBear));

        Permanent returnedFirst = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, returnedFirst)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, returnedFirst)).isEqualTo(5);

        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, secondBear.getId());
        resolveAllTriggers();

        Permanent returnedSecond = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(secondBear.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, returnedSecond)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, returnedSecond)).isEqualTo(5);
    }

    @Test
    void doesNotPromptWhenNoCreatureCardIsInTheGraveyard() {
        harness.setGraveyard(player1, List.of(new Forest(), new Shock()));
        harness.setHand(player1, List.of(new MycoidResurrection()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Mycoid Resurrection");
    }
}
