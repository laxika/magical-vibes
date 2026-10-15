package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CandyTrail;
import com.github.laxika.magicalvibes.cards.c.CandyGrapple;
import com.github.laxika.magicalvibes.cards.f.FerociousWerefox;
import com.github.laxika.magicalvibes.cards.f.Fling;
import com.github.laxika.magicalvibes.cards.m.Mintstrosity;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.h.HamletGlutton;
import com.github.laxika.magicalvibes.cards.u.UpTheBeanstalk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeseechTheMirror.class, CandyTrail.class, Mintstrosity.class, Island.class,
        HamletGlutton.class, BeanstalkWurm.class, FerociousWerefox.class, Fling.class,
        UpTheBeanstalk.class, CandyGrapple.class})
class BeseechTheMirrorTest extends BaseCardTest {

    @Test
    void withoutBargainPutsTheChosenCardIntoHand() {
        Card chosen = new Mintstrosity();
        prepare(chosen);

        harness.castSorcery(player1, 0, 0);
        resolveAndChoose();

        assertInHand(chosen);
        assertThat(gd.findExiledCard(chosen.getId())).isNull();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void bargainedEligibleCardIsExiledFaceDownAndMayBeCast() {
        Card chosen = new Mintstrosity();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        prepare(chosen);

        castBargained(sacrifice);
        harness.passBothPriorities();
        chooseFirstCard();

        ExiledCardEntry exiled = gd.findExiledCard(chosen.getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.faceDown()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mintstrosity");
        assertThat(gd.findExiledCard(chosen.getId())).isNull();
    }

    @Test
    void decliningEligibleFreeCastPutsTheCardIntoHand() {
        Card chosen = new Mintstrosity();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        prepare(chosen);

        castBargained(sacrifice);
        resolveAndChoose();
        harness.handleMayAbilityChosen(player1, false);

        assertInHand(chosen);
        assertThat(gd.findExiledCard(chosen.getId())).isNull();
    }

    @Test
    void bargainedLandAndExpensiveCardGoToHandWithoutAnOffer() {
        for (Card chosen : List.of(new Island(), new HamletGlutton())) {
            Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
            prepare(chosen);

            castBargained(sacrifice);
            resolveAndChoose();

            assertInHand(chosen);
            assertThat(gd.findExiledCard(chosen.getId())).isNull();
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    @Test
    void bargainedCardWithAnEligibleAdventureOffersToCastItDespiteExpensiveCreatureFace() {
        Card chosen = new BeanstalkWurm();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        prepare(chosen);

        castBargained(sacrifice);
        resolveAndChoose();

        assertThat(gd.findExiledCard(chosen.getId())).isNotNull();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertInHand(chosen);
    }

    @Test
    void bargainedSpellCanCastACreatureWithManaValueExactlyFour() {
        Card chosen = new FerociousWerefox();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        prepare(chosen);

        castBargained(sacrifice);
        resolveAndChoose();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Cast Ferocious Werefox");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ferocious Werefox");
        assertThat(gd.findExiledCard(chosen.getId())).isNull();
    }

    @Test
    void mandatorySacrificeCannotBeBypassedWhenCastingTheSearchedSpell() {
        Card chosen = new Fling();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        prepare(chosen);

        castBargained(sacrifice);
        resolveAndChoose();
        harness.handleMayAbilityChosen(player1, true);

        assertInHand(chosen);
        assertThat(gd.findExiledCard(chosen.getId())).isNull();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void bargainCanSacrificeAnEnchantment() {
        Card chosen = new Mintstrosity();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new UpTheBeanstalk());
        prepare(chosen);

        castBargained(sacrifice);
        harness.assertInGraveyard(player1, "Up the Beanstalk");
        resolveAndChoose();
        harness.handleMayAbilityChosen(player1, false);

        assertInHand(chosen);
    }

    @Test
    void uncastableTargetedSpellGoesToHand() {
        Card chosen = new CandyGrapple();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        prepare(chosen);

        castBargained(sacrifice);
        resolveAndChoose();
        harness.handleMayAbilityChosen(player1, true);

        assertInHand(chosen);
        assertThat(gd.findExiledCard(chosen.getId())).isNull();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void unrestrictedSearchCannotFailToFindInANonemptyLibrary() {
        Card chosen = new Mintstrosity();
        prepare(chosen);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        chooseFirstCard();
        assertInHand(chosen);
    }

    @Test
    void emptyLibraryFinishesWithoutAChoice() {
        prepare(new Mintstrosity());
        harness.setLibrary(player1, List.of());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Beseech the Mirror");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void bargainCannotSacrificeANontokenCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Mintstrosity());
        prepare(new Mintstrosity());

        assertThatThrownBy(() -> castBargained(creature)).isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Mintstrosity");
        harness.assertInHand(player1, "Beseech the Mirror");
    }

    @Test
    void bargainCannotSacrificeAnOpponentsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CandyTrail());
        prepare(new Mintstrosity());

        assertThatThrownBy(() -> castBargained(artifact)).isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Candy Trail");
        harness.assertInHand(player1, "Beseech the Mirror");
    }

    private void prepare(Card libraryCard) {
        harness.setHand(player1, List.of(new BeseechTheMirror()));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void resolveAndChoose() {
        harness.passBothPriorities();
        chooseFirstCard();
    }

    private void castBargained(Permanent sacrifice) {
        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, null, sacrifice.getId());
    }

    private void chooseFirstCard() {
        harness.handleCardChosen(player1, 0);
    }

    private void assertInHand(Card card) {
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(handCard -> handCard.getId().equals(card.getId()));
    }
}
