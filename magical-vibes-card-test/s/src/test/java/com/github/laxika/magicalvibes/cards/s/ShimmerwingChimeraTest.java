package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BanishingLight;
import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.cards.p.PheresBandBrawler;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShimmerwingChimera.class, BanishingLight.class, PheresBandBrawler.class, NyxbornColossus.class})
class ShimmerwingChimeraTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to one other enchantment you control during your upkeep")
    void returnsSelectedEnchantment() {
        Permanent chimera = harness.addToBattlefieldAndReturn(player1, new ShimmerwingChimera());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new BanishingLight());
        harness.addToBattlefield(player1, new PheresBandBrawler());
        harness.addToBattlefield(player2, new BanishingLight());

        advanceToUpkeep(player1);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(enchantment.getId());
        assertThat(choice.validPlayerIds()).isEmpty();
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player1, List.of(enchantment.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .doesNotContain(enchantment.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .contains(chimera.getId());
        assertThat(gd.playerHands.get(player1.getId())).contains(enchantment.getCard());
    }

    @Test
    @DisplayName("Can decline returning an enchantment")
    void canDeclineTarget() {
        Permanent chimera = harness.addToBattlefieldAndReturn(player1, new ShimmerwingChimera());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new BanishingLight());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class))
                .isNotNull();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactly(chimera.getId(), enchantment.getId());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(enchantment.getCard());
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new ShimmerwingChimera());
        harness.addToBattlefield(player1, new BanishingLight());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Shimmerwing Chimera");
        harness.assertOnBattlefield(player1, "Banishing Light");
    }

    @Test
    @DisplayName("Resolves with no target when only itself and opposing enchantments are present")
    void resolvesWithoutLegalTarget() {
        harness.addToBattlefield(player1, new ShimmerwingChimera());
        harness.addToBattlefield(player2, new BanishingLight());

        advanceToUpkeep(player1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shimmerwing Chimera");
        harness.assertOnBattlefield(player2, "Banishing Light");
        harness.assertNotInHand(player1, "Shimmerwing Chimera");
    }

    @Test
    @DisplayName("Returns a controlled enchantment to its owner's hand")
    void returnsEnchantmentToOwner() {
        harness.addToBattlefield(player1, new ShimmerwingChimera());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new BanishingLight());
        gd.playerBattlefields.get(player2.getId()).remove(enchantment);
        gd.playerBattlefields.get(player1.getId()).add(enchantment);
        gd.stolenCreatures.put(enchantment.getId(), player2.getId());

        advanceToUpkeep(player1);
        harness.handleMultiplePermanentsChosen(player1, List.of(enchantment.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Banishing Light");
        harness.assertInHand(player2, "Banishing Light");
        harness.assertNotInHand(player1, "Banishing Light");
    }

    @Test
    @DisplayName("Does not return a target that is no longer controlled by the ability's controller")
    void targetChangingControlBecomesIllegal() {
        harness.addToBattlefield(player1, new ShimmerwingChimera());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new BanishingLight());

        advanceToUpkeep(player1);
        harness.handleMultiplePermanentsChosen(player1, List.of(enchantment.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(enchantment);
        gd.playerBattlefields.get(player2.getId()).add(enchantment);
        gd.stolenCreatures.put(enchantment.getId(), player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Banishing Light");
        harness.assertNotInHand(player1, "Banishing Light");
        harness.assertNotInHand(player2, "Banishing Light");
    }

    @Test
    @DisplayName("Can return another enchantment creature")
    void returnsEnchantmentCreature() {
        harness.addToBattlefield(player1, new ShimmerwingChimera());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornColossus());

        advanceToUpkeep(player1);
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nyxborn Colossus");
        harness.assertInHand(player1, "Nyxborn Colossus");
        harness.assertOnBattlefield(player1, "Shimmerwing Chimera");
    }

    @Test
    @DisplayName("The trigger still resolves after Shimmerwing Chimera leaves the battlefield")
    void resolvesAfterSourceLeaves() {
        Permanent chimera = harness.addToBattlefieldAndReturn(player1, new ShimmerwingChimera());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new BanishingLight());

        advanceToUpkeep(player1);
        harness.handleMultiplePermanentsChosen(player1, List.of(enchantment.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, chimera));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Banishing Light");
        harness.assertInHand(player1, "Banishing Light");
        harness.assertInHand(player1, "Shimmerwing Chimera");
    }
}
