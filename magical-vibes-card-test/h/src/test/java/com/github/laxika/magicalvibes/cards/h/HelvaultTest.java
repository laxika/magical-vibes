package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;
import com.github.laxika.magicalvibes.cards.g.GatherTheTownsfolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Helvault.class, DawntreaderElk.class, GatherTheTownsfolk.class})
class HelvaultTest extends BaseCardTest {

    @Test
    @DisplayName("First ability exiles a creature you control, tracked with Helvault")
    void firstAbilityExilesOwnCreature() {
        Permanent helvault = harness.addToBattlefieldAndReturn(player1, new Helvault());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dawntreader Elk");
        assertThat(gd.getCardsExiledByPermanent(helvault.getId()))
                .anyMatch(c -> c.getName().equals("Dawntreader Elk"));
    }

    @Test
    @DisplayName("First ability cannot target a creature you don't control")
    void firstAbilityCannotTargetOpponentCreature() {
        harness.addToBattlefieldAndReturn(player1, new Helvault());
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, enemyBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability exiles a creature you don't control, tracked with Helvault")
    void secondAbilityExilesOpponentCreature() {
        Permanent helvault = harness.addToBattlefieldAndReturn(player1, new Helvault());
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 1, null, enemyBears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dawntreader Elk");
        assertThat(gd.getCardsExiledByPermanent(helvault.getId()))
                .anyMatch(c -> c.getName().equals("Dawntreader Elk"));
    }

    @Test
    @DisplayName("Second ability cannot target a creature you control")
    void secondAbilityCannotTargetOwnCreature() {
        harness.addToBattlefieldAndReturn(player1, new Helvault());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("When Helvault dies, all cards exiled with it return under their owners' control")
    void deathReturnsExiledCardsToOwners() {
        Permanent helvault = harness.addToBattlefieldAndReturn(player1, new Helvault());

        // Exile player1's own creature with the first ability.
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, ownBears.getId());
        harness.passBothPriorities();

        // Untap Helvault so its second {T} ability can be activated this turn.
        helvault.untap();

        // Exile player2's creature with the second ability.
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.activateAbility(player1, 0, 1, null, enemyBears.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(helvault.getId())).hasSize(2);

        // Destroy Helvault — its death trigger goes onto the stack.
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, helvault));
        harness.passBothPriorities();

        // Both creatures return to the battlefield under their owners' control.
        harness.assertOnBattlefield(player1, "Dawntreader Elk");
        harness.assertOnBattlefield(player2, "Dawntreader Elk");

