package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GliderStaff;
import com.github.laxika.magicalvibes.cards.o.OtterPenguin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WateryGrasp.class, OtterPenguin.class, GliderStaff.class})
class WateryGraspTest extends BaseCardTest {

    @Test
    @DisplayName("The enchanted creature does not untap while Watery Grasp remains attached")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2);
        creature.tap();
        attachAura(player1, creature);

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Waterbend shuffles the enchanted creature into its owner's library")
    void waterbendShufflesEnchantedCreatureIntoOwnersLibrary() {
        Permanent creature = addCreatureReady(player2);
        Permanent aura = attachAura(player1, creature);
        Permanent first = addCreatureReady(player1);
        Permanent second = addCreatureReady(player1);
        Permanent third = addCreatureReady(player1);
        Permanent fourth = addCreatureReady(player1);
        Permanent fifth = addCreatureReady(player1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        assertThat(fourth.isTapped()).isTrue();
        assertThat(fifth.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerDecks.get(player2.getId())).contains(creature.getOriginalCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
    }

    @Test
    @DisplayName("Waterbend cannot be paid without five generic payments")
    void waterbendRequiresFivePayments() {
        Permanent aura = attachAura(player1, addCreatureReady(player2));
        Permanent first = addCreatureReady(player1);
        Permanent second = addCreatureReady(player1);
        Permanent third = addCreatureReady(player1);
        Permanent fourth = addCreatureReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("waterbend");

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(third.isTapped()).isFalse();
        assertThat(fourth.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
    }

    @Test
    @DisplayName("Casting Watery Grasp attaches it without tapping the creature")
    void castingDoesNotTapEnchantedCreature() {
        Permanent creature = addCreatureReady(player2);
        harness.setHand(player1, java.util.List.of(new WateryGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(aura -> assertThat(aura.getAttachedTo()).isEqualTo(creature.getId()));
    }

    @Test
    @DisplayName("An unrelated creature untaps and the enchanted creature untaps after the Aura leaves")
    void untapRestrictionOnlyAppliesWhileAttached() {
        Permanent enchanted = addCreatureReady(player2);
        Permanent other = addCreatureReady(player2);
        enchanted.tap();
        other.tap();
        Permanent aura = attachAura(player1, enchanted);

        harness.performUntapStep(player2);

        assertThat(enchanted.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getOriginalCard());
        harness.performUntapStep(player2);

        assertThat(enchanted.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Waterbend can be paid entirely with mana without tapping creatures")
    void waterbendCanUseOnlyMana() {
        Permanent creature = addCreatureReady(player2);
        Permanent aura = attachAura(player1, creature);
        Permanent helper = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(helper.isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player2.getId())).contains(creature.getOriginalCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aura.getOriginalCard());
    }

    @Test
    @DisplayName("Waterbend accepts mana and summoning-sick creatures together")
    void waterbendCanMixManaAndCreatureTaps() {
        Permanent creature = addCreatureReady(player2);
        Permanent aura = attachAura(player1, creature);
        Permanent first = addCreatureReady(player1);
        Permanent second = addCreatureReady(player1);
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).contains(creature.getOriginalCard());
    }

    @Test
    @DisplayName("A creature controlled by someone other than its owner goes to its owner's library")
    void shufflesIntoOwnersLibraryRatherThanControllersLibrary() {
        OtterPenguin card = new OtterPenguin();
        card.setOwnerId(player2.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, card);
        Permanent aura = attachAura(player1, creature);
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerDecks.get(player2.getId())).contains(card);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Removing the Aura in response does not stop its activated ability")
    void abilityResolvesAfterAuraLeavesBattlefield() {
        Permanent creature = addCreatureReady(player2);
        Permanent aura = attachAura(player1, creature);
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getOriginalCard());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).contains(creature.getOriginalCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Moving the Aura before resolution makes the ability shuffle the newly enchanted creature")
    void abilityUsesCurrentEnchantedCreatureAtResolution() {
        Permanent original = addCreatureReady(player2);
        Permanent next = addCreatureReady(player2);
        Permanent aura = attachAura(player1, original);
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);

        aura.setAttachedTo(next.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(original).doesNotContain(next);
        assertThat(gd.playerDecks.get(player2.getId())).contains(next.getOriginalCard())
                .doesNotContain(original.getOriginalCard());
    }

    @Test
    @DisplayName("Waterbend can tap artifacts as well as creatures")
    void waterbendCanTapArtifacts() {
        Permanent creature = addCreatureReady(player2);
        Permanent aura = attachAura(player1, creature);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GliderStaff());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).contains(creature.getOriginalCard());
    }

    @Test
    @DisplayName("Waterbend cannot count tapped permanents or an opponent's creatures")
    void waterbendOnlyUsesUntappedPermanentsYouControl() {
        Permanent creature = addCreatureReady(player2);
        Permanent aura = attachAura(player1, creature);
        Permanent tapped = addCreatureReady(player1);
        tapped.tap();
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("waterbend");

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
    }

    @Test
    @DisplayName("A creature that leaves and returns is not shuffled by the old activated ability")
    void abilityDoesNotAffectCreatureThatLeftAndReturned() {
        Permanent creature = addCreatureReady(player2);
        Permanent aura = attachAura(player1, creature);
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        Permanent returned = harness.addToBattlefieldAndReturn(player2, creature.getOriginalCard());
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(returned);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(creature.getOriginalCard());
    }

    private Permanent addCreatureReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new OtterPenguin());
    }

    private Permanent attachAura(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new WateryGrasp());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
