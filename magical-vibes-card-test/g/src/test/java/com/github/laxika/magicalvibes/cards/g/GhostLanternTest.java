package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BindSpirit;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhostLantern.class, BindSpirit.class, GrizzlyBears.class, Plains.class, Shock.class})
class GhostLanternTest extends BaseCardTest {

    @Test
    void adventureReturnsCreatureToHandAndExilesTheCard() {
        GrizzlyBears target = new GrizzlyBears();
        GhostLantern card = new GhostLantern();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCannotTargetNonCreatureCard() {
        Card target = new Plains();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new GhostLantern()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equippedCreatureGetsCounterWhenAnotherCreatureYouControlDies() {
        Permanent lantern = harness.addToBattlefieldAndReturn(player1, new GhostLantern());
        Permanent equippedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        lantern.setAttachedTo(equippedCreature.getId());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killWithShock(player1, fodder.getId());

        assertThat(equippedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerWhenOpponentCreatureDies() {
        Permanent lantern = harness.addToBattlefieldAndReturn(player1, new GhostLantern());
        Permanent equippedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        lantern.setAttachedTo(equippedCreature.getId());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killWithShock(player1, opponentCreature.getId());

        assertThat(equippedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void equipAbilityAttachesToTargetCreature() {
        harness.addToBattlefield(player1, new GhostLantern());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        Permanent lantern = findPermanent(player1, "Ghost Lantern");
        assertThat(lantern.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void artifactFaceCanBeCastFromExileAfterAdventure() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        GhostLantern card = new GhostLantern();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ghost Lantern");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void adventureCannotTargetOpponentGraveyard() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new GhostLantern()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void adventureRequiresCreatureTarget() {
        harness.setHand(player1, List.of(new GhostLantern()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unattachedLanternDoesNotPutCounterOnOtherCreatures() {
        harness.addToBattlefield(player1, new GhostLantern());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killWithShock(player1, fodder.getId());

        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equippedCreatureDeathDoesNotPutCounterOnAnotherCreature() {
        Permanent lantern = harness.addToBattlefieldAndReturn(player1, new GhostLantern());
        Permanent equippedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        lantern.setAttachedTo(equippedCreature.getId());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killWithShock(player1, equippedCreature.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(equippedCreature.getCard());
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(lantern.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void killWithShock(Player caster, java.util.UUID targetId) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }
}
