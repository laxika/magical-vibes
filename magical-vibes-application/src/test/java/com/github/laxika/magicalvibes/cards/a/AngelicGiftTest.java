package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.m.ManifoldKey;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelicGift.class, GreenwoodSentinel.class, ManifoldKey.class, Unsummon.class})
class AngelicGiftTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Angelic Gift attaches to a creature and draws a card")
    void resolvingAttachesAndDraws() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());

        harness.setHand(player1, List.of(new AngelicGift()));
        harness.setLibrary(player1, List.of(new GreenwoodSentinel()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Angelic Gift")
                        && bears.getId().equals(p.getAttachedTo()));
        harness.assertInHand(player1, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("Enchanted creature has flying")
    void enchantedCreatureHasFlying() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AngelicGift());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Angelic Gift cannot enchant a noncreature permanent")
    void cannotEnchantNoncreature() {
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ManifoldKey());
        harness.setHand(player1, List.of(new AngelicGift()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanting an opponent's creature gives it flying but draws for the Aura controller")
    void enchantingOpponentsCreatureDrawsForAuraController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new AngelicGift()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new ManifoldKey()));
        harness.setLibrary(player2, List.of(new GreenwoodSentinel()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Angelic Gift").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        harness.assertInHand(player1, "Manifold Key");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An Aura whose target leaves before resolution does not enter or draw a card")
    void targetRemovedBeforeResolutionDoesNotDraw() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new AngelicGift()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.setLibrary(player1, List.of(new ManifoldKey()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        gs.passPriority(gd, player1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Greenwood Sentinel");
        harness.assertInGraveyard(player1, "Angelic Gift");
        harness.assertNotOnBattlefield(player1, "Angelic Gift");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The draw trigger resolves even if the enchanted creature and Aura leave first")
    void drawTriggerSurvivesAuraLeavingBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new AngelicGift()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.setLibrary(player1, List.of(new ManifoldKey()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Angelic Gift");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        gs.passPriority(gd, player1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Angelic Gift");
        harness.assertNotOnBattlefield(player1, "Angelic Gift");
        harness.assertInHand(player2, "Greenwood Sentinel");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Manifold Key");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
