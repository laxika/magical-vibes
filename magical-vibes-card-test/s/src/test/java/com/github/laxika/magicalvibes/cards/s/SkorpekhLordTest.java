package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkorpekhLord.class, Ornithopter.class, GrizzlyBears.class})
class SkorpekhLordTest extends BaseCardTest {

    @Test
    void buffsOtherArtifactCreaturesYouControl() {
        harness.addToBattlefield(player1, new SkorpekhLord());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent nonArtifactCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentArtifactCreature = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, artifactCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, artifactCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nonArtifactCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nonArtifactCreature, Keyword.MENACE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentArtifactCreature)).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, opponentArtifactCreature, Keyword.MENACE)).isFalse();
    }

    @Test
    void unearthReturnsWithHasteAndExilesAtTheNextEndStep() {
        harness.setGraveyard(player1, List.of(new SkorpekhLord()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent lord = findPermanent(player1, "Skorpekh Lord");
        assertThat(gqs.hasKeyword(gd, lord, Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Skorpekh Lord");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Skorpekh Lord");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Skorpekh Lord"));
    }

    @Test
    void commandProtocolsExcludeTheirSourceAndStackAcrossLords() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SkorpekhLord());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);

        Permanent second = harness.addToBattlefieldAndReturn(player1, new SkorpekhLord());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, artifactCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, artifactCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.MENACE)).isTrue();
    }

    @Test
    void unearthedLordBuffsOtherArtifactsAndItsExileRemovesTheBuff() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setGraveyard(player1, List.of(new SkorpekhLord()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, artifactCreature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skorpekh Lord");
        assertThat(gqs.getEffectivePower(gd, artifactCreature)).isZero();
        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.MENACE)).isFalse();
    }

    @Test
    void unearthedLordIsExiledInsteadOfDyingToLethalDamage() {
        harness.setGraveyard(player1, List.of(new SkorpekhLord()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        findPermanent(player1, "Skorpekh Lord").setMarkedDamage(2);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Skorpekh Lord");
        harness.assertNotInGraveyard(player1, "Skorpekh Lord");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Skorpekh Lord"));
    }

    @Test
    void unearthCannotBeActivatedOutsideAMainPhase() {
        harness.setGraveyard(player1, List.of(new SkorpekhLord()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Skorpekh Lord");
    }

    @Test
    void unearthRequiresAnEmptyStackAndReturnsOnlyTheActivatingCard() {
        SkorpekhLord first = new SkorpekhLord();
        SkorpekhLord second = new SkorpekhLord();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateGraveyardAbility(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(first.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(second.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second);
    }
}
