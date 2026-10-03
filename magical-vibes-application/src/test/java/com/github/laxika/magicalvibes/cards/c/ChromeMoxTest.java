package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AzoriusCharm;
import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.f.FangrenHunter;
import com.github.laxika.magicalvibes.cards.g.Ghostfire;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChromeMox.class, FangrenHunter.class, Bonesplitter.class, Mountain.class,
        AzoriusCharm.class, Ghostfire.class, Shatter.class})
class ChromeMoxTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may exile a nonartifact, nonland card and imprint it")
    void etbImprintsEligibleCard() {
        ChromeMox moxCard = new ChromeMox();
        FangrenHunter eligibleCard = new FangrenHunter();
        harness.setHand(player1, List.of(moxCard, eligibleCard, new Bonesplitter(), new Mountain()));
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

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
        FangrenHunter eligibleCard = new FangrenHunter();
        harness.setHand(player1, List.of(moxCard, eligibleCard));
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(eligibleCard);
        Permanent mox = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(gd.getImprintedCard(mox.getCard())).isNull();
    }

    @Test
    @DisplayName("Accepting the ETB imprint with no eligible card does nothing")
    void noEligibleCardLeavesImprintEmpty() {
        ChromeMox moxCard = new ChromeMox();
        Bonesplitter artifactCard = new Bonesplitter();
        Mountain landCard = new Mountain();
        harness.setHand(player1, List.of(moxCard, artifactCard, landCard));
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getImprintedCard(moxCard)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifactCard, landCard);
    }

    @Test
    @DisplayName("Adds mana of a single imprinted card color")
    void addsManaOfImprintedColor() {
        Permanent mox = addMoxWithImprint(new FangrenHunter());

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

    @Test
    @DisplayName("A Chrome Mox that returns to the battlefield has no previous imprint")
    void reenteringMoxDoesNotKeepPreviousImprint() {
        Permanent firstMox = addMoxWithImprint(new FangrenHunter());

        harness.inMutationScope(() -> assertThat(harness.getPermanentRemovalService()
                .removePermanentToHand(gd, firstMox)).isTrue());
        harness.setHand(player1, List.of(firstMox.getCard()));
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The imprint trigger can exile a card after Chrome Mox is destroyed")
    void imprintTriggerResolvesAfterSourceIsDestroyed() {
        FangrenHunter eligibleCard = new FangrenHunter();
        harness.setHand(player1, List.of(new ChromeMox(), eligibleCard));
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent mox = gd.playerBattlefields.get(player1.getId()).get(0);
        harness.castAndResolveInstant(player2, 0, mox.getId());
        harness.assertInGraveyard(player1, "Chrome Mox");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ImprintFromHandChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(eligibleCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(eligibleCard);
    }

    @Test
    @DisplayName("A colorless nonartifact card can be imprinted but produces no mana")
    void colorlessImprintProducesNoMana() {
        Ghostfire eligibleCard = new Ghostfire();
        harness.setHand(player1, List.of(new ChromeMox(), eligibleCard));
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(eligibleCard);
        assertThat(gd.playerBattlefields.get(player1.getId()).get(0).isTapped()).isTrue();
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Mana production stops when the imprinted card leaves exile")
    void imprintedCardLeavingExileProducesNoMana() {
        FangrenHunter imprintedCard = new FangrenHunter();
        Permanent mox = addMoxWithImprint(imprintedCard);
        harness.inMutationScope(() -> {
            assertThat(gd.removeFromExile(imprintedCard.getId())).isTrue();
            gd.addCardToHand(player1.getId(), imprintedCard);
        });

        harness.activateAbility(player1, 0, null, null);

        assertThat(mox.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addMoxWithImprint(Card imprintedCard) {
        ChromeMox moxCard = new ChromeMox();
        gd.setImprintedCard(moxCard, imprintedCard);
        gd.addToExile(player1.getId(), imprintedCard, moxCard.getId());
        return harness.addToBattlefieldAndReturn(player1, moxCard);
    }
}
