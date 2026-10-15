package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.h.HyenaUmbra;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.h.HonorOfThePure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KorSpiritdancer.class, HolyStrength.class, GrizzlyBears.class,
        HonorOfThePure.class, HyenaUmbra.class})
class KorSpiritdancerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 for each Aura attached to it")
    void getsBoostForEachAttachedAura() {
        Permanent kor = harness.addToBattlefieldAndReturn(player1, new KorSpiritdancer());
        Permanent firstAura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        firstAura.setAttachedTo(kor.getId());
        Permanent secondAura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        secondAura.setAttachedTo(kor.getId());

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

    @Test
    @DisplayName("Opponent-controlled Auras count, and moving one removes the bonus")
    void countsOpponentAuraOnlyWhileAttached() {
        Permanent kor = harness.addToBattlefieldAndReturn(player1, new KorSpiritdancer());
        Permanent otherKor = harness.addToBattlefieldAndReturn(player2, new KorSpiritdancer());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new HyenaUmbra());
        aura.setAttachedTo(kor.getId());

        assertThat(harness.getGameQueryService().getEffectivePower(gd, kor)).isEqualTo(3);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, kor)).isEqualTo(5);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, otherKor)).isZero();
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, otherKor)).isEqualTo(2);

        aura.setAttachedTo(otherKor.getId());

        assertThat(harness.getGameQueryService().getEffectivePower(gd, kor)).isZero();
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, kor)).isEqualTo(2);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, otherKor)).isEqualTo(3);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, otherKor)).isEqualTo(5);
    }

    @Test
    @DisplayName("An Aura cast on an opponent's creature draws before the Aura resolves")
    void drawsBeforeAuraResolvesRegardlessOfTarget() {
        harness.addToBattlefield(player1, new KorSpiritdancer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KorSpiritdancer());
        Card drawn = new KorSpiritdancer();
        Card aura = new HyenaUmbra();
        harness.setHand(player1, List.of(aura));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(aura.getId());
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, target)).isZero();
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent casting an Aura on Kor Spiritdancer does not trigger it")
    void opponentAuraCastDoesNotTrigger() {
        Permanent kor = harness.addToBattlefieldAndReturn(player1, new KorSpiritdancer());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new HyenaUmbra()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castEnchantment(player2, 0, kor.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("An Aura entering without being cast does not trigger a draw")
    void auraEnteringWithoutCastDoesNotTrigger() {
        Permanent kor = harness.addToBattlefieldAndReturn(player1, new KorSpiritdancer());
        Permanent aura = harness.enterBattlefieldAndReturn(player1, new HyenaUmbra());
        aura.setAttachedTo(kor.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }
}
