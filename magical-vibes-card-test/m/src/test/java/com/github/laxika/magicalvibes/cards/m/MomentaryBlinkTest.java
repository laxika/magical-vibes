package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.v.VesuvanShapeshifter;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MomentaryBlink.class, AshcoatBear.class, Island.class, VesuvanShapeshifter.class})
class MomentaryBlinkTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles and immediately returns a creature you control")
    void flickersCreatureYouControl() {
        harness.addToBattlefield(player1, new AshcoatBear());
        harness.setHand(player1, List.of(new MomentaryBlink()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearId = harness.getPermanentId(player1, "Ashcoat Bear");
        harness.castAndResolveInstant(player1, 0, bearId);

        harness.assertOnBattlefield(player1, "Ashcoat Bear");
        assertThat(harness.getPermanentId(player1, "Ashcoat Bear")).isNotEqualTo(bearId);
        Permanent returnedBear = findPermanent(player1, "Ashcoat Bear");
        assertThat(returnedBear.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Returns a controlled creature to its owner's battlefield")
    void returnsStolenCreatureToItsOwner() {
        AshcoatBear stolenCard = new AshcoatBear();
        stolenCard.setOwnerId(player2.getId());
        Permanent stolenBear = harness.addToBattlefieldAndReturn(player1, stolenCard);
        gd.stolenCreatures.put(stolenBear.getId(), player2.getId());
        harness.setHand(player1, List.of(new MomentaryBlink()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, stolenBear.getId());

        harness.assertNotOnBattlefield(player1, "Ashcoat Bear");
        harness.assertOnBattlefield(player2, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Flashback flickers a creature and exiles Momentary Blink")
    void flashbackFlickersCreatureAndExilesSpell() {
        harness.addToBattlefield(player1, new AshcoatBear());
        harness.setGraveyard(player1, List.of(new MomentaryBlink()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID bearId = harness.getPermanentId(player1, "Ashcoat Bear");
        harness.castAndResolveFlashback(player1, 0, bearId);

        harness.assertOnBattlefield(player1, "Ashcoat Bear");
        assertThat(harness.getPermanentId(player1, "Ashcoat Bear")).isNotEqualTo(bearId);
        harness.assertNotInGraveyard(player1, "Momentary Blink");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Momentary Blink"));
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new MomentaryBlink()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearId = harness.getPermanentId(player2, "Ashcoat Bear");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, bearId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new MomentaryBlink()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID islandId = harness.getPermanentId(player1, "Island");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, islandId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    void returnedCreatureIsUntappedAndHasNoOldCounters() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        bear.tap();
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new MomentaryBlink()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        Permanent returnedBear = findPermanent(player1, "Ashcoat Bear");
        assertThat(returnedBear.isTapped()).isFalse();
        assertThat(returnedBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Momentary Blink");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void flashbackWithAnInvalidTargetStillExilesTheSpell() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.setGraveyard(player1, List.of(new MomentaryBlink()));
        harness.setHand(player1, List.of(new MomentaryBlink()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0, bear.getId());
        harness.castAndResolveInstant(player1, 0, bear.getId());
        UUID returnedBearId = harness.getPermanentId(player1, "Ashcoat Bear");
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Ashcoat Bear")).isEqualTo(returnedBearId);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(card -> card.getName().equals("Momentary Blink")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Momentary Blink")).hasSize(1);
    }

    @Test
    void returningFaceDownShapeshifterAllowsItsOwnerToChooseACopy() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        Permanent shapeshifter = harness.addToBattlefieldAndReturn(player1, new VesuvanShapeshifter());
        shapeshifter.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setHand(player1, List.of(new MomentaryBlink()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, shapeshifter.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bear.getId());

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Vesuvan Shapeshifter"))
                .findFirst().orElseThrow();
        assertThat(returned.getId()).isNotEqualTo(shapeshifter.getId());
        assertThat(returned.isFaceDown()).isFalse();
        assertThat(returned.getCard().getName()).isEqualTo("Ashcoat Bear");
        harness.assertNotInGraveyard(player1, "Vesuvan Shapeshifter");
    }
}
