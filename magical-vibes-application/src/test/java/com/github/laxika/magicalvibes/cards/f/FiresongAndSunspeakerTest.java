package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.c.ColdWaterSnapper;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WhitesunsPassage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.d.DeepFreeze;
import com.github.laxika.magicalvibes.cards.s.SoothingBalm;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FiresongAndSunspeaker.class, Shock.class, GrizzlyBears.class, WhitesunsPassage.class,
        SoothingBalm.class, DeepFreeze.class, ColdWaterSnapper.class})
class FiresongAndSunspeakerTest extends BaseCardTest {

    @Test
    @DisplayName("Red instant targeting player: deals damage + controller gains life from spell lifelink")
    void redInstantTargetingPlayerGrantsLifelink() {
        harness.addToBattlefield(player1, new FiresongAndSunspeaker());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Shock deals 2 damage to player2
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        // Spell lifelink: player1 gains 2 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Red instant targeting creature: deals damage + controller gains life from spell lifelink")
    void redInstantTargetingCreatureGrantsLifelink() {
        harness.addToBattlefield(player1, new FiresongAndSunspeaker());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearsId);

        // Shock deals 2 damage to Grizzly Bears (kills it)
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        // Spell lifelink: player1 gains 2 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("White instant life gain triggers ability 2: deals 3 damage to chosen target")
    void whiteInstantLifeGainTriggersDealDamage() {
        harness.addToBattlefield(player1, new FiresongAndSunspeaker());
        harness.setHand(player1, List.of(new WhitesunsPassage()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castAndResolveInstant(player1, 0);

        // Whitesun's Passage gains 5 life and triggers ability 2
        // Now we need to choose a target for the triggered ability
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);

        // Choose player2 as the target for the 3 damage
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        // Firesong and Sunspeaker deals 3 damage to player2
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Non-red spell does not get lifelink")
    void nonRedSpellDoesNotGetLifelink() {
        harness.addToBattlefield(player1, new FiresongAndSunspeaker());
        harness.setHand(player1, List.of(new WhitesunsPassage()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 20);
        harness.castAndResolveInstant(player1, 0);

        // Whitesun's Passage gains 5 life (it's white, not red, so no lifelink bonus)
        // The only life gain is the 5 from the spell itself
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
    }

    @Test
    @DisplayName("Non-white spell life gain (from lifelink) does not trigger ability 2")
    void nonWhiteSpellLifelinkDoesNotTriggerAbility2() {
        // Shock is red, not white. Its lifelink life gain should NOT trigger ability 2.
        harness.addToBattlefield(player1, new FiresongAndSunspeaker());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Shock deals 2 damage, lifelink grants 2 life
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);

        // No pending trigger targets â€” ability 2 should not have triggered
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.LifeGainTriggerAnyTarget.class)).isFalse();
        // Stack should be empty (no triggered ability was queued)
        assertThat(gd.stack).isEmpty();
    }
    @Test
    @DisplayName("A targeted white life-gain spell triggers even when an opponent controls it")
    void opponentWhiteSpellLifeGainTriggersDamage() {
        harness.addToBattlefield(player1, new FiresongAndSunspeaker());
        harness.setHand(player2, List.of(new SoothingBalm()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 25);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Removing Firesong and Sunspeaker's abilities removes its spell lifelink grant")
    void losingAbilitiesRemovesSpellLifelink() {
        var firesong = harness.addToBattlefieldAndReturn(player1, new FiresongAndSunspeaker());
        harness.setHand(player1, List.of(new DeepFreeze(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castEnchantment(player1, 0, firesong.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("White spell life gain can cause Firesong and Sunspeaker to damage a creature")
    void lifeGainTriggerCanTargetCreature() {
        harness.addToBattlefield(player1, new FiresongAndSunspeaker());
        var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WhitesunsPassage()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 25);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opposing red spells do not receive lifelink")
    void opposingRedSpellDoesNotReceiveLifelink() {
        harness.addToBattlefield(player1, new FiresongAndSunspeaker());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Life-gain trigger excludes opposing hexproof creatures from target choices")
    void lifeGainTriggerCannotTargetOpposingHexproofCreature() {
        harness.addToBattlefield(player1, new FiresongAndSunspeaker());
        var snapper = harness.addToBattlefieldAndReturn(player2, new ColdWaterSnapper());
        harness.setHand(player1, List.of(new WhitesunsPassage()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);

        var choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).doesNotContain(snapper.getId());
    }
}