        // Nothing remains tracked with the (now-dead) Helvault.
        assertThat(gd.getCardsExiledByPermanent(helvault.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Helvault");
    }

    @Test
    @DisplayName("Helvault dying with no exiled cards does nothing extra")
    void deathWithNoExiledCards() {
        Permanent helvault = harness.addToBattlefieldAndReturn(player1, new Helvault());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, helvault));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Helvault");
    }

    @Test
    @DisplayName("Helvault cannot target noncreature permanents with either ability")
    void cannotTargetNoncreatures() {
        Permanent helvault = harness.addToBattlefieldAndReturn(player1, new Helvault());
        Permanent otherHelvault = harness.addToBattlefieldAndReturn(player2, new Helvault());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, helvault.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, otherHelvault.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(helvault.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both abilities require an untapped Helvault")
    void tappedHelvaultCannotActivateEitherAbility() {
        Permanent helvault = harness.addToBattlefieldAndReturn(player1, new Helvault());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        Permanent enemyCreature = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk());
        helvault.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, enemyCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The second ability cannot be activated with only six mana")
    void secondAbilityRequiresSevenMana() {
        Permanent helvault = harness.addToBattlefieldAndReturn(player1, new Helvault());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(helvault.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Dawntreader Elk");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A target that leaves before resolution is not exiled")
    void departedTargetIsNotExiled() {
        Permanent helvault = harness.addToBattlefieldAndReturn(player1, new Helvault());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, creature));

        harness.passBothPriorities();

        harness.assertInHand(player1, "Dawntreader Elk");
        assertThat(gd.getCardsExiledByPermanent(helvault.getId())).isEmpty();
        assertThat(helvault.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Destroying Helvault in response returns earlier exiles but not the pending target")
    void deathBeforeExileResolutionDoesNotReturnPendingTarget() {
        Permanent helvault = harness.addToBattlefieldAndReturn(player1, new Helvault());
        DawntreaderElk earlierCard = new DawntreaderElk();
        Permanent earlierCreature = harness.addToBattlefieldAndReturn(player1, earlierCard);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, earlierCreature.getId());
        harness.passBothPriorities();
        helvault.untap();
        DawntreaderElk pendingCard = new DawntreaderElk();
        Permanent pendingCreature = harness.addToBattlefieldAndReturn(player1, pendingCard);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, pendingCreature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, helvault));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(earlierCard.getId()))
                .anyMatch(p -> p.getId().equals(pendingCreature.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(pendingCard.getId()));
        assertThat(gd.findExiledCard(pendingCard.getId())).isNotNull();
        harness.assertInGraveyard(player1, "Helvault");
    }

    @Test
    @DisplayName("Bouncing and replaying Helvault cannot return cards exiled by its earlier instance")
    void replayedHelvaultDoesNotReturnEarlierExiles() {
        harness.setHand(player1, List.of());
        Helvault card = new Helvault();
        Permanent original = harness.addToBattlefieldAndReturn(player1, card);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, original));
        harness.assertInHand(player1, "Helvault");
        harness.assertNotOnBattlefield(player1, "Dawntreader Elk");
        assertThat(gd.stack).isEmpty();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent replayed = findPermanent(player1, "Helvault");
        assertThat(replayed.getId()).isNotEqualTo(original.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, replayed));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dawntreader Elk");
        assertThat(gd.getCardsExiledByPermanent(original.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Exiling Helvault does not return cards exiled with it")
    void exilingHelvaultDoesNotReturnCreatures() {
        Permanent helvault = harness.addToBattlefieldAndReturn(player1, new Helvault());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, helvault));

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Dawntreader Elk");
        assertThat(gd.getCardsExiledByPermanent(helvault.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An exiled creature returns to its owner rather than its previous controller")
    void stolenCreatureReturnsToOwner() {
        Permanent helvault = harness.addToBattlefieldAndReturn(player1, new Helvault());
        DawntreaderElk stolenCard = new DawntreaderElk();
        stolenCard.setOwnerId(player2.getId());
        Permanent stolenCreature = harness.addToBattlefieldAndReturn(player1, stolenCard);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, stolenCreature.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, helvault));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dawntreader Elk");
        harness.assertOnBattlefield(player2, "Dawntreader Elk");
    }

    @Test
    @DisplayName("The first ability requires one mana")
    void firstAbilityRequiresOneMana() {
        Permanent helvault = harness.addToBattlefieldAndReturn(player1, new Helvault());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(helvault.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Dawntreader Elk");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Helvault returns only cards exiled with that particular permanent")
    void differentHelvaultsTrackExilesSeparately() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Helvault());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Helvault());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, firstCreature.getId());
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, 0, null, secondCreature.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dawntreader Elk");
        harness.assertNotOnBattlefield(player2, "Dawntreader Elk");
        assertThat(gd.getCardsExiledByPermanent(second.getId())).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, second));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dawntreader Elk");
    }

    @Test
    @DisplayName("Helvault can exile a token but its graveyard trigger cannot return that token")
    void exiledTokenDoesNotReturn() {
        Permanent helvault = harness.addToBattlefieldAndReturn(player1, new Helvault());
        harness.setHand(player1, List.of(new GatherTheTownsfolk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, (java.util.UUID) null);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(2);
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, token.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(1);
        assertThat(gd.getCardsExiledByPermanent(helvault.getId())).isEmpty();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, helvault));
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(1);
    }
}
