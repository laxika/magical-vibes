package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AzoriusCharm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChromeMoxTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may exile a nonartifact, nonland card and imprint it")
    void etbImprintsEligibleCard() {
        ChromeMox moxCard = new ChromeMox();
        GrizzlyBears eligibleCard = new GrizzlyBears();
        harness.setHand(player1, List.of(moxCard, eligibleCard, new Spellbook(), new Mountain()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ImprintFromHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ImprintFromHandChoice.class).validIndices())
                .containsExactly(0);

        harness.handleCardChosen(player1, 0);

        Permanent mox = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(gd.getImprintedCard(mox.getCard())).isSameAs(eligibleCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(eligibleCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(eligibleCard);
    }

    @Test
    @DisplayName("Declining the ETB imprint leaves the hand unchanged")
    void declineImprintLeavesHandUnchanged() {
        ChromeMox moxCard = new ChromeMox();
        GrizzlyBears eligibleCard = new GrizzlyBears();
        harness.setHand(player1, List.of(moxCard, eligibleCard));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(eligibleCard);
        Permanent mox = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(gd.getImprintedCard(mox.getCard())).isNull();
    }

    @Test
    @DisplayName("Adds mana of a single imprinted card color")
    void addsManaOfImprintedColor() {
        Permanent mox = addMoxWithImprint(new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(mox.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A multicolored imprint restricts the mana choice to its colors")
    void multicoloredImprintRestrictsChoice() {
        addMoxWithImprint(new AzoriusCharm());

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE");

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot produce mana without a card exiled with it")
    void noImprintProducesNoMana() {
        harness.addToBattlefield(player1, new ChromeMox());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addMoxWithImprint(Card imprintedCard) {
        ChromeMox moxCard = new ChromeMox();
        gd.setImprintedCard(moxCard, imprintedCard);
        gd.exiledCards.add(new ExiledCardEntry(imprintedCard, player1.getId(), moxCard.getId()));
        return harness.addToBattlefieldAndReturn(player1, moxCard);
    }
}
