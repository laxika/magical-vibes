package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.SphereOfTheSuns;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.v.VedalkenAnatomist;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianRevoker.class, VedalkenAnatomist.class, SphereOfTheSuns.class, PlagueMyr.class, TurnToFrog.class})
class PhyrexianRevokerTest extends BaseCardTest {

    @Test
    void castingPutsOnStack() {
        prepareRevokerSpell();
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    void resolvingTriggersCardNameChoice() {
        prepareRevokerSpell();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Phyrexian Revoker");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    void choosingNameSetsOnPermanent() {
        prepareRevokerSpell();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Vedalken Anatomist");
        assertThat(findPermanent(player1, "Phyrexian Revoker").getChosenName()).isEqualTo("Vedalken Anatomist");
    }

    @Test
    void blocksNonManaActivatedAbilities() {
        Permanent revoker = addReadyRevoker(player1, "Vedalken Anatomist");
        addReadyAnatomist(player2);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, revoker.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("can't be activated");
    }

    @Test
    void blocksManaAbilities() {
        addReadyRevoker(player1, "Sphere of the Suns");
        addReadySphere(player2);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("can't be activated")
                .hasMessageContaining("Phyrexian Revoker");
    }

    @Test
    void doesNotBlockDifferentlyNamedCards() {
        Permanent revoker = addReadyRevoker(player1, "Plague Myr");
        addReadyAnatomist(player2);
        harness.activateAbility(player2, 0, null, revoker.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void blocksOwnCardsAbilities() {
        Permanent revoker = addReadyRevoker(player1, "Vedalken Anatomist");
        addReadyAnatomist(player1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, revoker.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("can't be activated");
    }

    @Test
    void abilitiesWorkAfterRevokerRemoved() {
        Permanent revoker = addReadyRevoker(player1, "Vedalken Anatomist");
        Permanent anatomist = addReadyAnatomist(player2);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, anatomist.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("can't be activated");
        gd.playerBattlefields.get(player1.getId()).remove(revoker);
        harness.activateAbility(player2, 0, null, anatomist.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void cannotChooseLandName() {
        prepareRevokerSpell();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handleListChoice(player1, "Forest"))
                .isInstanceOf(IllegalArgumentException.class);
        harness.assertNotOnBattlefield(player1, "Phyrexian Revoker");
    }

    @Test
    void blocksIntrinsicTapManaAbilities() {
        addReadyRevoker(player1, "Plague Myr");
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new PlagueMyr());
        myr.setSummoningSick(false);
        assertThatThrownBy(() -> harness.tapPermanent(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(myr.isTapped()).isFalse();
    }

    @Test
    void losingAbilitiesEndsNonManaAbilityLock() {
        Permanent revoker = addReadyRevoker(player1, "Vedalken Anatomist");
        addReadyAnatomist(player2);
        turnRevokerToFrog(revoker);
        harness.activateAbility(player2, 0, null, revoker.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void losingAbilitiesEndsManaAbilityLock() {
        Permanent revoker = addReadyRevoker(player1, "Sphere of the Suns");
        Permanent sphere = addReadySphere(player2);
        turnRevokerToFrog(revoker);
        harness.activateAbility(player2, 0, null, null);
        assertThat(sphere.isTapped()).isTrue();
        assertThat(sphere.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    private void prepareRevokerSpell() {
        harness.setHand(player1, List.of(new PhyrexianRevoker()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private Permanent addReadyRevoker(Player player, String chosenName) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new PhyrexianRevoker());
        permanent.setChosenName(chosenName);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addReadyAnatomist(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new VedalkenAnatomist());
        permanent.setSummoningSick(false);
        harness.addMana(player, ManaColor.BLUE, 3);
        return permanent;
    }

    private Permanent addReadySphere(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new SphereOfTheSuns());
        permanent.setCounterCount(CounterType.CHARGE, 3);
        return permanent;
    }

    private void turnRevokerToFrog(Permanent revoker) {
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, revoker.getId());
    }
}
