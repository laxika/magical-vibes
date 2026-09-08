package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DragonEgg;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloomvineRegent.class, DragonEgg.class, Forest.class, GrizzlyBears.class, Plains.class})
class BloomvineRegentTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 3 life when it enters")
    void gainsLifeWhenItEnters() {
        BloomvineRegent card = new BloomvineRegent();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int lifeBefore = gd.getLife(player1.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == card);
    }

    @Test
    @DisplayName("Gains 3 life when another Dragon you control enters")
    void gainsLifeWhenAnotherDragonEnters() {
        harness.addToBattlefield(player1, new BloomvineRegent());
        int lifeBefore = gd.getLife(player1.getId());

        harness.setHand(player1, List.of(new DragonEgg()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Does not trigger for a non-Dragon creature")
    void doesNotTriggerForNonDragon() {
        harness.addToBattlefield(player1, new BloomvineRegent());
        int lifeBefore = gd.getLife(player1.getId());

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Omen searches for basic Forests, putting one tapped onto the battlefield and one into hand")
    void omenSearchesForBasicForests() {
        Card card = new BloomvineRegent();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Plains()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).hasSize(2);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).allMatch(forest -> forest.getSubtypes().contains(CardSubtype.FOREST));

        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gameData.interaction.activeInteraction()).isNull();
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.FOREST)
                        && permanent.isTapped());
        assertThat(gameData.playerHands.get(player1.getId()))
                .anyMatch(forest -> forest.getSubtypes().contains(CardSubtype.FOREST));
        assertThat(gameData.playerDecks.get(player1.getId())).contains(card);
    }
}
