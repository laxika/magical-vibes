package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.e.ElvishSkysweeper;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.Leashling;
import com.github.laxika.magicalvibes.cards.s.SelesnyaGuildmage;
import com.github.laxika.magicalvibes.cards.v.ViashinoFangtail;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrownOfConvergence.class, BorosRecruit.class, ElvishSkysweeper.class, Forest.class,
        Leashling.class, SelesnyaGuildmage.class, ViashinoFangtail.class})
class CrownOfConvergenceTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts creatures sharing a color with a creature on top of the library")
    void boostsCreaturesSharingTopCreatureColor() {
        addCrown();
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player1, new ElvishSkysweeper());
        Permanent redCreature = harness.addToBattlefieldAndReturn(player1, new ViashinoFangtail());
        harness.setLibrary(player1, List.of(new ElvishSkysweeper()));

        assertThat(gqs.getEffectivePower(gd, greenCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, greenCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, redCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, redCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boosts creatures sharing either color with a multicolored creature on top")
    void boostsCreaturesSharingEitherColorWithMulticoloredTopCreature() {
        addCrown();
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player1, new ElvishSkysweeper());
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        Permanent redCreature = harness.addToBattlefieldAndReturn(player1, new ViashinoFangtail());
        harness.setLibrary(player1, List.of(new SelesnyaGuildmage()));

        assertThat(gqs.getEffectivePower(gd, greenCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, greenCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, whiteCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, whiteCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, redCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, redCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost creatures when the top card is not a creature")
    void doesNotBoostForNonCreatureTopCard() {
        addCrown();
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player1, new ElvishSkysweeper());
        harness.setLibrary(player1, List.of(new Forest()));

        assertThat(gqs.getEffectivePower(gd, greenCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, greenCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("A colorless creature on top does not boost any creature")
    void doesNotBoostForColorlessCreatureTopCard() {
        addCrown();
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player1, new ElvishSkysweeper());
        harness.setLibrary(player1, List.of(new Leashling()));

        assertThat(gqs.getEffectivePower(gd, greenCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, greenCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not boost creatures controlled by an opponent")
    void onlyBoostsCreaturesControlledByCrownController() {
        addCrown();
        Permanent controllerCreature = harness.addToBattlefieldAndReturn(player1, new ElvishSkysweeper());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ElvishSkysweeper());
        harness.setLibrary(player1, List.of(new ElvishSkysweeper()));

        assertThat(gqs.getEffectivePower(gd, controllerCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, controllerCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Reveals the controller's top library card to both players")
    void revealsControllerTopLibraryCardToBothPlayers() {
        addCrown();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Forest"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Forest"));
    }

    @Test
    @DisplayName("The boost follows the current top card")
    void boostFollowsTopCard() {
        Permanent crown = addCrown();
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player1, new ElvishSkysweeper());
        Permanent redCreature = harness.addToBattlefieldAndReturn(player1, new ViashinoFangtail());
        harness.setLibrary(player1, List.of(new ElvishSkysweeper(), new ViashinoFangtail()));

        assertThat(gqs.getEffectivePower(gd, greenCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, redCreature)).isEqualTo(3);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crown.isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Viashino Fangtail", "Elvish Skysweeper");
        assertThat(gqs.getEffectivePower(gd, greenCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, redCreature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Sharing both colors grants only one boost per Crown")
    void sharingBothColorsGrantsOneBoost() {
        addCrown();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SelesnyaGuildmage());
        harness.setLibrary(player1, List.of(new SelesnyaGuildmage()));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple Crowns each grant their boost")
    void multipleCrownsStack() {
        addCrown();
        addCrown();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ElvishSkysweeper());
        harness.setLibrary(player1, List.of(new ElvishSkysweeper()));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("An empty library grants no boost and the activated ability does nothing")
    void emptyLibraryDoesNotBoostAndCanActivate() {
        addCrown();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ElvishSkysweeper());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped Crown can activate and a single card library keeps the same card")
    void tappedCrownCanActivateWithSingleCardLibrary() {
        Permanent crown = addCrown();
        crown.setTapped(true);
        Card topCard = new ElvishSkysweeper();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(crown.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addCrown() {
        return harness.addToBattlefieldAndReturn(player1, new CrownOfConvergence());
    }
}
