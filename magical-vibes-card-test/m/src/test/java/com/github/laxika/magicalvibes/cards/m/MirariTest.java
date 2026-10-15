package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CallOfTheHerd;
import com.github.laxika.magicalvibes.cards.c.ClawsOfGix;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.d.Divert;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mirari.class, Disenchant.class, CallOfTheHerd.class, Squire.class, ClawsOfGix.class, Divert.class})
class MirariTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant offers to pay {3} to copy it")
    void castingInstantOffersCopy() {
        harness.addToBattlefield(player1, new Mirari());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ClawsOfGix());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(target.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Paying {3} copies the instant")
    void payingCopiesInstant() {
        harness.addToBattlefield(player1, new Mirari());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ClawsOfGix());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(target.getId()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.INSTANT_SPELL))
                .hasSize(2);
    }

    @Test
    @DisplayName("Declining the payment does not copy the instant")
    void decliningDoesNotCopy() {
        harness.addToBattlefield(player1, new Mirari());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ClawsOfGix());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(target.getId()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Casting a creature spell does not offer a copy")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Mirari());
        harness.castFromHand(player1, new Squire(), "{1}{W}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Paying for the copy allows choosing a new legal target")
    void payingAllowsChoosingNewTarget() {
        harness.addToBattlefield(player1, new Mirari());
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new ClawsOfGix());
        Permanent newTarget = harness.addToBattlefieldAndReturn(player2, new ClawsOfGix());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(originalTarget.getId()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(newTarget.getId(), originalTarget.getId());

        harness.handlePermanentChosen(player1, newTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(originalTarget, newTarget);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(originalTarget.getCard(), newTarget.getCard());
    }

    @Test
    @DisplayName("Accepting the payment without three mana does not create a copy")
    void cannotPayForCopy() {
        harness.addToBattlefield(player1, new Mirari());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ClawsOfGix());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(target.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("An opponent's instant does not trigger Mirari")
    void opponentCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new Mirari());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ClawsOfGix());
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Paying {3} copies a sorcery")
    void payingCopiesSorcery() {
        harness.addToBattlefield(player1, new Mirari());
        harness.castFromHand(player1, new CallOfTheHerd(), "{2}{G}");

        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.SORCERY_SPELL))
                .hasSize(2);
    }

    @Test
    @DisplayName("Casting a sorcery also offers the copy")
    void castingSorceryOffersCopy() {
        harness.addToBattlefield(player1, new Mirari());
        harness.castFromHand(player1, new CallOfTheHerd(), "{2}{G}");

        assertThat(gd.stack.stream()
                .anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY))
                .isTrue();
    }

    @Test
    @DisplayName("The copy inherits a target changed before Mirari's trigger resolves")
    void copiesCurrentTargetOfSpell() {
        harness.addToBattlefield(player1, new Mirari());
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new ClawsOfGix());
        Permanent changedTarget = harness.addToBattlefieldAndReturn(player2, new ClawsOfGix());
        Disenchant spell = new Disenchant();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, originalTarget.getId());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Divert()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, spell.getId());
        harness.handlePermanentChosen(player2, changedTarget.getId());

        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(originalTarget)
                .doesNotContain(changedTarget);
    }

    @Test
    @DisplayName("Copying a flashback spell creates two tokens without casting the copy")
    void copiesFlashbackSpellWithoutRetriggering() {
        harness.addToBattlefield(player1, new Mirari());
        CallOfTheHerd spell = new CallOfTheHerd();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(entry -> entry.card())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
