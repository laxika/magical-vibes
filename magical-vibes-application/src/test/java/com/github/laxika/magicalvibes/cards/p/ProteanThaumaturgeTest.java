package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProteanThaumaturge.class, GloriousAnthem.class, GrizzlyBears.class, HillGiant.class,
        NyxbornCourser.class})
class ProteanThaumaturgeTest extends BaseCardTest {

    @Test
    @DisplayName("An enchantment entering under your control lets it become another creature")
    void enchantmentTriggerCopiesAnotherCreature() {
        Permanent thaumaturge = addCreatureReady(player1, new ProteanThaumaturge());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(thaumaturge.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(thaumaturge.getCard().getPower()).isEqualTo(2);
        assertThat(thaumaturge.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The copied creature retains the constellation copy ability")
    void copyRetainsConstellationAbility() {
        Permanent thaumaturge = addCreatureReady(player1, new ProteanThaumaturge());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent hillGiant = addCreatureReady(player2, new HillGiant());
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, hillGiant.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(thaumaturge.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(thaumaturge.getCard().getPower()).isEqualTo(3);
        assertThat(thaumaturge.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The constellation trigger cannot target Protean Thaumaturge itself")
    void triggerCannotTargetItself() {
        Permanent thaumaturge = addCreatureReady(player1, new ProteanThaumaturge());
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(thaumaturge.getCard().getName()).isEqualTo("Protean Thaumaturge");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Constellation can copy the enchantment creature that just entered")
    void copiesEnteringEnchantmentCreature() {
        Permanent thaumaturge = addCreatureReady(player1, new ProteanThaumaturge());
        harness.castFromHand(player1, new NyxbornCourser(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Nyxborn Courser"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(thaumaturge.getCard().getName()).isEqualTo("Nyxborn Courser");
        assertThat(thaumaturge.getCard().getPower()).isEqualTo(2);
        assertThat(thaumaturge.getCard().getToughness()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Declining the copy leaves the creature unchanged and able to trigger again")
    void decliningCopyPreservesConstellation() {
        Permanent thaumaturge = addCreatureReady(player1, new ProteanThaumaturge());
        harness.castFromHand(player1, new NyxbornCourser(), "{1}{W}{W}");
        harness.passBothPriorities();
        var courserId = harness.getPermanentId(player1, "Nyxborn Courser");
        harness.handlePermanentChosen(player1, courserId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(thaumaturge.getCard().getName()).isEqualTo("Protean Thaumaturge");

        harness.castFromHand(player1, new NyxbornCourser(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, courserId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(thaumaturge.getCard().getName()).isEqualTo("Nyxborn Courser");
    }

    @Test
    @DisplayName("An enchantment entering under an opponent's control does not trigger constellation")
    void opponentsEnchantmentDoesNotTrigger() {
        Permanent thaumaturge = addCreatureReady(player2, new ProteanThaumaturge());
        harness.castFromHand(player1, new NyxbornCourser(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(thaumaturge.getCard().getName()).isEqualTo("Protean Thaumaturge");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Copying ignores the target's anthem bonus while the source's anthem still applies")
    void copiesBaseCharacteristicsWithoutCopyingStaticBonuses() {
        Permanent thaumaturge = addCreatureReady(player1, new ProteanThaumaturge());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(thaumaturge.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, thaumaturge)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, thaumaturge)).isEqualTo(3);
    }
}
