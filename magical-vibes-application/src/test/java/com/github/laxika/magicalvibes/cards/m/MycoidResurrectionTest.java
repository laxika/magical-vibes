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

        harness.castAndResolveSorcery(player1, 0, List.of());

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
        harness.castAndResolveSorcery(player1, 0, secondBear.getId());

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

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Mycoid Resurrection");
    }

    @Test
    void repeatedCastsAddBoostsOnlyToCreaturesStillInOwnGraveyard() {
        GrizzlyBears firstBear = new GrizzlyBears();
        GrizzlyBears secondBear = new GrizzlyBears();
        GrizzlyBears opposingBear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstBear, secondBear, new Forest()));
        harness.setGraveyard(player2, List.of(opposingBear));
        harness.setHand(player1, List.of(new MycoidResurrection(), new MycoidResurrection()));
        harness.setHand(player2, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 10);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(firstBear));
        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(secondBear));

        List<Permanent> returned = findPermanents(player1, "Grizzly Bears");
        assertThat(returned).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, returned.get(0))).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, returned.get(0))).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, returned.get(1))).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, returned.get(1))).isEqualTo(7);

        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player2, 0, opposingBear.getId());

        Permanent opposingReturned = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, opposingReturned)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingReturned)).isEqualTo(2);
    }

    @Test
    void perpetualBoostSurvivesDeathAndAnotherReturnToBattlefield() {
        GrizzlyBears bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear));
        harness.setHand(player1, List.of(new MycoidResurrection(), new Shock(), new Shock(), new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(bear));

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, returned.getId());
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, returned.getId());
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");

        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, bear.getId());

        Permanent returnedAgain = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, returnedAgain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returnedAgain)).isEqualTo(3);
    }
}
