package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CurseOfChains;
import com.github.laxika.magicalvibes.cards.k.KithkinShielddare;
import com.github.laxika.magicalvibes.cards.l.LureboundScarecrow;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MineExcavation.class, LureboundScarecrow.class, CurseOfChains.class,
        KithkinShielddare.class, MistmeadowSkulk.class})
class MineExcavationTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target artifact card from graveyard to hand")
    void returnsTargetArtifactToHand() {
        Card artifact = new LureboundScarecrow();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new MineExcavation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, artifact.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(artifact.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(artifact.getId()));
    }

    @Test
    @DisplayName("Returns a target enchantment card from graveyard to hand")
    void returnsTargetEnchantmentToHand() {
        Card enchantment = new CurseOfChains();
        harness.setGraveyard(player1, List.of(enchantment));
        harness.setHand(player1, List.of(new MineExcavation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, enchantment.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(enchantment.getId()));
    }

    @Test
    @DisplayName("Can target an opponent's graveyard; the card returns to its owner's hand")
    void returnsFromOpponentGraveyardToOwnersHand() {
        Card artifact = new LureboundScarecrow();
        harness.setGraveyard(player2, List.of(artifact));
        harness.setHand(player1, List.of(new MineExcavation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, artifact.getId());

        GameData gd = harness.getGameData();
        // "to its owner's hand" — the opponent's card goes to the opponent's hand, not the caster's.
        assertThat(gd.playerHands.get(player2.getId())).anyMatch(c -> c.getId().equals(artifact.getId()));
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getId().equals(artifact.getId()));
    }

    @Test
    @DisplayName("Cannot target a creature card that is neither artifact nor enchantment")
    void cannotTargetPlainCreature() {
        Card creature = new KithkinShielddare();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MineExcavation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Conspire taps two color-sharing creatures and queues a copy of the spell")
    void conspireTapsCreaturesAndQueuesCopy() {
        Card artifact = new LureboundScarecrow();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new MineExcavation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        Permanent skulk = addCreatureReady(player1, new MistmeadowSkulk());
        Permanent shielddare = addCreatureReady(player1, new KithkinShielddare());

        harness.castWithConspire(player1, 0, artifact.getId(), List.of(skulk.getId(), shielddare.getId()));

        GameData gd = harness.getGameData();
        assertThat(skulk.isTapped()).isTrue();
        assertThat(shielddare.isTapped()).isTrue();

        // The spell plus one conspire copy trigger are on the stack.
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack).anyMatch(e -> e.getEffectsToResolve().stream()
                .anyMatch(fx -> fx instanceof CopyControllerCastSpellEffect));
    }

    @Test
    @DisplayName("Conspire is rejected when a chosen creature does not share a color with the spell")
    void conspireRejectsColorlessCreature() {
        Card artifact = new LureboundScarecrow();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new MineExcavation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        Permanent skulk = addCreatureReady(player1, new MistmeadowSkulk());
        Permanent scarecrow = addCreatureReady(player1, new LureboundScarecrow()); // colorless

        assertThatThrownBy(() -> harness.castWithConspire(player1, 0, artifact.getId(),
                List.of(skulk.getId(), scarecrow.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Conspire can retarget its copy and both spell instances resolve")
    void conspireCopyCanChooseNewGraveyardTarget() {
        Card originalTarget = new LureboundScarecrow();
        Card copyTarget = new LureboundScarecrow();
        harness.setGraveyard(player1, List.of(originalTarget, copyTarget));
        harness.setHand(player1, List.of(new MineExcavation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        Permanent skulk = addCreatureReady(player1, new MistmeadowSkulk());
        Permanent shielddare = addCreatureReady(player1, new KithkinShielddare());

        harness.castWithConspire(player1, 0, originalTarget.getId(),
                List.of(skulk.getId(), shielddare.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(originalTarget.getId(), copyTarget.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(originalTarget.getId())
                        || card.getId().equals(copyTarget.getId()));
    }

    @Test
    void doesNotReturnTargetThatLeftGraveyardBeforeResolution() {
        Card artifact = new LureboundScarecrow();
        Card otherArtifact = new LureboundScarecrow();
        harness.setGraveyard(player1, List.of(artifact, otherArtifact));
        harness.setHand(player1, List.of(new MineExcavation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, artifact.getId());
        harness.setGraveyard(player1, List.of(otherArtifact));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).contains(otherArtifact.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void conspireCanTapSummoningSickCreaturesAndKeepOriginalTarget() {
        Card artifact = new LureboundScarecrow();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new MineExcavation()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        Permanent skulk = harness.addToBattlefieldAndReturn(player1, new MistmeadowSkulk());
        Permanent shielddare = harness.addToBattlefieldAndReturn(player1, new KithkinShielddare());
        skulk.setSummoningSick(true);
        shielddare.setSummoningSick(true);

        harness.castWithConspire(player1, 0, artifact.getId(), List.of(skulk.getId(), shielddare.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(skulk.isTapped()).isTrue();
        assertThat(shielddare.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId).containsExactly(artifact.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Mine Excavation");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void conspireRejectsUsingSameCreatureTwice() {
        Card artifact = new LureboundScarecrow();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new MineExcavation()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        Permanent skulk = addCreatureReady(player1, new MistmeadowSkulk());

        assertThatThrownBy(() -> harness.castWithConspire(player1, 0, artifact.getId(),
                List.of(skulk.getId(), skulk.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(skulk.isTapped()).isFalse();
    }

    @Test
    void conspireRejectsTappedCreature() {
        Card artifact = new LureboundScarecrow();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new MineExcavation()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        Permanent skulk = addCreatureReady(player1, new MistmeadowSkulk());
        Permanent shielddare = addCreatureReady(player1, new KithkinShielddare());
        shielddare.tap();

        assertThatThrownBy(() -> harness.castWithConspire(player1, 0, artifact.getId(),
                List.of(skulk.getId(), shielddare.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(skulk.isTapped()).isFalse();
    }

    @Test
    void conspireRejectsOpponentsCreature() {
        Card artifact = new LureboundScarecrow();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new MineExcavation()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        Permanent skulk = addCreatureReady(player1, new MistmeadowSkulk());
        Permanent shielddare = addCreatureReady(player2, new KithkinShielddare());

        assertThatThrownBy(() -> harness.castWithConspire(player1, 0, artifact.getId(),
                List.of(skulk.getId(), shielddare.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(skulk.isTapped()).isFalse();
        assertThat(shielddare.isTapped()).isFalse();
    }

    @Test
    void conspireCopyCanReturnEnchantmentFromOpponentsGraveyard() {
        Card artifact = new LureboundScarecrow();
        Card enchantment = new CurseOfChains();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setGraveyard(player2, List.of(enchantment));
        harness.setHand(player1, List.of(new MineExcavation()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        Permanent skulk = addCreatureReady(player1, new MistmeadowSkulk());
        Permanent shielddare = addCreatureReady(player1, new KithkinShielddare());

        harness.castWithConspire(player1, 0, artifact.getId(), List.of(skulk.getId(), shielddare.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, enchantment.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId).containsExactly(artifact.getId());
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getId).contains(enchantment.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId).doesNotContain(enchantment.getId());
        assertThat(gd.stack).isEmpty();
    }
}
