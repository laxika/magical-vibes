package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.d.DreadOsseosaur;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VisageOfDread.class, DreadOsseosaur.class, DarksteelRelic.class, Forest.class, GrizzlyBears.class})
class VisageOfDreadTest extends BaseCardTest {

    @Test
    void entersAndDiscardsAChosenArtifactOrCreature() {
        Card artifact = new DarksteelRelic();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(artifact, creature, land)));
        harness.setHand(player1, List.of(new VisageOfDread()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0, 1);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(artifact, land);
    }

    @Test
    void craftWithTwoCreaturesReturnsDreadOsseosaurAndMayMill() {
        harness.addToBattlefieldAndReturn(player1, new VisageOfDread());
        Permanent battlefieldCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        addCraftMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isTransformed()
                        && permanent.getCard() instanceof DreadOsseosaur);
        assertThat(gd.findExiledCard(battlefieldCreature.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(graveyardCreature.getId())).isNotNull();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof Forest).hasSize(2);
    }

    @Test
    void attackingMayMillTwoCards() {
        Permanent osseosaur = harness.addToBattlefieldAndReturn(player1, new DreadOsseosaur());
        osseosaur.setSummoningSick(false);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof Forest).hasSize(2);
    }

    private void addCraftMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
