package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeroicSacrifice.class, GrizzlyBears.class, Shock.class, Murder.class, PaladinEnVec.class})
class HeroicSacrificeTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects damage to the chosen creature instead of you and your other creatures")
    void redirectsDamageToChosenCreature() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castHeroicSacrifice(chosen);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(chosen.getMarkedDamage()).isEqualTo(2);
        assertThat(other.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Transfers the dead creature's counters and draws a card")
    void transfersCountersAndDrawsWhenChosenCreatureDies() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        chosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        castHeroicSacrifice(chosen);

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, chosen.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(recipient.getId(), player1.getId());
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Still draws when no creature is available for the optional counter target")
    void drawsWithoutCounterRecipient() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castHeroicSacrifice(chosen);

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, chosen.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HeroicSacrifice()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Protection prevents redirected damage from the original red source")
    void protectionPreventsRedirectedDamage() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new PaladinEnVec());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castHeroicSacrifice(chosen);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Paladin en-Vec");
        assertThat(chosen.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Redirects damage from another controlled creature")
    void redirectsDamageFromOtherCreature() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        chosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castHeroicSacrifice(chosen);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, other.getId());

        assertThat(chosen.getMarkedDamage()).isEqualTo(2);
        assertThat(other.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(chosen, other);
    }

    @Test
    @DisplayName("Can decline the counter target even when a creature is available")
    void canDeclineCounterTarget() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        chosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castHeroicSacrifice(chosen);

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, chosen.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not draw if the chosen counter target becomes illegal")
    void illegalCounterTargetStopsDraw() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        chosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castHeroicSacrifice(chosen);

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, chosen.getId());
        harness.handlePermanentChosen(player1, recipient.getId());

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, recipient.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Damage is no longer redirected after the chosen creature dies")
    void stopsRedirectingAfterChosenCreatureDies() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castHeroicSacrifice(chosen);

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, chosen.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("The death ability draws even when the dying creature had no counters")
    void drawsWithNoCountersAndLegalRecipient() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castHeroicSacrifice(chosen);

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, chosen.getId());
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(recipient.getId()).doesNotContain(opponent.getId());
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not install either effect when the spell's target dies in response")
    void targetDiesBeforeSpellResolves() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new HeroicSacrifice()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, chosen.getId());

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, chosen.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 18);
    }

    private void castHeroicSacrifice(Permanent chosen) {
        harness.setHand(player1, List.of(new HeroicSacrifice()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, chosen.getId());
    }
}
