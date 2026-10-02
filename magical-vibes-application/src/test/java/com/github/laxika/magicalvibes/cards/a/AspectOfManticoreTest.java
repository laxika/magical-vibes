package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.cards.r.ReturnToNature;
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

@CardUsed({AspectOfManticore.class, NyxbornCourser.class, ReturnToNature.class})
class AspectOfManticoreTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+0")
    void enchantedCreatureGetsPowerBoost() {
        Permanent bears = castAspectOfManticore();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enchanted creature gains first strike until end of turn")
    void enchantedCreatureGainsFirstStrikeUntilEndOfTurn() {
        Permanent bears = castAspectOfManticore();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("The static boost is lost when the Aura leaves")
    void effectsAreLostWhenAuraLeaves() {
        Permanent bears = castAspectOfManticore();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Aspect of Manticore"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Flash allows enchanting an opponent's creature during their turn")
    void canEnchantOpponentsCreatureAtInstantSpeed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGIN_COMBAT);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new AspectOfManticore()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The entry trigger still grants first strike if the Aura is destroyed in response")
    void triggerResolvesAfterAuraIsDestroyed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        harness.setHand(player1, List.of(new AspectOfManticore()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Aspect of Manticore");
        harness.setHand(player2, List.of(new ReturnToNature()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castModalInstant(player2, 0, 1, List.of(aura.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aspect of Manticore");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    private Permanent castAspectOfManticore() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new AspectOfManticore()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        return bears;
    }
}
