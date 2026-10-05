package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrislySpectacle;
import com.github.laxika.magicalvibes.cards.g.GutterSkulk;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({MidnightRecovery.class, GutterSkulk.class, GrislySpectacle.class})
class MidnightRecoveryTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature card from your graveyard to your hand when cipher is declined")
    void returnsCreatureCardToHand() {
        Card creature = new GutterSkulk();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MidnightRecovery()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, creature.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(creature.getId()));
        harness.assertInGraveyard(player1, "Midnight Recovery");
    }

    @Test
    @DisplayName("Cannot target a non-creature card in your graveyard")
    void cannotTargetNonCreatureCard() {
        Card instant = new GrislySpectacle();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new MidnightRecovery()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card creature = new GutterSkulk();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new MidnightRecovery()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("An illegal graveyard target prevents both recovery and encoding")
    void illegalTargetPreventsEncoding() {
        Card creature = new GutterSkulk();
        harness.setGraveyard(player1, List.of(creature));
        addCreatureReady(player1, new GutterSkulk());
        harness.setHand(player1, List.of(new MidnightRecovery()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, creature.getId());
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Gutter Skulk");
        harness.assertInGraveyard(player1, "Midnight Recovery");
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getName().equals("Midnight Recovery"));
    }

    @Test
    @DisplayName("The controller may decline to cast the cipher copy")
    void mayDeclineCipherCopy() {
        Card first = new GutterSkulk();
        Card second = new GutterSkulk();
        harness.setGraveyard(player1, List.of(first, second));
        Permanent attacker = addCreatureReady(player1, new GutterSkulk());
        harness.setHand(player1, List.of(new MidnightRecovery()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, first.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getId().equals(second.getId()));
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getId().equals(second.getId()));
        assertThat(gd.exiledCards).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Encodes on a creature and casts a copy after combat damage")
    void encodesAndCastsCopy() {
        Card first = new GutterSkulk();
        Card second = new GutterSkulk();
        harness.setGraveyard(player1, List.of(first, second));
        Permanent attacker = addCreatureReady(player1, new GutterSkulk());
        harness.setHand(player1, List.of(new MidnightRecovery()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, first.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());

        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getName().equals("Midnight Recovery"));
        harness.assertNotInGraveyard(player1, "Midnight Recovery");

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(second.getId()));
        assertThat(gd.exiledCards).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInGraveyard(player1, "Midnight Recovery");
    }
}
