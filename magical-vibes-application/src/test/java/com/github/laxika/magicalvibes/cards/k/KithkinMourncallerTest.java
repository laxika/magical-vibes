package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.ElvishHandservant;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KithkinMourncallerTest extends BaseCardTest {

    // "Whenever an attacking Kithkin or Elf is put into your graveyard from the battlefield,
    //  you may draw a card."

    /** Player1 shocks their own creature; resolve Shock, death, then the death trigger onto/off the stack. */
    private void killWithShock(String targetName) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(player1, targetName);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities(); // resolve Shock -> creature dies -> death trigger
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
}
