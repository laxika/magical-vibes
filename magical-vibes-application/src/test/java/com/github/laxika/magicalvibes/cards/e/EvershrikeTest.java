package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Evershrike")
class EvershrikeTest extends BaseCardTest {

    private Permanent evershrikeOnBattlefield() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Evershrike"))
                .findFirst().orElse(null);
    }

    @Test
    @DisplayName("Graveyard ability returns Evershrike and attaches a chosen Aura within X")
    void returnsAndAttachesAura() {
        Evershrike evershrike = new Evershrike();
        harness.setGraveyard(player1, List.of(evershrike));
        harness.setHand(player1, List.of(new HolyStrength())); // mana value 1
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0, 0, 1); // X = 1
        harness.passBothPriorities(); // return to battlefield, then prompt Aura choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.TargetedHandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent evershrike1 = evershrikeOnBattlefield();
        assertThat(evershrike1).isNotNull();

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Holy Strength"))
                .findFirst().orElse(null);
        assertThat(aura).isNotNull();
        assertThat(aura.getAttachedTo()).isEqualTo(evershrike1.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getName().equals("Evershrike"));
    }

    @Test
    @DisplayName("Evershrike gets +2/+2 for each Aura attached to it")
    void staticBoostPerAura() {
        Evershrike evershrike = new Evershrike();
        harness.setGraveyard(player1, List.of(evershrike));
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0, 0, 1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent evershrike1 = evershrikeOnBattlefield();
        // Base 2/2 + static +2/+2 (one Aura) + Holy Strength +1/+2 = 5/6
        assertThat(harness.getGameQueryService().getEffectivePower(gd, evershrike1)).isEqualTo(5);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, evershrike1)).isEqualTo(6);
    }

    @Test
    @DisplayName("Declining the Aura exiles Evershrike")
    void decliningExilesEvershrike() {
        Evershrike evershrike = new Evershrike();
        harness.setGraveyard(player1, List.of(evershrike));
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0, 0, 1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.TargetedHandCardChoice.class);

        harness.handleCardChosen(player1, -1); // decline

        assertThat(evershrikeOnBattlefield()).isNull();
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Evershrike"));
    }

    @Test
    @DisplayName("Evershrike is exiled when no Aura with mana value X or less is in hand")
    void exiledWhenNoEligibleAura() {
        Evershrike evershrike = new Evershrike();
        harness.setGraveyard(player1, List.of(evershrike));
        harness.setHand(player1, List.of(new Pacifism())); // mana value 2 > X (1)
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0, 0, 1); // X = 1
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetedHandCardChoice.class)).isNull();
        assertThat(evershrikeOnBattlefield()).isNull();
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Evershrike"));
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Pacifism"));
    }

    @Test
    @DisplayName("A higher X allows a costlier Aura to be attached")
    void higherXAllowsCostlierAura() {
        Evershrike evershrike = new Evershrike();
        harness.setGraveyard(player1, List.of(evershrike));
        harness.setHand(player1, List.of(new Pacifism())); // mana value 2
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0, 0, 2); // X = 2
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.TargetedHandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent evershrike1 = evershrikeOnBattlefield();
        assertThat(evershrike1).isNotNull();
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Pacifism"))
                .findFirst().orElse(null);
        assertThat(aura).isNotNull();
        assertThat(aura.getAttachedTo()).isEqualTo(evershrike1.getId());
    }
}
