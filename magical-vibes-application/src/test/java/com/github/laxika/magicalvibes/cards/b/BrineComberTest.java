package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrineComber.class, BrineboundGift.class, FountainOfYouth.class, GrizzlyBears.class, Pacifism.class, Shock.class})
class BrineComberTest extends BaseCardTest {

    @Test
    @DisplayName("Brine Comber creates a Spirit when it enters")
    void createsSpiritOnFrontFaceEntry() {
        harness.setHand(player1, List.of(new BrineComber()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("Brine Comber creates a Spirit when targeted by an Aura spell")
    void createsSpiritWhenTargetedByAuraSpell() {
        Permanent comber = addCreatureReady(player1, new BrineComber());
        harness.setHand(player2, List.of(new Pacifism()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0, comber.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("Disturb casts Brinebound Gift transformed and creates a Spirit on entry")
    void disturbEntersTransformedAndCreatesSpirit() {
        Permanent aura = castWithDisturb();

        assertThat(aura.isTransformed()).isTrue();
        assertThat(aura.getCard()).isInstanceOf(BrineboundGift.class);
        assertThat(aura.getAttachedTo()).isNotNull();
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("Brinebound Gift creates a Spirit when its enchanted creature is targeted by an Aura")
    void backFaceTriggersWhenEnchantedCreatureIsTargetedByAuraSpell() {
        Permanent aura = castWithDisturb();
        Permanent enchantedCreature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(aura.getAttachedTo()))
                .findFirst()
                .orElseThrow();

        harness.setHand(player2, List.of(new Pacifism()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0, enchantedCreature.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
    }

    @Test
    @DisplayName("Brinebound Gift does not trigger when its enchanted creature is targeted by a non-Aura spell")
    void backFaceDoesNotTriggerForNonAuraSpell() {
        Permanent aura = castWithDisturb();
        Permanent enchantedCreature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(aura.getAttachedTo()))
                .findFirst()
                .orElseThrow();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, enchantedCreature.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("Brinebound Gift is exiled instead of going to the graveyard")
    void backFaceIsExiledInsteadOfGoingToGraveyard() {
        Permanent aura = castWithDisturb();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, aura));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId()))
                .contains(aura.getOriginalCard().getId());
    }

    @Test
    @DisplayName("Disturb requires a creature target")
    void disturbRequiresCreatureTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new BrineComber()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private Permanent castWithDisturb() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new BrineComber()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castFlashback(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof BrineComber)
                .findFirst()
                .orElseThrow();
    }
}
