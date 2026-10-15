package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BesottedKnight;
import com.github.laxika.magicalvibes.cards.b.BetrothTheBeast;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.ThreeBlindMice;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShatterTheOath.class, Forest.class, BesottedKnight.class, BetrothTheBeast.class,
        ThreeBlindMice.class})
class ShatterTheOathTest extends BaseCardTest {

    @Test
    void destroysCreatureAndAttachesWickedRoleToYourCreature() {
        Permanent destroyed = harness.addToBattlefieldAndReturn(player2, new BesottedKnight());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BesottedKnight());
        cast(List.of(destroyed.getId(), target.getId()));

        harness.assertNotOnBattlefield(player2, "Besotted Knight");
        Permanent role = findPermanent(player1, "Wicked");
        assertThat(role.getCard().getSubtypes()).contains(CardSubtype.ROLE);
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
    }

    @Test
    void destroysEnchantmentWhenRoleTargetIsOmitted() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new ThreeBlindMice());
        cast(List.of(enchantment.getId()));

        harness.assertNotOnBattlefield(player2, "Three Blind Mice");
        assertThat(findPermanents(player1, "Wicked")).isEmpty();
    }

    @Test
    void cannotTargetALand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ShatterTheOath()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresDestructionTargetEvenWhenNoRoleTargetIsChosen() {
        harness.setHand(player1, List.of(new ShatterTheOath()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.<UUID>of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotResolveWhenOnlyChosenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BesottedKnight());
        harness.setHand(player1, List.of(new ShatterTheOath()));
        addMana();
        harness.castSorcery(player1, 0, List.of(target.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Wicked")).isEmpty();
        harness.assertInGraveyard(player1, "Shatter the Oath");
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotAttachRoleToOpponentsCreature() {
        Permanent destroyed = harness.addToBattlefieldAndReturn(player2, new ThreeBlindMice());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BesottedKnight());
        harness.setHand(player1, List.of(new ShatterTheOath()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(destroyed.getId(), opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canDestroyYourCreatureWithoutChoosingRoleTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BesottedKnight());

        cast(List.of(target.getId()));

        harness.assertNotOnBattlefield(player1, "Besotted Knight");
        harness.assertInGraveyard(player1, "Besotted Knight");
        assertThat(findPermanents(player1, "Wicked")).isEmpty();
    }

    @Test
    void sameCreatureCanBeBothTargetsButNoRoleIsCreatedAfterItIsDestroyed() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BesottedKnight());

        cast(List.of(target.getId(), target.getId()));

        harness.assertNotOnBattlefield(player1, "Besotted Knight");
        assertThat(findPermanents(player1, "Wicked")).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void createsRoleWhenDestructionTargetLeavesBeforeResolution() {
        Permanent destroyed = harness.addToBattlefieldAndReturn(player2, new BesottedKnight());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BesottedKnight());
        harness.setHand(player1, List.of(new ShatterTheOath()));
        addMana();
        harness.castSorcery(player1, 0, List.of(destroyed.getId(), target.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, destroyed));

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Wicked").getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void destroysFirstTargetWhenRoleTargetLeavesBeforeResolution() {
        Permanent destroyed = harness.addToBattlefieldAndReturn(player2, new BesottedKnight());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BesottedKnight());
        harness.setHand(player1, List.of(new ShatterTheOath()));
        addMana();
        harness.castSorcery(player1, 0, List.of(destroyed.getId(), target.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Besotted Knight");
        harness.assertInGraveyard(player2, "Besotted Knight");
        assertThat(findPermanents(player1, "Wicked")).isEmpty();
    }

    @Test
    void destroyingWickedRoleMakesOnlyOpponentLoseLife() {
        Permanent destroyed = harness.addToBattlefieldAndReturn(player2, new BesottedKnight());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BesottedKnight());
        cast(List.of(destroyed.getId(), target.getId()));
        Permanent role = findPermanent(player1, "Wicked");

        cast(List.of(role.getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wicked")).isEmpty();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    void replacingWickedRoleKeepsNewestRoleAndTriggersOldRolesLifeLoss() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new BesottedKnight());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BesottedKnight());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BesottedKnight());
        cast(List.of(first.getId(), target.getId()));
        UUID oldRoleId = findPermanent(player1, "Wicked").getId();

        cast(List.of(second.getId(), target.getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wicked")).hasSize(1);
        Permanent role = findPermanent(player1, "Wicked");
        assertThat(role.getId()).isNotEqualTo(oldRoleId);
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    void creatureDyingAlsoPutsItsRoleIntoGraveyardAndTriggersLifeLoss() {
        Permanent destroyed = harness.addToBattlefieldAndReturn(player2, new BesottedKnight());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BesottedKnight());
        cast(List.of(destroyed.getId(), target.getId()));

        cast(List.of(target.getId()));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Besotted Knight");
        assertThat(findPermanents(player1, "Wicked")).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    private void cast(List<UUID> targets) {
        harness.setHand(player1, List.of(new ShatterTheOath()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, targets);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
