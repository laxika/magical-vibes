package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AgentMariaHill;
import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvisibleWomanSueStorm.class, AgentMariaHill.class, BurstOfStrength.class, GrizzlyBears.class})
class InvisibleWomanSueStormTest extends BaseCardTest {

    @Test
    @DisplayName("Putting a +1/+1 counter on another controlled Hero may create a Wall")
    void createsWallForOtherControlledHero() {
        addCreatureReady(player1, new InvisibleWomanSueStorm());
        Permanent hero = addCreatureReady(player1, new AgentMariaHill());

        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, hero.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent wall = findPermanent(player1, "Wall");
        assertThat(wall.getCard().getColor()).isNull();
        assertThat(wall.getCard().getSubtypes()).containsExactly(CardSubtype.WALL);
        assertThat(gqs.getEffectivePower(gd, wall)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, wall, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("The trigger ignores Sue herself, non-Heroes, and opposing placements")
    void ignoresInvalidCounterPlacements() {
        Permanent sue = addCreatureReady(player1, new InvisibleWomanSueStorm());
        Permanent nonHero = addCreatureReady(player1, new GrizzlyBears());
        Permanent hero = addCreatureReady(player1, new AgentMariaHill());

        putCounter(player1, sue);
        putCounter(player1, nonHero);

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new BurstOfStrength()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castInstant(player2, 0, hero.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Wall")).isEmpty();
    }

    private void putCounter(com.github.laxika.magicalvibes.model.Player player, Permanent target) {
        harness.setHand(player, List.of(new BurstOfStrength()));
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.castInstant(player, 0, target.getId());
        harness.passBothPriorities();
    }
}
