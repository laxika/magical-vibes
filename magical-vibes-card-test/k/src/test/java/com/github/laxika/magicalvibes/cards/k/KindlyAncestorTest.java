package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AncestorsEmbrace;
import com.github.laxika.magicalvibes.cards.s.SnarlingWolf;
import com.github.laxika.magicalvibes.cards.w.WeddingInvitation;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KindlyAncestor.class, AncestorsEmbrace.class, SnarlingWolf.class, WeddingInvitation.class})
class KindlyAncestorTest extends BaseCardTest {

    @Test
    @DisplayName("Disturb casts Kindly Ancestor transformed as Ancestor's Embrace")
    void disturbEntersTransformedAttachedAndGrantsLifelink() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new SnarlingWolf());
        Permanent aura = castWithDisturb(wolf.getId());

        assertThat(aura.isTransformed()).isTrue();
        assertThat(aura.getCard()).isInstanceOf(AncestorsEmbrace.class);
        assertThat(aura.getAttachedTo()).isEqualTo(wolf.getId());
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.LIFELINK)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Disturb requires a creature target")
    void disturbCannotTargetNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WeddingInvitation());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new KindlyAncestor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Ancestor's Embrace is exiled instead of going to the graveyard")
    void transformedAuraIsExiledInsteadOfGraveyard() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new SnarlingWolf());
        Permanent aura = castWithDisturb(wolf.getId());
        UUID cardId = aura.getOriginalCard().getId();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, aura));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).contains(cardId);
    }

    @Test
    void frontFaceCombatDamageGainsLife() {
        harness.castFromHand(player1, new KindlyAncestor(), "{2}{W}");
        harness.passBothPriorities();
        Permanent ancestor = findPermanent(player1, "Kindly Ancestor");
        ancestor.setSummoningSick(false);
        ancestor.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void enchantedOpponentsCreatureGainsLifeForItsController() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new SnarlingWolf());
        castWithDisturb(wolf.getId());
        wolf.setSummoningSick(false);
        wolf.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 21);
    }

    @Test
    void disturbSpellIsExiledWhenItsTargetLeavesBeforeResolution() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new SnarlingWolf());
        beginDisturb(wolf.getId());
        UUID ancestorId = gd.stack.getFirst().getCard().getId();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, wolf));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ancestor's Embrace");
        harness.assertNotInGraveyard(player1, "Kindly Ancestor");
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).contains(ancestorId);
    }

    @Test
    void auraIsExiledWhenEnchantedCreatureDies() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new SnarlingWolf());
        Permanent aura = castWithDisturb(wolf.getId());
        UUID ancestorId = aura.getOriginalCard().getId();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, wolf));
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Ancestor's Embrace");
        harness.assertNotInGraveyard(player1, "Kindly Ancestor");
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).contains(ancestorId);
    }

    @Test
    void frontFaceGoesToGraveyardNormally() {
        Permanent ancestor = harness.addToBattlefieldAndReturn(player1, new KindlyAncestor());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, ancestor));

        harness.assertInGraveyard(player1, "Kindly Ancestor");
        assertThat(gd.exiledCards).isEmpty();
    }

    private Permanent castWithDisturb(UUID targetId) {
        beginDisturb(targetId);
        harness.passBothPriorities();
        return findPermanent(player1, "Ancestor's Embrace");
    }

    private void beginDisturb(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new KindlyAncestor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0, targetId);
    }
}
