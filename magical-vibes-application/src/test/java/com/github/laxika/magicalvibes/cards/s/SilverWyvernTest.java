package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SilverWyvern.class, SkyshroudFalcon.class, Shock.class, ShamanEnKor.class, SeedsOfStrength.class})
class SilverWyvernTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects a spell targeting only Silver Wyvern to another creature")
    void redirectsSpellTargetingOnlySilverWyvern() {
        Permanent wyvern = harness.addToBattlefieldAndReturn(player1, new SilverWyvern());
        Permanent falcon = harness.addToBattlefieldAndReturn(player1, new SkyshroudFalcon());

        harness.forceActivePlayer(player2);
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, wyvern.getId());
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, falcon.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Silver Wyvern");
        harness.assertNotOnBattlefield(player1, "Skyshroud Falcon");
    }

    @Test
    @DisplayName("Cannot target a spell that targets another creature")
    void cannotTargetSpellThatTargetsAnotherCreature() {
        harness.addToBattlefield(player1, new SilverWyvern());
        Permanent falcon = harness.addToBattlefieldAndReturn(player1, new SkyshroudFalcon());

        harness.forceActivePlayer(player2);
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, falcon.getId());
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Redirects an activated ability targeting only Silver Wyvern")
    void redirectsAbilityTargetingOnlySilverWyvern() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new ShamanEnKor());
        Permanent wyvern = harness.addToBattlefieldAndReturn(player1, new SilverWyvern());
        Permanent falcon = harness.addToBattlefieldAndReturn(player1, new SkyshroudFalcon());

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, 0, null, wyvern.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 1, null, shaman.getCard().getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, falcon.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, shaman.getId());

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .contains(shaman)
                .doesNotContain(falcon);
        assertThat(shaman.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Redirects every target occurrence when a spell targets only Silver Wyvern multiple times")
    void redirectsEveryOccurrenceWhenSpellTargetsSilverWyvernMultipleTimes() {
        Permanent wyvern = harness.addToBattlefieldAndReturn(player1, new SilverWyvern());
        Permanent falcon = harness.addToBattlefieldAndReturn(player1, new SkyshroudFalcon());
        SeedsOfStrength seeds = new SeedsOfStrength();

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(seeds));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, List.of(wyvern.getId(), wyvern.getId(), wyvern.getId()));

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, seeds.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, falcon.getId());
        for (int remaining = 0; remaining < 2
                && harness.getGameData().interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice;
                remaining++) {
            harness.handlePermanentChosen(player1, falcon.getId());
        }
        harness.passBothPriorities();

        assertThat(wyvern.getPowerModifiers()).isZero();
        assertThat(wyvern.getToughnessModifiers()).isZero();
        assertThat(falcon.getPowerModifiers()).isEqualTo(3);
        assertThat(falcon.getToughnessModifiers()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot redirect a spell that targets Silver Wyvern and another creature")
    void cannotRedirectSpellTargetingSilverWyvernAndAnotherCreature() {
        Permanent wyvern = harness.addToBattlefieldAndReturn(player1, new SilverWyvern());
        Permanent falcon = harness.addToBattlefieldAndReturn(player1, new SkyshroudFalcon());
        SeedsOfStrength seeds = new SeedsOfStrength();

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(seeds));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, List.of(wyvern.getId(), falcon.getId(), wyvern.getId()));

        harness.addMana(player1, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, seeds.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot redirect Shock to a player even though Shock can target players")
    void replacementTargetMustBeCreature() {
        Permanent wyvern = harness.addToBattlefieldAndReturn(player1, new SilverWyvern());
        Permanent falcon = harness.addToBattlefieldAndReturn(player2, new SkyshroudFalcon());
        Shock shock = new Shock();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, wyvern.getId());
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = (PendingInteraction.PermanentChoice)
                harness.getGameData().interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(falcon.getId());
        harness.handlePermanentChosen(player1, falcon.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Silver Wyvern");
        harness.assertNotOnBattlefield(player2, "Skyshroud Falcon");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Leaves the spell's target unchanged when no other creature is available")
    void noLegalReplacementLeavesTargetUnchanged() {
        Permanent wyvern = harness.addToBattlefieldAndReturn(player1, new SilverWyvern());
        Shock shock = new Shock();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, wyvern.getId());
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Silver Wyvern");
        assertThat(wyvern.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Replacement targets must obey the original ability's controller restriction")
    void replacementMustBeLegalForOriginalAbility() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new ShamanEnKor());
        Permanent wyvern = harness.addToBattlefieldAndReturn(player1, new SilverWyvern());
        Permanent falcon = harness.addToBattlefieldAndReturn(player2, new SkyshroudFalcon());
        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, 0, null, wyvern.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 1, null, shaman.getCard().getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = (PendingInteraction.PermanentChoice)
                harness.getGameData().interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(shaman.getId()).doesNotContain(falcon.getId());
        harness.handlePermanentChosen(player1, shaman.getId());
        harness.passBothPriorities();
    }
}
