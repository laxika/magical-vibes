package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.cards.h.HonorOfThePure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KorSpiritdancerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 for each Aura attached to it")
    void getsBoostForEachAttachedAura() {
        Permanent kor = harness.addToBattlefieldAndReturn(player1, new KorSpiritdancer());
        Permanent firstAura = new Permanent(new HolyStrength());
        firstAura.setAttachedTo(kor.getId());
        Permanent secondAura = new Permanent(new HolyStrength());
        secondAura.setAttachedTo(kor.getId());
        gd.playerBattlefields.get(player1.getId()).addAll(List.of(firstAura, secondAura));

        assertThat(harness.getGameQueryService().getEffectivePower(gd, kor)).isEqualTo(6);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, kor)).isEqualTo(10);
    }

    @Test
    @DisplayName("Casting an Aura spell prompts to draw a card")
    void auraCastPromptsForDraw() {
        harness.addToBattlefield(player1, new KorSpiritdancer());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, target.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting the Aura trigger draws a card")
    void acceptingAuraTriggerDrawsCard() {
        harness.addToBattlefield(player1, new KorSpiritdancer());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Declining the Aura trigger does not draw a card")
    void decliningAuraTriggerDoesNotDrawCard() {
        harness.addToBattlefield(player1, new KorSpiritdancer());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card notDrawn = new GrizzlyBears();
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.setLibrary(player1, List.of(notDrawn));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(notDrawn);
        assertThat(gd.playerDecks.get(player1.getId())).contains(notDrawn);
    }

    @Test
    @DisplayName("A non-Aura enchantment does not trigger")
    void nonAuraDoesNotTrigger() {
        harness.addToBattlefield(player1, new KorSpiritdancer());
        harness.setHand(player1, List.of(new HonorOfThePure()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }
}
