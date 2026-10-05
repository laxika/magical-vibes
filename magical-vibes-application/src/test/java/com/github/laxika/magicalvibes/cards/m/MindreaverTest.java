package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Mindreaver.class, GiantGrowth.class, GrizzlyBears.class, Shock.class})
class MindreaverTest extends BaseCardTest {

    @Test
    @DisplayName("Heroic exiles the top three cards of the chosen player's library")
    void heroicExilesTopThreeFromChosenPlayer() {
        Permanent mindreaver = addCreatureReady(player1, new Mindreaver());
        harness.setLibrary(player1, List.of(new Shock(), new GrizzlyBears(), new Shock(), new Shock()));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, mindreaver.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.getCardsExiledByPermanent(mindreaver.getId())).hasSize(3);
    }

    @Test
    @DisplayName("An opponent's spell targeting Mindreaver does not trigger heroic")
    void opponentsSpellDoesNotTriggerHeroic() {
        Permanent mindreaver = addCreatureReady(player1, new Mindreaver());
        harness.forceActivePlayer(player2);
        harness.setLibrary(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, mindreaver.getId());

        assertThat(gd.getCardsExiledByPermanent(mindreaver.getId())).isEmpty();
    }

    @Test
    @DisplayName("The sacrifice ability counters a spell matching an exiled card")
    void sacrificeCountersMatchingSpell() {
        Permanent mindreaver = addCreatureReady(player1, new Mindreaver());
        gd.addToExile(player2.getId(), new Shock(), mindreaver.getId());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Mindreaver");
    }

    @Test
    @DisplayName("The sacrifice ability cannot target a spell with an unmatched name")
    void sacrificeCannotTargetUnmatchedSpell() {
        Permanent mindreaver = addCreatureReady(player1, new Mindreaver());
        gd.addToExile(player2.getId(), new Shock(), mindreaver.getId());
        GiantGrowth giantGrowth = new GiantGrowth();
        harness.setHand(player2, List.of(giantGrowth));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, mindreaver.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, giantGrowth.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("same name as a card exiled with Mindreaver");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(mindreaver.getId()));
    }

    @Test
    @DisplayName("Heroic exiles all remaining cards from a short opponent library")
    void heroicExilesShortOpponentLibrary() {
        Permanent mindreaver = addCreatureReady(player1, new Mindreaver());
        Shock first = new Shock();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player2, List.of(first, second));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, mindreaver.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(mindreaver.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("Heroic does not trigger for a spell targeting another creature")
    void heroicRequiresTargetingThisCreature() {
        Permanent mindreaver = addCreatureReady(player1, new Mindreaver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.getCardsExiledByPermanent(mindreaver.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cards exiled by another Mindreaver do not enable the sacrifice ability")
    void sacrificeOnlyUsesCardsExiledWithThisPermanent() {
        Permanent first = addCreatureReady(player1, new Mindreaver());
        Permanent second = addCreatureReady(player1, new Mindreaver());
        gd.addToExile(player2.getId(), new Shock(), first.getId());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("same name as a card exiled with Mindreaver");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(second.getId()));
    }
}
