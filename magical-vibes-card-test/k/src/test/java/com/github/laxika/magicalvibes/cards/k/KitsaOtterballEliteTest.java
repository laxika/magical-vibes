package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PearlOfWisdom;
import com.github.laxika.magicalvibes.cards.s.ShoreUp;
import com.github.laxika.magicalvibes.cards.s.SazacapsBrew;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KitsaOtterballElite.class, CounselOfTheSoratami.class, GrizzlyBears.class, Island.class,
        PearlOfWisdom.class, ShoreUp.class, SazacapsBrew.class})
class KitsaOtterballEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability draws a card, then prompts for a discard")
    void drawsThenDiscards() {
        addReadyKitsa();
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Copies a target instant or sorcery spell when Kitsa has enough power")
    void copiesOwnSpellAtPowerThreshold() {
        Permanent kitsa = addReadyKitsa();
        kitsa.setPowerModifier(2);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.activateAbility(player1, 0, 1, null, counsel.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
    }

    @Test
    @DisplayName("Cannot copy a spell while Kitsa has less than three power")
    void cannotCopyBelowPowerThreshold() {
        addReadyKitsa();

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, counsel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power must be 3 or greater");
    }

    @Test
    void discardsTheDrawnCardWhenHandWasEmpty() {
        addReadyKitsa();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island()));

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Island");
    }

    @Test
    void powerReductionAfterActivationDoesNotPreventCopy() {
        Permanent kitsa = addReadyKitsa();
        kitsa.setPowerModifier(2);
        PearlOfWisdom pearl = new PearlOfWisdom();
        harness.setHand(player1, List.of(pearl));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.activateAbility(player1, 0, 1, null, pearl.getId());

        kitsa.setPowerModifier(0);
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
    }

    @Test
    void resolvesOriginalAndCopyWithoutTriggeringProwessForCopy() {
        Permanent kitsa = addReadyKitsa();
        kitsa.setPowerModifier(1);
        PearlOfWisdom pearl = new PearlOfWisdom();
        harness.setHand(player1, List.of(pearl));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kitsa)).isEqualTo(3);
        harness.activateAbility(player1, 0, 1, null, pearl.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gqs.getEffectivePower(gd, kitsa)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(pearl);
    }

    @Test
    void cannotCopyOpponentsSpell() {
        Permanent kitsa = addReadyKitsa();
        kitsa.setPowerModifier(2);
        ShoreUp shore = new ShoreUp();
        Permanent opponentKitsa = addCreatureReady(player2, new KitsaOtterballElite());
        harness.setHand(player2, List.of(shore));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, opponentKitsa.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, shore.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kitsa.isTapped()).isFalse();
    }

    @Test
    void mayRetargetAnInstantCopyWithoutChangingOriginalTarget() {
        Permanent kitsa = addReadyKitsa();
        kitsa.setPowerModifier(2);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        ShoreUp shore = new ShoreUp();
        harness.setHand(player1, List.of(shore));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, kitsa.getId());
        harness.activateAbility(player1, 0, 1, null, shore.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .extracting(StackEntry::getTargetId).containsExactly(target.getId());
        assertThat(gd.stack).filteredOn(entry -> entry.getCard() == shore)
                .extracting(StackEntry::getTargetId).containsExactly(kitsa.getId());
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(kitsa.isTapped()).isFalse();
    }

    @Test
    void vigilanceLeavesKitsaUntappedAfterAttacking() {
        Permanent kitsa = addReadyKitsa();

        declareAttackers(List.of(0));

        assertThat(kitsa.isTapped()).isFalse();
    }

    @Test
    void cannotCopyCreatureSpell() {
        Permanent kitsa = addReadyKitsa();
        kitsa.setPowerModifier(2);
        KitsaOtterballElite creatureSpell = new KitsaOtterballElite();
        harness.setHand(player1, List.of(creatureSpell));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creatureSpell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.getEffectivePower(gd, kitsa)).isEqualTo(3);
    }

    @Test
    void copyPreservesPromisedGiftAndItsBonus() {
        Permanent kitsa = addReadyKitsa();
        kitsa.setPowerModifier(2);
        SazacapsBrew brew = new SazacapsBrew();
        Island discarded = new Island();
        harness.setHand(player1, List.of(brew, discarded));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Island(), new Island()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gs.playCardWithGift(gd, player1, 0, 0, null, null,
                List.of(player2.getId(), kitsa.getId()), 1, true);
        harness.activateAbility(player1, 0, 1, null, brew.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Fish")).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, kitsa)).isEqualTo(8);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded, brew);
    }

    private Permanent addReadyKitsa() {
        return addCreatureReady(player1, new KitsaOtterballElite());
    }
}
