package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VampireNoble;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TimotharBaronOfBats.class, VampireNoble.class, GrizzlyBears.class, Shock.class})
class TimotharBaronOfBatsTest extends BaseCardTest {

    @Test
    @DisplayName("Paying exiles a dying Vampire and creates a flying Bat")
    void payingExilesVampireAndCreatesBat() {
        addTimotharAndVampire();
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID vampireCardId = findPermanent(player1, "Vampire Noble").getCard().getId();
        killCreatureWithShock(findPermanent(player1, "Vampire Noble").getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(vampireCardId);
        assertThat(findPermanents(player1, "Bat")).hasSize(1);
        Permanent bat = findPermanent(player1, "Bat");
        assertThat(bat.getCard().getPower()).isEqualTo(1);
        assertThat(bat.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining leaves the dying Vampire in its graveyard")
    void decliningLeavesVampireInGraveyard() {
        addTimotharAndVampire();

        killCreatureWithShock(findPermanent(player1, "Vampire Noble").getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vampire Noble");
        assertThat(findPermanents(player1, "Bat")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability ignores non-Vampire deaths")
    void ignoresNonVampireDeaths() {
        harness.addToBattlefield(player1, new TimotharBaronOfBats());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killCreatureWithShock(bears.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findPermanents(player1, "Bat")).isEmpty();
    }

    @Test
    @DisplayName("A Bat hit sacrifices it and returns the exiled Vampire tapped")
    void batCombatDamageReturnsExiledVampireTapped() {
        addTimotharAndVampire();
        harness.addMana(player1, ManaColor.BLACK, 1);
        Permanent vampire = findPermanent(player1, "Vampire Noble");

        killCreatureWithShock(vampire.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent bat = findPermanent(player1, "Bat");
        bat.setSummoningSick(false);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bat)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bat")).isEmpty();
        assertThat(findPermanents(player1, "Vampire Noble")).hasSize(1);
        assertThat(findPermanent(player1, "Vampire Noble").isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void addTimotharAndVampire() {
        harness.addToBattlefield(player1, new TimotharBaronOfBats());
        harness.addToBattlefield(player1, new VampireNoble());
    }

    private void killCreatureWithShock(UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() == null) {
            harness.passBothPriorities();
        }
    }
}
