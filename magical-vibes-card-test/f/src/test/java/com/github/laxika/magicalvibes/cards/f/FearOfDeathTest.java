package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DawnhartDisciple;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FearOfDeath.class, Forest.class, DawnhartDisciple.class})
class FearOfDeathTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, its controller mills two cards")
    void controllerMillsTwoCards() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        int graveyardSizeBefore = gd.playerGraveyards.get(player1.getId()).size();

        castAura(player1, bears.getId());

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardSizeBefore + 2);
    }

    @Test
    @DisplayName("Enchanted creature gets -1/-0 for each card in the Aura controller's graveyard")
    void debuffUsesAuraControllersGraveyard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new DawnhartDisciple());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FearOfDeath());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Debuff updates when the Aura controller's graveyard changes")
    void debuffUpdatesDynamically() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FearOfDeath());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.setGraveyard(player1, List.of(new Forest()));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new FearOfDeath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Mill waits for the enters trigger and affects only the Aura controller")
    void millIsSeparateTriggerForAuraController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DawnhartDisciple());
        Forest first = new Forest();
        DawnhartDisciple second = new DawnhartDisciple();
        Forest third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new Forest(), new Forest(), new Forest()));
        int opponentLibrarySize = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(new FearOfDeath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibrarySize);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gqs.getEffectivePower(gd, creature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mills the only remaining card when the library has fewer than two cards")
    void millsShortLibrary() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());
        Forest lastCard = new Forest();
        harness.setLibrary(player1, List.of(lastCard));
        harness.setGraveyard(player1, List.of());

        castAura(player1, creature.getId());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(lastCard);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("An empty library does not prevent the Aura from resolving")
    void resolvesWithEmptyLibrary() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of());

        castAura(player1, creature.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Power can become negative and recovers when cards leave the graveyard")
    void negativePowerRecoversWhenGraveyardShrinks() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DawnhartDisciple());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FearOfDeath());
        aura.setAttachedTo(creature.getId());
        harness.setGraveyard(player1, List.of(new Forest(), new DawnhartDisciple(), new Forest()));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.setGraveyard(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    private void castAura(com.github.laxika.magicalvibes.model.Player player, java.util.UUID targetId) {
        harness.setHand(player, List.of(new FearOfDeath()));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
