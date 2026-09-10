package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Broodspinner.class, Forest.class, GrizzlyBears.class, Shock.class})
class BroodspinnerTest extends BaseCardTest {

    @Test
    @DisplayName("When Broodspinner enters, it surveils two")
    void entersAndSurveilsTwo() {
        GameData gd = harness.getGameData();
        Card topCard = new GrizzlyBears();
        Card secondCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new Broodspinner()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard, secondCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
    }

    @Test
    @DisplayName("Sacrificing Broodspinner creates one flying Insect per card type in its graveyard")
    void sacrificeCreatesInsectsForCardTypesInGraveyard() {
        Permanent broodspinner = addCreatureReady(player1, new Broodspinner());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(broodspinner);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Insect")).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(broodspinner.getCard());
        Permanent insect = findPermanent(player1, "Insect");
        assertThat(insect.getEffectivePower()).isEqualTo(1);
        assertThat(insect.getEffectiveToughness()).isEqualTo(1);
        assertThat(insect.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(insect.getCard().getSubtypes()).contains(CardSubtype.INSECT);
        assertThat(insect.getCard().getKeywords()).contains(Keyword.FLYING);
    }
}
