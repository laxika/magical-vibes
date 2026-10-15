package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulOfTheros.class, BronzeSable.class})
class SoulOfTherosTest extends BaseCardTest {

    @Test
    @DisplayName("Battlefield ability boosts own creatures and grants keywords")
    void battlefieldAbilityBoostsOwnCreaturesAndGrantsKeywords() {
        harness.addToBattlefield(player1, new SoulOfTheros());
        harness.addToBattlefield(player1, new BronzeSable());
        harness.addToBattlefield(player2, new BronzeSable());
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
        harness.addToBattlefield(player1, new BronzeSable());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);
        harness.passBothPriorities();

        for (Permanent permanent : gd.playerBattlefields.get(player1.getId())) {
            assertThat(permanent.getPowerModifier()).isZero();
            assertThat(permanent.getToughnessModifier()).isZero();
            assertThat(gqs.hasKeyword(gd, permanent, Keyword.FIRST_STRIKE)).isFalse();
            assertThat(gqs.hasKeyword(gd, permanent, Keyword.LIFELINK)).isFalse();
        }
    }

    @Test
    @DisplayName("Graveyard ability exiles source and affects own creatures")
    void graveyardAbilityExilesSourceAndAffectsOwnCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new SoulOfTheros()));
        harness.addToBattlefield(player1, new BronzeSable());
        harness.addToBattlefield(player2, new BronzeSable());
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

    @Test
    @CardUsed({SoulOfTheros.class, BronzeSable.class, RayOfCommand.class})
    void sourceStolenInResponseDoesNotGainKeywords() {
        harness.addToBattlefield(player1, new SoulOfTheros());
        harness.addToBattlefield(player1, new BronzeSable());
        Permanent soul = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent sable = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castInstant(player2, 0, soul.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(soul);
        harness.passBothPriorities();

        assertThat(soul.getPowerModifier()).isZero();
        assertThat(soul.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, soul, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, soul, Keyword.LIFELINK)).isFalse();
        assertThat(sable.getPowerModifier()).isEqualTo(2);
        assertThat(sable.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, sable, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sable, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void repeatedActivationsStackButDoNotAffectLaterCreatures() {
        harness.addToBattlefield(player1, new SoulOfTheros());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new BronzeSable());

        Permanent soul = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent lateCreature = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(soul.getPowerModifier()).isEqualTo(4);
        assertThat(soul.getToughnessModifier()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, soul, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, soul, Keyword.LIFELINK)).isTrue();
        assertThat(lateCreature.getPowerModifier()).isZero();
        assertThat(lateCreature.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void graveyardAbilityIncludesCreaturesPresentAtResolutionAndExpiresAtCleanup() {
        harness.setGraveyard(player1, List.of(new SoulOfTheros()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.addToBattlefield(player1, new BronzeSable());
        Permanent sable = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.passBothPriorities();

        assertThat(sable.getPowerModifier()).isEqualTo(2);
        assertThat(sable.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, sable, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sable, Keyword.LIFELINK)).isTrue();

        harness.addToBattlefield(player1, new BronzeSable());
        Permanent lateCreature = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(lateCreature.getPowerModifier()).isZero();
        assertThat(lateCreature.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.LIFELINK)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);
        harness.passBothPriorities();

        assertThat(sable.getPowerModifier()).isZero();
        assertThat(sable.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, sable, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, sable, Keyword.LIFELINK)).isFalse();
    }
}

