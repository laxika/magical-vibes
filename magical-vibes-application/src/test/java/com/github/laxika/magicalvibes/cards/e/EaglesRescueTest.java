package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({EaglesRescue.class, GrizzlyBears.class, Ornithopter.class})
class EaglesRescueTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Eagle's Rescue attaches it and grants +2/+2 and flying")
    void resolvingAttachesAndBoosts() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new EaglesRescue()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Eagle's Rescue")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Graveyard ability returns Eagle's Rescue attached to a creature with power 1 or less")
    void graveyardAbilityReturnsAttachedToSmallCreature() {
        Permanent ornithopter = addCreatureReady(player1, new Ornithopter());
        harness.setGraveyard(player1, List.of(new EaglesRescue()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateGraveyardAbility(player1, 0, ornithopter.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Eagle's Rescue");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Eagle's Rescue")
                        && p.isAttached()
                        && p.getAttachedTo().equals(ornithopter.getId()));
        assertThat(gqs.getEffectivePower(gd, ornithopter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ornithopter)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ornithopter, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Graveyard ability rejects a creature with power greater than 1")
    void graveyardAbilityRejectsLargeCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new EaglesRescue()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control with power 1 or less");
    }

    @Test
    @DisplayName("Graveyard ability rejects an opponent's creature")
    void graveyardAbilityRejectsOpponentCreature() {
        Permanent ornithopter = addCreatureReady(player2, new Ornithopter());
        harness.setGraveyard(player1, List.of(new EaglesRescue()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, ornithopter.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control with power 1 or less");
    }

    @Test
    @DisplayName("Graveyard ability can only be activated as a sorcery")
    void graveyardAbilityIsSorcerySpeedOnly() {
        Permanent ornithopter = addCreatureReady(player1, new Ornithopter());
        harness.setGraveyard(player1, List.of(new EaglesRescue()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, ornithopter.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
