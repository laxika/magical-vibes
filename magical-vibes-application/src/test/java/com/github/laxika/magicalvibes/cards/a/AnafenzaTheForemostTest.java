package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnafenzaTheForemost.class, GrizzlyBears.class, MindRot.class, Peek.class,
        Shock.class, WrathOfGod.class, Humble.class})
class AnafenzaTheForemostTest extends BaseCardTest {

    @Test
    @DisplayName("Attacks and targets another tapped creature you control for a counter")
    void attacksAndCountersAnotherTappedCreatureYouControl() {
        Permanent anafenza = addCreatureReady(player1, new AnafenzaTheForemost());
        Permanent tappedCreature = addCreatureReady(player1, new GrizzlyBears());
        tappedCreature.tap();
        Permanent untappedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(tappedCreature.getId())
                .doesNotContain(anafenza.getId(), untappedCreature.getId(), opponentCreature.getId());

        harness.handlePermanentChosen(player1, tappedCreature.getId());
        harness.passBothPriorities();

        assertThat(tappedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exiles an opponent-owned nontoken creature instead of letting it die")
    void opponentOwnedNontokenCreatureIsExiledInsteadOfDying() {
        harness.addToBattlefield(player1, new AnafenzaTheForemost());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bearsId);

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(isExiled("Grizzly Bears")).isTrue();
    }

    @Test
    @DisplayName("Does not exile a creature its controller owns")
    void ownCreatureStillGoesToGraveyard() {
        harness.addToBattlefield(player1, new AnafenzaTheForemost());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bearsId);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(isExiled("Grizzly Bears")).isFalse();
    }

    @Test
    @DisplayName("Does not exile a token creature")
    void tokenCreatureIsNotExiled() {
        harness.addToBattlefield(player1, new AnafenzaTheForemost());
        Permanent token = addTokenCreature(player2);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, token.getId());

        assertThat(isExiled("Bear Token")).isFalse();
    }

    @Test
    @DisplayName("Exiles an opponent's discarded creature card but not a noncreature card")
    void opponentCreatureCardDiscardedFromHandIsExiled() {
        harness.addToBattlefield(player1, new AnafenzaTheForemost());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Peek");
        assertThat(isExiled("Grizzly Bears")).isTrue();
        assertThat(isExiled("Peek")).isFalse();
    }

    @Test
    @DisplayName("Can put the attack counter on another creature attacking alongside Anafenza")
    void countersAnotherAttackingCreature() {
        addCreatureReady(player1, new AnafenzaTheForemost());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not put a counter on a target that becomes untapped before resolution")
    void untappedTargetIsIllegalOnResolution() {
        addCreatureReady(player1, new AnafenzaTheForemost());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        bears.untap();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Exiles an opponent-owned creature even when Anafenza's controller controls it")
    void replacementUsesOwnershipRatherThanControl() {
        harness.addToBattlefield(player1, new AnafenzaTheForemost());
        GrizzlyBears card = new GrizzlyBears();
        card.setOwnerId(player2.getId());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, card);
        gd.stolenCreatures.put(bears.getId(), player2.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(isExiled("Grizzly Bears")).isTrue();
    }

    @Test
    @DisplayName("Exiles opposing creatures when Anafenza is destroyed simultaneously with them")
    void replacementAppliesDuringSimultaneousDestruction() {
        harness.addToBattlefield(player1, new AnafenzaTheForemost());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Anafenza, the Foremost");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(isExiled("Grizzly Bears")).isTrue();
    }

    @Test
    @DisplayName("Exiles opposing creatures when Anafenza dies to lethal damage simultaneously")
    void replacementAppliesDuringSimultaneousStateBasedDeaths() {
        Permanent anafenza = harness.addToBattlefieldAndReturn(player1, new AnafenzaTheForemost());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        anafenza.setMarkedDamage(4);
        bears.setMarkedDamage(2);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Anafenza, the Foremost");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(isExiled("Grizzly Bears")).isTrue();
    }

    @Test
    @DisplayName("Stops replacing creature deaths while Anafenza has lost all abilities")
    void noDeathReplacementAfterLosingAbilities() {
        Permanent anafenza = harness.addToBattlefieldAndReturn(player1, new AnafenzaTheForemost());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Humble(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, anafenza.getId());
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(isExiled("Grizzly Bears")).isFalse();
    }

    @Test
    @DisplayName("Stops replacing discarded creature cards while Anafenza has lost all abilities")
    void noDiscardReplacementAfterLosingAbilities() {
        Permanent anafenza = harness.addToBattlefieldAndReturn(player1, new AnafenzaTheForemost());
        harness.setHand(player1, List.of(new Humble(), new MindRot()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new Peek()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, anafenza.getId());
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Peek");
        assertThat(isExiled("Grizzly Bears")).isFalse();
    }

    private Permanent addTokenCreature(Player player) {
        Card tokenCard = new Card();
        tokenCard.setName("Bear Token");
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setManaCost("");
        tokenCard.setToken(true);
        tokenCard.setColor(CardColor.GREEN);
        tokenCard.setPower(2);
        tokenCard.setToughness(2);
        tokenCard.setSubtypes(List.of(CardSubtype.BEAR));
        return harness.addToBattlefieldAndReturn(player, tokenCard);
    }

    private boolean isExiled(String cardName) {
        return gd.exiledCards.stream().anyMatch(exiled -> exiled.card().getName().equals(cardName));
    }

}
