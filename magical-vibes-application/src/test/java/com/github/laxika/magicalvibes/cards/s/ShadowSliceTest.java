package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArmoredTransport;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({ShadowSlice.class, ArmoredTransport.class, TurnToFrog.class})
class ShadowSliceTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent loses 3 life when cipher is declined")
    void opponentLosesThreeLife() {
        harness.setHand(player1, List.of(new ShadowSlice()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Shadow Slice");
    }

    @Test
    @DisplayName("Encodes on a creature and casts a copy after combat damage")
    void encodesAndCastsCopy() {
        Permanent attacker = addCreatureReady(player1, new ArmoredTransport());
        harness.setHand(player1, List.of(new ShadowSlice()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());

        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getName().equals("Shadow Slice"));
        harness.assertNotInGraveyard(player1, "Shadow Slice");
        harness.assertLife(player2, 17);

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 12);
        assertThat(gd.exiledCards).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotInGraveyard(player1, "Shadow Slice");
    }

    @Test
    @DisplayName("Cannot target the caster")
    void cannotTargetCaster() {
        harness.setHand(player1, List.of(new ShadowSlice()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Shadow Slice");
    }

    @Test
    @DisplayName("Accepting cipher without a creature leaves the spell in the graveyard")
    void cannotEncodeWithoutCreature() {
        harness.setHand(player1, List.of(new ShadowSlice()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Shadow Slice");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Declining a cipher copy leaves the original encoded")
    void canDeclineCombatDamageCopy() {
        Permanent attacker = addCreatureReady(player1, new ArmoredTransport());
        harness.setHand(player1, List.of(new ShadowSlice()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player2, 15);
        assertThat(gd.exiledCards).hasSize(1);
        harness.assertNotInGraveyard(player1, "Shadow Slice");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Losing all abilities after encoding prevents the cipher trigger")
    void abilityRemovalSuppressesCipherTrigger() {
        Permanent attacker = addCreatureReady(player1, new ArmoredTransport());
        harness.setHand(player1, List.of(new ShadowSlice(), new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, attacker.getId());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).hasSize(1);
    }
}
