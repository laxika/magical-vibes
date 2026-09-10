package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CanopySpider;
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

@CardUsed({FugitiveDruid.class, CanopySpider.class, Pacifism.class, SearingTouch.class})
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
}
