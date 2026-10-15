package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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

@CardUsed({DragonsFire.class, ChandraNalaar.class, DarksteelColossus.class, ShivanDragon.class, Unsummon.class})
class DragonsFireTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage without beholding a Dragon")
    void dealsBaseDamageWithoutBehold() {
        Permanent target = addCreatureReady(player2, new DarksteelColossus());
        harness.setHand(player1, List.of(new DragonsFire()));
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deals damage equal to a Dragon card's power when revealed from hand")
    void usesRevealedDragonCardPower() {
        Permanent target = addCreatureReady(player2, new DarksteelColossus());
        harness.setHand(player1, List.of(new DragonsFire(), new ShivanDragon()));
        addMana();

        harness.castInstantWithBehold(player1, 0, target.getId(), List.of(), List.of(1));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof ShivanDragon);
    }

    @Test
    @DisplayName("Uses the chosen Dragon's power when it is on the battlefield")
    void usesChosenDragonPower() {
        Permanent dragon = addCreatureReady(player1, new ShivanDragon());
        Permanent target = addCreatureReady(player2, new DarksteelColossus());
        harness.setHand(player1, List.of(new DragonsFire()));
        addMana();

        harness.castInstantWithBehold(player1, 0, target.getId(), List.of(dragon.getId()), List.of());
        dragon.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    @DisplayName("Can target a planeswalker")
    void targetsPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new DragonsFire()));
        addMana();

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Uses the chosen Dragon's last power after it returns to hand")
    void usesLastKnownPowerAfterDragonLeaves() {
        Permanent dragon = addCreatureReady(player1, new ShivanDragon());
        Permanent target = addCreatureReady(player2, new DarksteelColossus());
        harness.setHand(player1, List.of(new DragonsFire()));
        harness.setHand(player2, List.of(new Unsummon()));
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstantWithBehold(player1, 0, target.getId(), List.of(dragon.getId()), List.of());
        dragon.setPowerModifier(1);
        harness.castAndResolveInstant(player2, 0, dragon.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Shivan Dragon");
        assertThat(target.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    @DisplayName("A chosen Dragon with zero power deals no damage instead of the base three")
    void zeroPowerReplacesBaseDamage() {
        Permanent dragon = addCreatureReady(player1, new ShivanDragon());
        Permanent target = addCreatureReady(player2, new DarksteelColossus());
        harness.setHand(player1, List.of(new DragonsFire()));
        addMana();

        harness.castInstantWithBehold(player1, 0, target.getId(), List.of(dragon.getId()), List.of());
        dragon.setPowerModifier(-5);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Can reveal a Dragon before the spell in hand without discarding it")
    void revealsDragonBeforeSpellInHand() {
        Permanent target = addCreatureReady(player2, new DarksteelColossus());
        harness.setHand(player1, List.of(new ShivanDragon(), new DragonsFire()));
        addMana();

        harness.castInstantWithBehold(player1, 1, target.getId(), List.of(), List.of(0));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(ShivanDragon.class);
    }

    @Test
    @DisplayName("Cannot choose an opponent's Dragon for the additional cost")
    void cannotChooseOpponentsDragon() {
        Permanent dragon = addCreatureReady(player2, new ShivanDragon());
        Permanent target = addCreatureReady(player2, new DarksteelColossus());
        harness.setHand(player1, List.of(new DragonsFire()));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithBehold(player1, 0, target.getId(),
                List.of(dragon.getId()), List.of())).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot reveal a non-Dragon card for the additional cost")
    void cannotRevealNonDragon() {
        Permanent target = addCreatureReady(player2, new DarksteelColossus());
        harness.setHand(player1, List.of(new DragonsFire(), new DarksteelColossus()));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithBehold(player1, 0, target.getId(),
                List.of(), List.of(1))).isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
