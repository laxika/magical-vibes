package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChandraNovicePyromancer;
import com.github.laxika.magicalvibes.cards.g.GoblinBirdGrabber;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaskOfImmolation.class, GoblinBirdGrabber.class, ChandraNovicePyromancer.class})
class MaskOfImmolationTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Mask of Immolation creates and attaches a red Elemental token")
    void enteringCreatesAndAttachesElemental() {
        harness.setHand(player1, List.of(new MaskOfImmolation()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent mask = findPermanent(player1, "Mask of Immolation");
        Permanent elemental = findPermanent(player1, "Elemental");

        assertThat(elemental.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(elemental.getCard().getPower()).isEqualTo(1);
        assertThat(elemental.getCard().getToughness()).isEqualTo(1);
        assertThat(elemental.getCard().getSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(mask.getAttachedTo()).isEqualTo(elemental.getId());
    }

    @Test
    @DisplayName("Equipped creature can sacrifice itself to deal 1 damage to a player")
    void equippedCreatureCanSacrificeItselfToDealDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MaskOfImmolation()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent elemental = findPermanent(player1, "Elemental");
        int elementalIndex = gd.playerBattlefields.get(player1.getId()).indexOf(elemental);

        harness.activateAbility(player1, elementalIndex, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertNotOnBattlefield(player1, "Elemental");
        harness.assertOnBattlefield(player1, "Mask of Immolation");
    }

    @Test
    void sacrificeIsPaidBeforeDamageResolvesAndWorksWhileTapped() {
        castMaskAndResolveTrigger();
        Permanent elemental = findPermanent(player1, "Elemental");
        elemental.tap();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(elemental),
                null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Elemental");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    @Test
    void equippedCreatureCanDealLethalDamageToCreature() {
        harness.addToBattlefield(player2, new GoblinBirdGrabber());
        Permanent target = findPermanent(player2, "Goblin Bird-Grabber");
        castMaskAndResolveTrigger();
        Permanent elemental = findPermanent(player1, "Elemental");

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(elemental),
                null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Goblin Bird-Grabber");
        harness.assertInGraveyard(player2, "Goblin Bird-Grabber");
        harness.assertOnBattlefield(player1, "Mask of Immolation");
    }

    @Test
    void equippedCreatureCanDamagePlaneswalker() {
        harness.addToBattlefield(player2, new ChandraNovicePyromancer());
        Permanent chandra = findPermanent(player2, "Chandra, Novice Pyromancer");
        chandra.setCounterCount(CounterType.LOYALTY, 5);
        castMaskAndResolveTrigger();
        Permanent elemental = findPermanent(player1, "Elemental");

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(elemental),
                null, chandra.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertLife(player2, 20);
    }

    @Test
    void equipMovesMaskAndGrantsAbilityToNewCreature() {
        harness.addToBattlefield(player1, new GoblinBirdGrabber());
        Permanent goblin = findPermanent(player1, "Goblin Bird-Grabber");
        castMaskAndResolveTrigger();
        Permanent mask = findPermanent(player1, "Mask of Immolation");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mask),
                null, goblin.getId());
        harness.passBothPriorities();

        assertThat(mask.getAttachedTo()).isEqualTo(goblin.getId());
        harness.assertOnBattlefield(player1, "Elemental");
        Permanent elemental = findPermanent(player1, "Elemental");
        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(elemental), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(goblin),
                1, null, player2.getId());
        harness.assertInGraveyard(player1, "Goblin Bird-Grabber");
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Elemental");
        harness.assertOnBattlefield(player1, "Mask of Immolation");
        assertThat(mask.getAttachedTo()).isNull();
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GoblinBirdGrabber());
        castMaskAndResolveTrigger();
        Permanent mask = findPermanent(player1, "Mask of Immolation");
        Permanent elemental = findPermanent(player1, "Elemental");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mask), null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(mask.getAttachedTo()).isEqualTo(elemental.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotBeActivatedOutsideMainPhase() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinBirdGrabber());
        castMaskAndResolveTrigger();
        Permanent mask = findPermanent(player1, "Mask of Immolation");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mask), null, goblin.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(mask.getAttachedTo()).isEqualTo(findPermanent(player1, "Elemental").getId());
        assertThat(gd.stack).isEmpty();
    }

    private void castMaskAndResolveTrigger() {
        harness.setHand(player1, List.of(new MaskOfImmolation()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
