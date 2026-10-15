package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Smother;
import com.github.laxika.magicalvibes.cards.w.WelkinTern;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchonOfRedemption.class, GrizzlyBears.class, WelkinTern.class, Smother.class,
        ArchetypeOfImagination.class})
class ArchonOfRedemptionTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry may gain life equal to its power")
    void ownEntryMayGainLifeEqualToPower() {
        harness.setHand(player1, List.of(new ArchonOfRedemption()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Another flying creature may gain life equal to its power")
    void anotherFlyingCreatureMayGainLifeEqualToPower() {
        harness.addToBattlefield(player1, new ArchonOfRedemption());
        harness.setHand(player1, List.of(new WelkinTern()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).getLast().setPowerModifier(2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("A nonflying creature does not trigger it")
    void nonflyingCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new ArchonOfRedemption());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Its controller may decline the life gain")
    void mayDeclineLifeGain() {
        harness.setHand(player1, List.of(new ArchonOfRedemption()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's flying creature does not trigger it")
    void opposingFlyingCreatureDoesNotTrigger() {
        harness.addToBattlefield(player2, new ArchonOfRedemption());
        harness.setHand(player1, List.of(new WelkinTern()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A departed flying creature contributes its last known power")
    void departedFlyerUsesLastKnownPower() {
        harness.addToBattlefield(player1, new ArchonOfRedemption());
        harness.setHand(player1, List.of(new WelkinTern(), new Smother()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        var tern = gd.playerBattlefields.get(player1.getId()).getLast();
        tern.setPowerModifier(2);

        harness.castAndResolveInstant(player1, 0, tern.getId());
        harness.assertInGraveyard(player1, "Welkin Tern");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Its own entry triggers even when it has lost flying")
    void ownEntryWithoutFlyingStillTriggers() {
        harness.addToBattlefield(player2, new ArchetypeOfImagination());
        harness.setHand(player1, List.of(new ArchonOfRedemption()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 23);
    }
}
