package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.a.AshnodsHarvester;
import com.github.laxika.magicalvibes.cards.s.Soulherder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MeticulousExcavation.class, AshnodsHarvester.class, ArgothianSprite.class, Soulherder.class})
class MeticulousExcavationTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a permanent with unearth even when it was not unearthed")
    void exilesNormallyEnteredPermanentWithUnearth() {
        harness.addToBattlefield(player1, new MeticulousExcavation());
        Permanent observer = harness.addToBattlefieldAndReturn(player1, new Soulherder());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AshnodsHarvester());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ashnod's Harvester");
        harness.assertNotOnBattlefield(player1, "Ashnod's Harvester");
        assertThat(observer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns a permanent without unearth directly to hand without exiling it")
    void doesNotExilePermanentWithoutUnearth() {
        harness.addToBattlefield(player1, new MeticulousExcavation());
        Permanent observer = harness.addToBattlefieldAndReturn(player1, new Soulherder());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Argothian Sprite");
        harness.assertNotOnBattlefield(player1, "Argothian Sprite");
        assertThat(observer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can return itself to hand while another activation is on the stack")
    void canReturnItselfWithAnotherActivationOnStack() {
        Permanent excavation = harness.addToBattlefieldAndReturn(player1, new MeticulousExcavation());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player1, 0, null, excavation.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Meticulous Excavation");
        harness.assertInHand(player1, "Meticulous Excavation");
        harness.assertNotOnBattlefield(player1, "Argothian Sprite");
        harness.assertInHand(player1, "Argothian Sprite");
    }

    @Test
    @DisplayName("Returns a target permanent you control to its owner's hand")
    void returnsTargetPermanentToHand() {
        harness.addToBattlefield(player1, new MeticulousExcavation());
        AshnodsHarvester targetCard = new AshnodsHarvester();
        Permanent target = harness.addToBattlefieldAndReturn(player1, targetCard);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ashnod's Harvester");
        harness.assertInHand(player1, "Ashnod's Harvester");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(targetCard.getId()));
    }

    @Test
    @DisplayName("Exiles an unearthed permanent before returning it to its owner's hand")
    void exilesUnearthedPermanentBeforeReturningItToHand() {
        harness.addToBattlefield(player1, new MeticulousExcavation());
        AshnodsHarvester targetCard = new AshnodsHarvester();
        harness.setGraveyard(player1, List.of(targetCard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent target = findPermanent(player1, "Ashnod's Harvester");
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ashnod's Harvester");
        harness.assertInHand(player1, "Ashnod's Harvester");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(targetCard.getId()));
    }

    @Test
    @DisplayName("Can target only a permanent you control")
    void canTargetOnlyOwnPermanent() {
        harness.addToBattlefield(player1, new MeticulousExcavation());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be activated only during its controller's turn")
    void canBeActivatedOnlyDuringItsControllersTurn() {
        harness.addToBattlefield(player1, new MeticulousExcavation());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
