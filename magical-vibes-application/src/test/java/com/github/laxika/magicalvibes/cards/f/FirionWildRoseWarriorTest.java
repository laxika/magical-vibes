package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FirionWildRoseWarrior.class, GrizzlyBears.class, LeoninScimitar.class, Boomerang.class})
class FirionWildRoseWarriorTest extends BaseCardTest {

    @Test
    void equippedCreaturesYouControlHaveHaste() {
        Permanent firion = addCreatureReady(player1, new FirionWildRoseWarrior());
        Permanent equippedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        scimitar.setAttachedTo(equippedCreature.getId());

        assertThat(gqs.hasKeyword(gd, equippedCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, firion, Keyword.HASTE)).isFalse();
    }

    @Test
    void createsReducedEquipCostTokenCopyForNontokenEquipment() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new FirionWildRoseWarrior());
        harness.castFromHand(player1, new LeoninScimitar(), "{1}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);

        harness.activateAbility(player1, tokenIndex, 0, target.getId());
        harness.passBothPriorities();

        assertThat(token.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void sacrificesTokenAtBeginningOfNextUpkeep() {
        addCreatureReady(player1, new FirionWildRoseWarrior());
        harness.castFromHand(player1, new LeoninScimitar(), "{1}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());

        advanceToUpkeep(player2);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void tokenCopyDoesNotTriggerAnotherCopy() {
        addCreatureReady(player1, new FirionWildRoseWarrior());
        harness.castFromHand(player1, new LeoninScimitar(), "{1}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList()).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsEquipmentDoesNotCreateCopy() {
        addCreatureReady(player1, new FirionWildRoseWarrior());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new LeoninScimitar(), "{1}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void tokenReductionDoesNotReduceOriginalEquipCost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new FirionWildRoseWarrior());
        harness.castFromHand(player1, new LeoninScimitar(), "{1}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent original = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof LeoninScimitar)
                .filter(permanent -> !permanent.getCard().isToken())
                .findFirst().orElseThrow();
        int originalIndex = gd.playerBattlefields.get(player1.getId()).indexOf(original);

        assertThatThrownBy(() -> harness.activateAbility(player1, originalIndex, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, originalIndex, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(original.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void createsCopyUsingLastKnownInformationWhenEquipmentLeaves() {
        addCreatureReady(player1, new FirionWildRoseWarrior());
        harness.castFromHand(player1, new LeoninScimitar(), "{1}");
        harness.passBothPriorities();
        Permanent original = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof LeoninScimitar)
                .findFirst().orElseThrow();
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, original.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Leonin Scimitar");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList()).hasSize(1);
    }

    @Test
    void hasteIsLimitedToYourEquippedCreaturesAndEndsWhenFirionLeaves() {
        Permanent firion = addCreatureReady(player1, new FirionWildRoseWarrior());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent ownEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent opposingEquipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        ownEquipment.setAttachedTo(ownCreature.getId());
        opposingEquipment.setAttachedTo(opposingCreature.getId());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HASTE)).isFalse();

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, firion.getId());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isFalse();
    }
}
