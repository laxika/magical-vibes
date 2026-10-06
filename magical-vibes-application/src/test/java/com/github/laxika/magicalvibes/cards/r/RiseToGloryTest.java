package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AnimateDead;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiseToGlory.class, GrizzlyBears.class, HolyStrength.class, AnimateDead.class})
class RiseToGloryTest extends BaseCardTest {

    @Test
    void returnsTargetCreature() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        cast(new int[]{0}, List.of(creature.getId()));

        assertThat(findPermanent(player1, "Grizzly Bears")).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    void returnedAuraCanEnchantCreatureReturnedByTheSameSpell() {
        Card creature = new GrizzlyBears();
        Card aura = new HolyStrength();
        harness.setGraveyard(player1, List.of(creature, aura));

        cast(new int[]{0, 1}, List.of(creature.getId(), aura.getId()));
        Permanent returnedCreature = findPermanent(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, returnedCreature.getId());

        assertThat(findPermanent(player1, "Holy Strength").getAttachedTo()).isEqualTo(returnedCreature.getId());
    }

    @Test
    void returnedAuraCanEnchantAnOpponentsPermanent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Card aura = new HolyStrength();
        harness.setGraveyard(player1, List.of(aura));

        cast(new int[]{1}, List.of(aura.getId()));
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(findPermanent(player1, "Holy Strength").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void leavesAuraInGraveyardWhenNothingCanEnchantIt() {
        Card aura = new HolyStrength();
        harness.setGraveyard(player1, List.of(aura));

        cast(new int[]{1}, List.of(aura.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(aura.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void rejectsNonAuraCardForAuraMode() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        assertThatThrownBy(() -> cast(new int[]{1}, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<UUID> targetIds) {
        castWithoutResolving(modes, targetIds);
        harness.passBothPriorities();
    }

    private void castWithoutResolving(int[] modes, List<UUID> targetIds) {
        harness.setHand(player1, List.of(new RiseToGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, targetIds, List.of());
    }


    @Test
    void returnsCreatureWhenAuraTargetLeavesGraveyard() {
        Card creature = new GrizzlyBears();
        Card aura = new HolyStrength();
        harness.setGraveyard(player1, List.of(creature, aura));

        castWithoutResolving(new int[]{0, 1}, List.of(creature.getId(), aura.getId()));
        harness.setGraveyard(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Holy Strength");
    }

    @Test
    void returnsAuraWhenCreatureTargetLeavesGraveyard() {
        Permanent host = addCreatureReady(player2, new GrizzlyBears());
        Card creature = new GrizzlyBears();
        Card aura = new HolyStrength();
        harness.setGraveyard(player1, List.of(creature, aura));

        castWithoutResolving(new int[]{0, 1}, List.of(creature.getId(), aura.getId()));
        harness.setGraveyard(player1, List.of(aura));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, host.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Holy Strength").getAttachedTo()).isEqualTo(host.getId());
    }

    @Test
    void rejectsCreatureInOpponentsGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));

        assertThatThrownBy(() -> castWithoutResolving(new int[]{0}, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsAuraInOpponentsGraveyard() {
        Card aura = new HolyStrength();
        harness.setGraveyard(player2, List.of(aura));

        assertThatThrownBy(() -> castWithoutResolving(new int[]{1}, List.of(aura.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnedGraveyardAuraOffersLegalCreatureCardAttachment() {
        Card creature = new GrizzlyBears();
        Card aura = new AnimateDead();
        harness.setGraveyard(player1, List.of(creature, aura));

        cast(new int[]{1}, List.of(aura.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }
}
