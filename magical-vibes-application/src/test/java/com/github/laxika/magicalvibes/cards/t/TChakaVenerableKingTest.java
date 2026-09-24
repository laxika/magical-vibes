package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TChakaVenerableKing.class, Forest.class, Shock.class, GrizzlyBears.class})
class TChakaVenerableKingTest extends BaseCardTest {

    @Test
    void millsThreeAndMayReturnAland() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new Shock(), new Shock()));

        castTChaka();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void decliningTheMilledCardLeavesItInTheGraveyard() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new Shock(), new Shock()));

        castTChaka();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
    }

    @Test
    void graveyardAbilityMakesYouTheMonarchWhenYouControlYourCommander() {
        addCommanderToBattlefield();
        TChakaVenerableKing tChaka = new TChakaVenerableKing();
        harness.setGraveyard(player1, List.of(tChaka));

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(tChaka);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(tChaka.getId()));
    }

    @Test
    void graveyardAbilityRequiresControlOfYourCommander() {
        harness.setGraveyard(player1, List.of(new TChakaVenerableKing()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castTChaka() {
        harness.setHand(player1, List.of(new TChakaVenerableKing()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addCommanderToBattlefield() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        harness.addToBattlefield(player1, commander);
    }
}
