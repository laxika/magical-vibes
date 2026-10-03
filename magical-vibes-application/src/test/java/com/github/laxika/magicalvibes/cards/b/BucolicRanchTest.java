package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.cards.p.PrairieDog;
import com.github.laxika.magicalvibes.cards.t.TrainedArynx;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BucolicRanch.class, Forest.class, NamelessInversion.class, PrairieDog.class, TrainedArynx.class})
class BucolicRanchTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one colorless mana")
    void addsColorlessMana() {
        harness.addToBattlefield(player1, new BucolicRanch());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted mana can cast a Mount spell")
    void restrictedManaCastsMountSpell() {
        harness.addToBattlefield(player1, new BucolicRanch());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "WHITE");

        harness.setHand(player1, List.of(new TrainedArynx()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Trained Arynx");
    }

    @Test
    @DisplayName("Restricted mana cannot cast a non-Mount spell")
    void restrictedManaCannotCastNonMountSpell() {
        harness.addToBattlefield(player1, new BucolicRanch());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "WHITE");

        harness.setHand(player1, List.of(new PrairieDog()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Looks at the top card and offers a Mount for the hand")
    void revealsMountToHand() {
        harness.addToBattlefield(player1, new BucolicRanch());
        TrainedArynx mount = new TrainedArynx();
        harness.setLibrary(player1, List.of(mount));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(mount);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining a Mount reveal offers the bottom choice")
    void decliningMountRevealOffersBottom() {
        harness.addToBattlefield(player1, new BucolicRanch());
        TrainedArynx mount = new TrainedArynx();
        harness.setLibrary(player1, List.of(mount));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(mount);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(mount);
    }

    @Test
    @DisplayName("Offers a non-Mount top card for the library bottom")
    void offersNonMountForBottom() {
        harness.addToBattlefield(player1, new BucolicRanch());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void putsDeclinedMountBelowRemainingCards() {
        harness.addToBattlefield(player1, new BucolicRanch());
        TrainedArynx mount = new TrainedArynx();
        Forest next = new Forest();
        harness.setLibrary(player1, List.of(mount, next));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, mount);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(mount);
    }

    @Test
    void mayLeaveMountOnTopAfterDecliningBothChoices() {
        harness.addToBattlefield(player1, new BucolicRanch());
        TrainedArynx mount = new TrainedArynx();
        Forest next = new Forest();
        harness.setLibrary(player1, List.of(mount, next));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(mount, next);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(mount);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void putsNonMountBelowRemainingCards() {
        harness.addToBattlefield(player1, new BucolicRanch());
        Forest top = new Forest();
        TrainedArynx next = new TrainedArynx();
        harness.setLibrary(player1, List.of(top, next));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(top);
    }

    @Test
    void mayLeaveNonMountOnTop() {
        harness.addToBattlefield(player1, new BucolicRanch());
        Forest top = new Forest();
        TrainedArynx next = new TrainedArynx();
        harness.setLibrary(player1, List.of(top, next));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(top);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryResolvesWithoutOfferingAChoice() {
        harness.addToBattlefield(player1, new BucolicRanch());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanent(player1, "Bucolic Ranch").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void restrictedManaCannotPayForAnActivatedAbility() {
        harness.addToBattlefield(player1, new BucolicRanch());
        harness.addToBattlefield(player1, new BucolicRanch());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "WHITE");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mayPutNoncreatureChangelingIntoHand() {
        harness.addToBattlefield(player1, new BucolicRanch());
        NamelessInversion mount = new NamelessInversion();
        Forest next = new Forest();
        harness.setLibrary(player1, List.of(mount, next));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(mount);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next);
    }

    @Test
    void restrictedManaCanCastNoncreatureChangeling() {
        harness.addToBattlefield(player1, new BucolicRanch());
        harness.addToBattlefield(player2, new PrairieDog());
        harness.setHand(player1, List.of(new NamelessInversion()));
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLACK");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Prairie Dog"));

        harness.assertInGraveyard(player1, "Nameless Inversion");
        harness.assertInGraveyard(player2, "Prairie Dog");
    }
}
