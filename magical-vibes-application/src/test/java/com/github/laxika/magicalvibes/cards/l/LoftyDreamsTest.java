package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoftyDreams.class, Demystify.class, FountainOfYouth.class, GrizzlyBears.class, Unsummon.class})
class LoftyDreamsTest extends BaseCardTest {

    @Test
    void resolvingAttachesDrawsAndAppliesEffects() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LoftyDreams()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof LoftyDreams
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(creature.getId()));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new LoftyDreams()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void effectsEndWhenAuraLeaves() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LoftyDreams()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        Permanent aura = findPermanent(player1, "Lofty Dreams");
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, aura.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    void summoningSickCreaturesIncludingTargetCanConvokeGenericCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        List<Permanent> convokers = List.of(target, second, third);
        convokers.forEach(creature -> creature.setSummoningSick(true));
        harness.setHand(player1, List.of(new LoftyDreams()));
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 2);

        gs.playCard(gd, player1, 0, 0, target.getId(), null, List.of(),
                convokers.stream().map(Permanent::getId).toList());

        assertThat(convokers).allMatch(Permanent::isTapped);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Lofty Dreams").getAttachedTo()).isEqualTo(target.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isFalse();
    }

    @Test
    void enchantingOpponentsCreatureDrawsForAuraController() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LoftyDreams()));
        harness.setHand(player2, List.of());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(findPermanent(player1, "Lofty Dreams").getAttachedTo()).isEqualTo(target.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    void targetLeavingBeforeResolutionPreventsEntryAndDraw() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        LoftyDreams aura = new LoftyDreams();
        harness.setHand(player1, List.of(aura));
        GrizzlyBears libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Lofty Dreams")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aura);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void destroyingAuraInResponseToDrawTriggerDoesNotPreventDraw() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LoftyDreams()));
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        Permanent aura = findPermanent(player1, "Lofty Dreams");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, aura.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Lofty Dreams")).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }
}
