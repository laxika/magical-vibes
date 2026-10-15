package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.b.Battlegrowth;
import com.github.laxika.magicalvibes.cards.b.BrownOuphe;
import com.github.laxika.magicalvibes.cards.c.ChaliceOfTheVoid;
import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PullFromEternity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThoughtPrison.class, Battlegrowth.class, BrownOuphe.class, AlphaMyr.class,
        CopperMyr.class, Forest.class, Ornithopter.class, ChaliceOfTheVoid.class,
        TelJiladChosen.class, PullFromEternity.class})
class ThoughtPrisonTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability exiles and imprints a chosen nonland card")
    void acceptsImprint() {
        CardChoiceSetup setup = castAndResolveEtb(List.of(new Battlegrowth(), new Forest()));

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(setup.chosenCard());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(setup.landCard());
        assertThat(gd.getImprintedCard(setup.prison())).isSameAs(setup.chosenCard());
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Declining the ETB ability leaves the hand unchanged and does not imprint")
    void declinesImprint() {
        CardChoiceSetup setup = castAndResolveEtb(List.of(new Battlegrowth()));

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(setup.chosenCard());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(setup.chosenCard());
        assertThat(gd.getImprintedCard(setup.prison())).isNull();
    }

    @Test
    @DisplayName("Accepting the ETB ability with no nonland card leaves the card unimprinted")
    void noEligibleCardLeavesImprintEmpty() {
        ThoughtPrison prison = new ThoughtPrison();
        Forest land = new Forest();

        harness.setHand(player1, List.of(prison));
        harness.setHand(player2, List.of(land));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castArtifact(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getImprintedCard(prison)).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("A spell sharing a color with the imprinted card damages its caster")
    void matchingColorDamagesCaster() {
        addPrisonWithImprint(new Battlegrowth());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new BrownOuphe(), "{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("A spell sharing a mana value with the imprinted card damages its caster")
    void matchingManaValueDamagesCaster() {
        addPrisonWithImprint(new AlphaMyr());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new CopperMyr(), "{2}");
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("A matching spell cast by an opponent damages that opponent")
    void matchingOpponentSpellDamagesOpponent() {
        addPrisonWithImprint(new Battlegrowth());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new BrownOuphe(), "{G}");
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A spell sharing neither color nor mana value does not trigger the damage")
    void ignoresNonmatchingSpell() {
        addPrisonWithImprint(new Battlegrowth());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Matching color alone triggers damage even when mana values differ")
    void matchingOnlyColorDamagesCaster() {
        addPrisonWithImprint(new TelJiladChosen());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new BrownOuphe(), "{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Color identity does not make a colorless imprinted card share green")
    void colorIdentityDoesNotCountAsColor() {
        addPrisonWithImprint(new CopperMyr());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new BrownOuphe(), "{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Without an imprinted card spells do not trigger damage")
    void noImprintDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThoughtPrison());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Both X symbols contribute to a spell's mana value on the stack")
    void doubleXSpellMatchesTwiceChosenX() {
        addPrisonWithImprint(new AlphaMyr());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new ChaliceOfTheVoid()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("A double-X spell does not match an imprinted card with mana value X")
    void doubleXSpellDoesNotMatchChosenXAlone() {
        addPrisonWithImprint(new Battlegrowth());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new ChaliceOfTheVoid()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Moving the imprinted card out of exile stops future matching triggers")
    @CardUsed({ThoughtPrison.class, AlphaMyr.class, PullFromEternity.class, CopperMyr.class})
    void imprintedCardLeavingExileStopsTriggers() {
        CardChoiceSetup setup = castAndResolveEtb(List.of(new AlphaMyr()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.setHand(player1, List.of(new PullFromEternity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, setup.chosenCard().getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(setup.chosenCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(setup.chosenCard());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new CopperMyr(), "{2}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    private CardChoiceSetup castAndResolveEtb(List<Card> targetHand) {
        Card chosenCard = targetHand.getFirst();
        Card landCard = targetHand.size() > 1 ? targetHand.get(1) : null;
        ThoughtPrison prison = new ThoughtPrison();

        harness.setHand(player1, List.of(prison));
        harness.setHand(player2, targetHand);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castArtifact(player1, 0, player2.getId());
        resolveAllTriggers();

        return new CardChoiceSetup(prison, chosenCard, landCard);
    }

    private void addPrisonWithImprint(Card imprintedCard) {
        ThoughtPrison prison = new ThoughtPrison();
        harness.addToBattlefield(player1, prison);
        harness.setExile(player2, List.of(imprintedCard));
        gd.setImprintedCard(prison, imprintedCard);
    }

    private record CardChoiceSetup(ThoughtPrison prison,
                                   Card chosenCard,
                                   Card landCard) {
    }
}
