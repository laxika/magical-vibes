package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SoulOfTherosTest extends BaseCardTest {

    @Test
    @DisplayName("Battlefield ability boosts own creatures and grants keywords")
    void battlefieldAbilityBoostsOwnCreaturesAndGrantsKeywords() {
        harness.addToBattlefield(player1, new SoulOfTheros());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        List<Permanent> ownPermanents = gd.playerBattlefields.get(player1.getId());
        for (Permanent permanent : ownPermanents) {
            assertThat(permanent.getPowerModifier()).isEqualTo(2);
            assertThat(permanent.getToughnessModifier()).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, permanent, Keyword.FIRST_STRIKE)).isTrue();
            assertThat(gqs.hasKeyword(gd, permanent, Keyword.LIFELINK)).isTrue();
        }

        Permanent opponentBear = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(opponentBear.getPowerModifier()).isZero();
        assertThat(opponentBear.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Temporary ability effects wear off at cleanup")
    void temporaryAbilityEffectsWearOffAtCleanup() {
        harness.addToBattlefield(player1, new SoulOfTheros());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        for (Permanent permanent : gd.playerBattlefields.get(player1.getId())) {
            assertThat(permanent.getPowerModifier()).isZero();
            assertThat(permanent.getToughnessModifier()).isZero();
            if (permanent.getCard().getName().equals("Grizzly Bears")) {
                assertThat(gqs.hasKeyword(gd, permanent, Keyword.FIRST_STRIKE)).isFalse();
                assertThat(gqs.hasKeyword(gd, permanent, Keyword.LIFELINK)).isFalse();
            }
        }
    }

    @Test
    @DisplayName("Graveyard ability exiles source and affects own creatures")
    void graveyardAbilityExilesSourceAndAffectsOwnCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new SoulOfTheros()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Soul of Theros");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Soul of Theros"));

        harness.passBothPriorities();

        Permanent ownBear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(ownBear.getPowerModifier()).isEqualTo(2);
        assertThat(ownBear.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.LIFELINK)).isTrue();

        Permanent opponentBear = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(opponentBear.getPowerModifier()).isZero();
        assertThat(opponentBear.getToughnessModifier()).isZero();
    }
}
