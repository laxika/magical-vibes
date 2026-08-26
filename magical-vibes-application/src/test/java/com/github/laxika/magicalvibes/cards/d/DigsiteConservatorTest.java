package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DigsiteConservator.class, CounselOfTheSoratami.class, Forest.class, GrizzlyBears.class,
        AirElemental.class, LightningBolt.class})
class DigsiteConservatorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice ability exiles up to four cards from a single graveyard")
    void exilesUpToFourCardsFromSingleGraveyard() {
        Permanent conservator = addReadyConservator(player1);
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        Card card3 = new Forest();
        Card card4 = new AirElemental();
        harness.setGraveyard(player2, new ArrayList<>(List.of(card1, card2, card3, card4)));

        harness.activateAbilityWithGraveyardTargets(player1, battlefieldIndex(conservator), 0,
                List.of(card1.getId(), card2.getId(), card3.getId(), card4.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(conservator);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(card1.getId(), card2.getId(), card3.getId(), card4.getId());
    }

    @Test
    @DisplayName("Sacrifice ability requires all targets to come from one graveyard")
    void targetsMustShareOneGraveyard() {
        Permanent conservator = addReadyConservator(player1);
        Card mine = new GrizzlyBears();
        Card theirs = new LightningBolt();
        harness.setGraveyard(player1, new ArrayList<>(List.of(mine)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(theirs)));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(conservator), 0, List.of(mine.getId(), theirs.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single graveyard");
    }

    @Test
    @DisplayName("Sacrifice ability can only be activated at sorcery speed")
    void abilityRequiresSorcerySpeed() {
        Permanent conservator = addReadyConservator(player1);
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(target)));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(conservator), 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Death trigger may pay four to discover four")
    void deathTriggerDiscoversFourWhenPaid() {
        Permanent conservator = harness.addToBattlefieldAndReturn(player1, new DigsiteConservator());
        CounselOfTheSoratami discovered = new CounselOfTheSoratami();
        Forest land = new Forest();
        AirElemental expensive = new AirElemental();
        GrizzlyBears below = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, expensive, discovered, below));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, conservator));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(discovered);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(land, expensive, below);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private Permanent addReadyConservator(Player player) {
        Permanent permanent = new Permanent(new DigsiteConservator());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
