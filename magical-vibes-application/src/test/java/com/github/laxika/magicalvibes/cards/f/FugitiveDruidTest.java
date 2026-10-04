package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CanopySpider;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.SearingTouch;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FugitiveDruid.class, CanopySpider.class, Counterspell.class, Pacifism.class, SearingTouch.class})
class FugitiveDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Controller draws a card when an opponent's Aura spell targets the Druid")
    void drawsOnOpponentAuraSpell() {
        Permanent druid = addCreatureReady(player1, new FugitiveDruid());

        harness.setHand(player2, List.of(new Pacifism()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.castEnchantment(player2, 0, druid.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Controller draws a card from their own Aura spell too")
    void drawsOnOwnAuraSpell() {
        Permanent druid = addCreatureReady(player1, new FugitiveDruid());

        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.castEnchantment(player1, 0, druid.getId());
        harness.passBothPriorities();

        // Pacifism leaves the hand as it is cast, so the net change is the drawn card minus it.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("A non-Aura spell targeting the Druid does not trigger the draw")
    void noDrawOnNonAuraSpell() {
        Permanent druid = addCreatureReady(player1, new FugitiveDruid());

        harness.setHand(player2, List.of(new SearingTouch()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, druid.getId());

        // Searing Touch alone - no draw trigger stacked on top of it.
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An Aura spell targeting another creature does not trigger the draw")
    void noDrawWhenAuraTargetsAnotherCreature() {
        addCreatureReady(player1, new FugitiveDruid());
        Permanent spider = addCreatureReady(player1, new CanopySpider());

        harness.setHand(player2, List.of(new Pacifism()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.castEnchantment(player2, 0, spider.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("The draw resolves before the Aura enters the battlefield")
    void drawsBeforeAuraResolves() {
        Permanent druid = addCreatureReady(player1, new FugitiveDruid());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new CanopySpider()));
        harness.setHand(player2, List.of(new Pacifism()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0, druid.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Canopy Spider");
        harness.assertNotInHand(player2, "Canopy Spider");
        harness.assertNotOnBattlefield(player2, "Pacifism");
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Pacifism");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Countering the Aura does not counter the Druid's draw trigger")
    void drawsEvenIfAuraIsCountered() {
        Permanent druid = addCreatureReady(player1, new FugitiveDruid());
        Pacifism aura = new Pacifism();
        harness.setHand(player1, List.of(new Counterspell()));
        harness.setLibrary(player1, List.of(new CanopySpider()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(aura));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0, druid.getId());
        harness.castAndResolveInstant(player1, 0, aura.getId());

        harness.assertInGraveyard(player2, "Pacifism");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertInHand(player1, "Canopy Spider");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Pacifism");
    }

    @Test
    @DisplayName("The draw trigger survives the Druid dying before it resolves")
    void drawsEvenIfDruidDies() {
        Permanent druid = addCreatureReady(player1, new FugitiveDruid());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new CanopySpider()));
        harness.setHand(player2, List.of(new Pacifism(), new SearingTouch(), new SearingTouch()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0, druid.getId());
        harness.castAndResolveInstant(player2, 0, druid.getId());
        harness.castAndResolveInstant(player2, 0, druid.getId());

        harness.assertInGraveyard(player1, "Fugitive Druid");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        resolveAllTriggers();

        harness.assertInHand(player1, "Canopy Spider");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Pacifism");
        harness.assertNotOnBattlefield(player2, "Pacifism");
    }
}
