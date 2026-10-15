package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.ElvishHandservant;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KithkinMourncaller.class, ElvishHandservant.class, GoldmeadowStalwart.class,
        GrizzlyBears.class, Shock.class, WoodlandChangeling.class})
class KithkinMourncallerTest extends BaseCardTest {

    // "Whenever an attacking Kithkin or Elf is put into your graveyard from the battlefield,
    //  you may draw a card."

    /** Player1 shocks their own creature; resolve Shock, death, then the death trigger onto/off the stack. */
    private void killWithShock(String targetName) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(player1, targetName);
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.passBothPriorities(); // resolve the death trigger (MayEffect prompt)
    }

    private Permanent addAttacking(Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, card);
        perm.setAttacking(true);
        return perm;
    }

    @Test
    @DisplayName("Attacking Elf dying and accepting draws a card")
    void attackingElfDeathDraws() {
        harness.addToBattlefield(player1, new KithkinMourncaller());
        addAttacking(new ElvishHandservant());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        killWithShock("Elvish Handservant");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Attacking Kithkin dying and accepting draws a card")
    void attackingKithkinDeathDraws() {
        harness.addToBattlefield(player1, new KithkinMourncaller());
        addAttacking(new GoldmeadowStalwart());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        killWithShock("Goldmeadow Stalwart");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining the may ability draws no card")
    void decliningDrawsNothing() {
        harness.addToBattlefield(player1, new KithkinMourncaller());
        addAttacking(new ElvishHandservant());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        killWithShock("Elvish Handservant");
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A non-attacking Elf dying does not trigger")
    void nonAttackingElfDoesNotTrigger() {
        harness.addToBattlefield(player1, new KithkinMourncaller());
        harness.addToBattlefield(player1, new ElvishHandservant()); // not attacking
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        killWithShock("Elvish Handservant");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An attacking non-Kithkin, non-Elf creature dying does not trigger")
    void attackingNonMatchingSubtypeDoesNotTrigger() {
        harness.addToBattlefield(player1, new KithkinMourncaller());
        addAttacking(new GrizzlyBears()); // Bear, not Kithkin or Elf
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        killWithShock("Grizzly Bears");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An attacking Mourncaller triggers for its own death")
    void attackingMourncallerDrawsForItsOwnDeath() {
        addAttacking(new KithkinMourncaller());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        killWithShock("Kithkin Mourncaller");

        harness.assertInGraveyard(player1, "Kithkin Mourncaller");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent-owned attacker dying under your control does not trigger")
    void opponentOwnedAttackerDoesNotTrigger() {
        harness.addToBattlefield(player1, new KithkinMourncaller());
        Card elf = new ElvishHandservant();
        elf.setOwnerId(player2.getId());
        addAttacking(elf);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        killWithShock("Elvish Handservant");

        harness.assertInGraveyard(player2, "Elvish Handservant");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Your attacker dying under an opponent's control still triggers")
    void ownedAttackerControlledByOpponentTriggers() {
        harness.addToBattlefield(player1, new KithkinMourncaller());
        Card elf = new ElvishHandservant();
        elf.setOwnerId(player1.getId());
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, elf);
        attacker.setAttacking(true);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Elvish Handservant");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An attacker that is both Kithkin and Elf triggers only once")
    void changelingDeathDrawsOnlyOneCard() {
        harness.addToBattlefield(player1, new KithkinMourncaller());
        addAttacking(new WoodlandChangeling());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        killWithShock("Woodland Changeling");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
