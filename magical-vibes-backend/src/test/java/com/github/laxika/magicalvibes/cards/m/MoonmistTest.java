package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GatstafShepherd;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.PreventCombatDamageExceptBySubtypesEffect;
import com.github.laxika.magicalvibes.model.effect.TransformAllEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MoonmistTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Moonmist has correct effects")
    void hasCorrectEffects() {
        Moonmist card = new Moonmist();

        List<?> spellEffects = card.getEffects(EffectSlot.SPELL);
        assertThat(spellEffects).hasSize(2);
        assertThat(spellEffects.get(0)).isInstanceOf(TransformAllEffect.class);
        TransformAllEffect transformEffect = (TransformAllEffect) spellEffects.get(0);
        assertThat(transformEffect.filter()).isInstanceOf(PermanentHasSubtypePredicate.class);
        PermanentHasSubtypePredicate humanFilter = (PermanentHasSubtypePredicate) transformEffect.filter();
        assertThat(humanFilter.subtype()).isEqualTo(CardSubtype.HUMAN);

        assertThat(spellEffects.get(1)).isInstanceOf(PreventCombatDamageExceptBySubtypesEffect.class);
        PreventCombatDamageExceptBySubtypesEffect preventEffect =
                (PreventCombatDamageExceptBySubtypesEffect) spellEffects.get(1);
        assertThat(preventEffect.exemptPredicate()).isInstanceOf(PermanentHasAnySubtypePredicate.class);
        PermanentHasAnySubtypePredicate exemptFilter =
                (PermanentHasAnySubtypePredicate) preventEffect.exemptPredicate();
        assertThat(exemptFilter.subtypes()).containsExactlyInAnyOrder(CardSubtype.WEREWOLF, CardSubtype.WOLF);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Moonmist puts it on the stack as an instant")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new Moonmist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Moonmist");
    }

    // ===== Transform all Humans =====

    @Test
    @DisplayName("Transforms Human DFC creatures when it resolves")
    void transformsHumanDfcCreatures() {
        // GatstafShepherd is a Human Werewolf DFC
        harness.addToBattlefield(player1, new GatstafShepherd());
        Permanent shepherd = findPermanent(player1, "Gatstaf Shepherd");

        assertThat(shepherd.isTransformed()).isFalse();

        harness.setHand(player1, List.of(new Moonmist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(shepherd.isTransformed()).isTrue();
        assertThat(shepherd.getCard().getName()).isEqualTo("Gatstaf Howler");
    }

    @Test
    @DisplayName("Transforms Human DFCs on both players' battlefields")
    void transformsHumansOnBothBattlefields() {
        harness.addToBattlefield(player1, new GatstafShepherd());
        harness.addToBattlefield(player2, new GatstafShepherd());
        Permanent p1Shepherd = findPermanent(player1, "Gatstaf Shepherd");
        Permanent p2Shepherd = findPermanent(player2, "Gatstaf Shepherd");

        harness.setHand(player1, List.of(new Moonmist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(p1Shepherd.isTransformed()).isTrue();
        assertThat(p2Shepherd.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Transforms already-transformed Human DFCs back to front face")
    void transformsAlreadyTransformedBack() {
        harness.addToBattlefield(player1, new GatstafShepherd());
        Permanent shepherd = findPermanent(player1, "Gatstaf Shepherd");

        // Manually transform to back face (Gatstaf Howler is a Werewolf, no longer Human)
        Card backFace = shepherd.getOriginalCard().getBackFaceCard();
        shepherd.setCard(backFace);
        shepherd.setTransformed(true);
        assertThat(shepherd.getCard().getName()).isEqualTo("Gatstaf Howler");

        // Moonmist says "Transform all Humans" — Gatstaf Howler is a Werewolf (not Human)
        // so it should NOT be affected
        harness.setHand(player1, List.of(new Moonmist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // Should stay transformed since Gatstaf Howler is not a Human
        assertThat(shepherd.isTransformed()).isTrue();
        assertThat(shepherd.getCard().getName()).isEqualTo("Gatstaf Howler");
    }

    @Test
    @DisplayName("Does not affect non-Human creatures")
    void doesNotAffectNonHumans() {
        // GrizzlyBears is a Bear, not a Human
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new Moonmist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(bears.isTransformed()).isFalse();
    }

    // ===== Combat damage prevention =====

    @Test
    @DisplayName("Sets combat damage exempt predicate after resolving")
    void setsCombatDamageExemptPredicate() {
        harness.setHand(player1, List.of(new Moonmist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.combatDamageExemptPredicate).isNotNull();
    }

    @Test
    @DisplayName("Non-Werewolf/Wolf creatures are prevented from dealing combat damage")
    void nonWerewolfCreaturesPreventedFromDealingCombatDamage() {
        // GrizzlyBears is a Bear — not a Werewolf or Wolf
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new Moonmist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isTrue();
    }

    @Test
    @DisplayName("Non-Werewolf/Wolf creatures can still deal non-combat damage")
    void nonWerewolfCreaturesCanDealNonCombatDamage() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new Moonmist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // Non-combat damage should not be prevented
        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, false)).isFalse();
    }

    @Test
    @DisplayName("Werewolf creatures are NOT prevented from dealing combat damage")
    void werewolfCreaturesCanDealCombatDamage() {
        // GatstafShepherd front face is Human Werewolf — has Werewolf subtype
        harness.addToBattlefield(player1, new GatstafShepherd());
        Permanent shepherd = findPermanent(player1, "Gatstaf Shepherd");

        harness.setHand(player1, List.of(new Moonmist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // After Moonmist, the shepherd transformed to Gatstaf Howler (Werewolf subtype)
        // Should be able to deal combat damage
        assertThat(gqs.isPreventedFromDealingDamage(gd, shepherd, true)).isFalse();
    }

    // ===== Goes to graveyard =====

    @Test
    @DisplayName("Moonmist goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new Moonmist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Moonmist"));
    }

    // ===== Helper methods =====

}
