package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarduRunemark.class, GrizzlyBears.class, EliteVanguard.class, ScatheZombies.class, FountainOfYouth.class})
class MarduRunemarkTest extends BaseCardTest {

    @Test
    void enchantedCreatureGetsPlusTwoPlusTwo() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attach(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void enchantedCreatureHasFirstStrikeWhileAuraControllerControlsWhitePermanent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attach(player1, creature);
        addCreatureReady(player1, new EliteVanguard());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void enchantedCreatureHasFirstStrikeWhileAuraControllerControlsBlackPermanent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attach(player1, creature);
        addCreatureReady(player1, new ScatheZombies());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void firstStrikeRequiresAuraControllerToControlQualifyingPermanent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attach(player1, creature);
        addCreatureReady(player2, new EliteVanguard());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void bonusesDisappearWhenAuraLeaves() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attach(player1, creature);
        addCreatureReady(player1, new EliteVanguard());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new MarduRunemark()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void losingLastQualifyingPermanentRemovesOnlyFirstStrike() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attach(player1, creature);
        Permanent white = addCreatureReady(player1, new EliteVanguard());
        Permanent black = addCreatureReady(player1, new ScatheZombies());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(white);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(black);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void enchantedWhiteCreatureItselfSatisfiesCondition() {
        Permanent creature = addCreatureReady(player1, new EliteVanguard());
        attach(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void resolvesAttachedToOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MarduRunemark()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Mardu Runemark");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent attach(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new MarduRunemark());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
