package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MomentaryBlink.class, AshcoatBear.class, Island.class})
class MomentaryBlinkTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles and immediately returns a creature you control")
    void flickersCreatureYouControl() {
        harness.addToBattlefield(player1, new AshcoatBear());
        harness.setHand(player1, List.of(new MomentaryBlink()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearId = harness.getPermanentId(player1, "Ashcoat Bear");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

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
        Permanent stolenBear = new Permanent(stolenCard);
        gd.playerBattlefields.get(player1.getId()).add(stolenBear);
        gd.stolenCreatures.put(stolenBear.getId(), player2.getId());
        harness.setHand(player1, List.of(new MomentaryBlink()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, stolenBear.getId());
        harness.passBothPriorities();

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
        harness.castFlashback(player1, 0, List.of(bearId));
        harness.passBothPriorities();

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
}
